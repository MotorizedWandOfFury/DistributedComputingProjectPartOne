/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.BufferedReader;
import java.io.IOException;
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

                while ((request = reader.readLine()) != null) { // we end this loop when no request has been recieved

                    String[] commands = request.split("\\s");

                    LOG.log(Level.INFO, "Responding to request from client {0}: {1}", new Object[]{clientPort, request});

                    dispatch(commands, writer, reader);

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

    private void dispatch(String[] commands, PrintWriter writer, BufferedReader reader) throws UnknownHostException, IOException {
        switch (commands[0]) {
            case "HELLO":
                if (commands.length != 3) {
                    LOG.warning("Insufficient number of parameters in ADD command");
                    break;
                } //gaurd clause

                String clientName = commands[1];
                String portNumber = commands[2];

                addNewClient(clientName, portNumber);

                writer.println("ACKNOWLEDGED");
                break;
            case "BYE":
                break;
            case "FILE":
                break;
            case "GET":
                break;
            default:
                LOG.log(Level.WARNING, "Unknown command: {0}", commands[0]);
        }
    }

    private int getPort(String clientName) throws UnknownHostException, IOException {
        String[] commands;
        try (Socket s = new Socket("localhost", ServerRouter.PORT)) {
            PrintWriter w = new PrintWriter(s.getOutputStream(), true);
            BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
            w.println("FIND " + clientName);
            String response = r.readLine();
            commands = response.split("\\s");
            r.close();
            w.close();
        }

        //return the port as an int if found, else return 0
        return (commands[0].contentEquals("FOUND")) ? Integer.parseInt(commands[1]) : 0;
    }

    private void addNewClient(String clientName, String portNumber) throws UnknownHostException, IOException {
        try (Socket s = new Socket("localhost", ServerRouter.PORT)) {
            PrintWriter w = new PrintWriter(s.getOutputStream(), true);

            w.println("ADD " + clientName + " " + portNumber);

            w.close();
        }
    }
}
