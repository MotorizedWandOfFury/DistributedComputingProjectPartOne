/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ping
 */
public class CSRouter {

    private ServerSocket socket;
    public static final int PORT = 5999;
    private static final Logger LOG = Logger.getLogger(CSRouter.class.getName());
    private int clientCount = 0;
    private ExecutorService exec = Executors.newFixedThreadPool(11);

    public CSRouter() throws IOException {
        socket = new ServerSocket(PORT);
        LOG.log(Level.INFO, "Starting up CSRouter on port {0}", PORT);
    }

    private class CSRouterWorker implements Runnable {

        private PrintWriter writer;
        private BufferedReader reader;
        private Socket client;
        private int clientPort;

        public CSRouterWorker(Socket s) {
            client = s;
            clientPort = s.getPort();
        }

        @Override
        public void run() {
            LOG.log(Level.INFO, "New Thread spawned for client at port: {0}", clientPort);
            try {
                writer = new PrintWriter(client.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String request = "";

                while (!client.isClosed() && (request = reader.readLine()) != null) { // we end this loop when no request has been recieved or socket is closed

                    String[] commands = request.split("\\s");

                    LOG.log(Level.INFO, "Responding to request from client {0}: {1}", new Object[]{clientPort, request});

                    dispatch(commands, client);

                    LOG.log(Level.INFO, "Response sent to client at port {0}", clientPort);
                }

            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                try {

                    writer.close();
                    reader.close();
                    client.close();
                    LOG.log(Level.INFO, "Client {0} disconnected. Ending thread.", clientPort);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void listen() throws IOException {

        LOG.info("CSRouter is now listening");

        try {
            while (true) {
                exec.execute(new CSRouterWorker(socket.accept()));

            }
        } finally {
            socket.close();
        }
    }

    private void dispatch(String[] commands, Socket client) throws UnknownHostException, IOException {
       PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
               BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        
        switch (commands[0]) {
            case "HELLO":
                if (commands.length != 3) {
                    LOG.warning("Insufficient number of parameters in ADD command");
                    writer.println("REQUESTUNSUCCESSFULL");
                    break;
                } //gaurd clause

                String clientName = commands[1];
                String portNumber = commands[2];

                if(addNewClient(clientName, portNumber)){
                    writer.println("ACKNOWLEDGED");
                } else {
                    writer.println("REQUESTUNSUCCESSFULL");
                }
         
                break;
            case "BYE":
                if (commands.length != 2) {
                    LOG.warning("Insufficient number of parameters in BYE command");
                    writer.println("REQUESTUNSUCCESSFULL");
                    break;
                } //gaurd clause

                clientName = commands[1];
                removeClient(clientName);

                LOG.log(Level.INFO, "client request serviced: BYE {0}", clientName);
                break;
            case "GET":
                long start = System.currentTimeMillis();
                if (commands.length != 3) {
                    LOG.warning("Insufficient number of parameters in GET command");
                    writer.println("REQUESTUNSUCCESSFULL");
                    break;
                } //gaurd clause

                String fileName = commands[1];
                String fromClient = commands[2];

                int fromPort = getPort(fromClient);

                if (fromPort == 0) {
                    writer.println("REQUESTUNSUCCESSFULL");
                } else {
                    File requestedFile = getFileFromClient(fromPort, fileName);
                    if (requestedFile == null) {
                        writer.println("REQUESTUNSUCCESSFULL");
                    } else {
                        writer.println("FILE");
                        LOG.info("File received. Sending to client" );
                        
                        FileInputStream fis = new FileInputStream(requestedFile);
                        BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
                        byte[] buffer = new byte[1024];
                        int count;
                        
                        while((count = fis.read(buffer)) > 0){
                            bos.write(buffer, 0, count);
                            bos.flush();
                        }
                        fis.close();
                        bos.close();
                        LOG.info("File sent");
                    }
                }

                long end = System.currentTimeMillis();
                LOG.log(Level.INFO, "client request serviced in {2}ms: GET {0} {1}", new Object[]{commands[1], commands[2], (end - start)});
                break;
            default:
                LOG.log(Level.WARNING, "Unknown command: {0}", commands[0]);
        }
    }

    private int getPort(String clientName) throws UnknownHostException, IOException {
        long start = System.currentTimeMillis();

        String[] commands;
        try (Socket s = new Socket("localhost", ServerRouter.PORT);
                PrintWriter w = new PrintWriter(s.getOutputStream(), true);
                BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

            w.println("FIND " + clientName);
            String response = r.readLine();

            commands = response.split("\\s");
        }

        long end = System.currentTimeMillis();
        LOG.log(Level.INFO, "Port name retrieved in {0} ms", (end - start));
        //return the port as an int if found, else return 0
        return (commands[0].contentEquals("FOUND")) ? Integer.parseInt(commands[1]) : 0;
    }

    private boolean addNewClient(String clientName, String portNumber) throws UnknownHostException, IOException {
        try (Socket s = new Socket("localhost", ServerRouter.PORT)) {
            PrintWriter w = new PrintWriter(s.getOutputStream(), true);
            BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));

            w.println("ADD " + clientName + " " + portNumber);
            
            String result = r.readLine();

            w.close();
            r.close();
            
            return result.contentEquals("OK") ? true : false;
        }
        
        
    }

    private void removeClient(String clientName) throws UnknownHostException, IOException {
        try (Socket s = new Socket("localhost", ServerRouter.PORT)) {
            PrintWriter w = new PrintWriter(s.getOutputStream(), true);

            w.println("REMOVE " + clientName);

            w.close();
        }
    }

    private File getFileFromClient(int port, String fileName) throws UnknownHostException, IOException {
        long start = System.currentTimeMillis();
        
        File f = null;
        int fileSize = 0;
        try (Socket fileServer = new Socket("localhost", port)) {
            PrintWriter newWriter = new PrintWriter(fileServer.getOutputStream(), true);
            BufferedReader newReader = new BufferedReader(new InputStreamReader(fileServer.getInputStream()));

            newWriter.println("GET " + fileName);

            String response = newReader.readLine();
            String[] commands = response.split("\\s");

            switch (commands[0]) {
                case "FILENOTFOUND":
                    break;
                case "FILE":
                    if (commands.length != 2) {
                        LOG.warning("Insufficient number of parameters in FILE command");
                    }                
                    try {
                        fileSize = Integer.parseInt(commands[1]);
                    } catch (NumberFormatException nfe) {
                        LOG.warning("filesize parameter was not an integer");
                        break;
                    }
                    new File("C:\\Users\\Ping\\Downloads\\temp\\").mkdir();
                    f = new File("C:\\Users\\Ping\\Downloads\\temp\\"+fileName);
                    FileOutputStream fos = new FileOutputStream(f);
                    byte[] buffer = new byte[fileSize];
                    int count;
                    
                    InputStream in = fileServer.getInputStream();
                    
                    LOG.info("Receiving file");
                    while((count = in.read(buffer)) > 0){
                        fos.write(buffer, 0, count); 
                        fos.flush();
                        LOG.log(Level.FINE, "Count variable: {0}", count);
                    }
                    
                    fos.close();
                    in.close();
                    
                    
                    LOG.info("File received");
                    break;
            }
        }
        long end = System.currentTimeMillis();
        LOG.log(Level.INFO, "File retrieval operation completed in {0}ms. File recieved was {1} bytes in size", new Object[]{(end - start), fileSize});
        return f;
    }
}
