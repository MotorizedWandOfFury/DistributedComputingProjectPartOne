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
import java.util.Scanner;

/**
*
* @author David
*/
public class Client {

    private ServerSocket receiveSocket;
    public static final int receivePort = 5999;
    private ServerSocket sendSocket;
    public static final int sendPort = 6000;
    private ServerSocket commSocket;
    public static final int commPort = 6001;
    private static final Logger LOG = Logger.getLogger(Client.class.getName());
    private int clientCount = 0;
    private ExecutorService exec = Executors.newFixedThreadPool(11);

    public Client() throws IOException {
        socket = new ServerSocket(PORT);
        LOG.log(Level.INFO, "Starting up Client on port {0}", PORT);
    }
    
    // Note I am currently assuming that the server maintains the transfer queue, not the client.  Given that by the documentation, all transfers must run 
    // through the server, it would have to be the one to decide which goes through if it is swamped.
    private class ClientReceiver implements Runnable {

        private PrintWriter writer;
        private BufferedReader reader;
        private Socket client;
        private int clientPort;

        public ClientWorker(Socket s) {
            client = s;
            //s.getPort() should be replaced with the appropriate port on the server, not sure what the best way to find that is.
            clientPort = s.getPort();
        }

        @Override
        public void run() {
            LOG.log(Level.INFO, "New Receiver Thread spawned for client at port: {0}", clientPort);
            try {
                writer = new PrintWriter(client.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
                
                String packet = "";
                String data = "";
                
                // We'll need to change the condition to close on completed file transfer, not sure the best way to do that.
                while ((packet = reader.readLine()) != null) { // we end this loop when no request has been recieved
                    LOG.log(Level.INFO, "Received packet from client {0}: {1}", new Object[]{clientPort, request});
                    
                    data = parsePacket(packet);
                    
                    // Need to figure out where on the disc we want to store interim data, in what format, &c.
                }

            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                try {
                    writer.close();
                    reader.close();
                    client.close();
                    LOG.log(Level.INFO, "Server {0} disconnected. Ending thread.", clientPort);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    
    private class ClientSender implements Runnable {

        private PrintWriter writer;
        private BufferedReader reader;
        private Socket client;
        private int clientPort;
        private String filename;

        public ClientWorker(Socket s, String name) {
            client = s;
            //s.getPort() should be replaced with the appropriate port on the server, not sure what the best way to find that is.
            clientPort = s.getPort();
            filename = name;
        }

        @Override
        public void run() {
            LOG.log(Level.INFO, "New Receiver Thread spawned for client at port: {0}", clientPort);
            try {
                writer = new PrintWriter(client.getOutputStream(), true);
                // CHANGE NEEDED: need to change where reader gets its data to the file specified in filename.
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String data = "";
                String packet = "";
                
                // If I remember bufffered readers correctly, the condition should work when reading from a file; not sure. Will need to change it to only
                // read a certain number of bytes though, unless java handles sizing things into the correctly sized packet automagically.
                while ((data = reader.readLine()) != null) { // we end this loop when no request has been recieved
                    LOG.log(Level.INFO, "Creating packet for server {0}: {1}", new Object[]{clientPort, request});
                    
                    packet = packData(data);
                    writer.println(packet);
                    
                }

            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                try {
                    writer.close();
                    reader.close();
                    client.close();
                    LOG.log(Level.INFO, "Server {0} disconnected. Ending thread.", clientPort);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    // This is super simple and super unsafe.  Essentially just prints console input over the socket, assuming the user knows what they're doing. Not very
    // Java, and very C, but worry not, I do plan to improve this substantially; this is what first drafts are for.
    private class ClientCommunicator implements Runnable {
      private Socket server;
      private int serverPort;
      private PrintWriter writer;
      // I'm not sure if scanners are still the accepted way to do console input, may need to be changed.
      private Scanner scanner;
      private String input;
      
      public ClientCommunicator(Socket s) {
        client = s;
        //s.getPort() should be replaced with the appropriate port on the server, not sure what the best way to find that is.
        clientPort = s.getPort();
      }
      
      // I can't actually think of a situation in which we would want to spawn multiple communicators per client, but doing it like this for consistency
      @Override
      public void run() {
        LOG.log(Level.INFO, "New Communicator Thread spawned for server at port: {0}", clientPort);
        input = "";
        
        //presumably there is some sort of exit command the client uses.  Not sure what it is, so I'm using quit for the moment
        while (input != "QUIT") {
          input = scanner.nextLine();
          if (input != "" && input != "QUIT") {
            writer.println(input)
          }
        }
        
      }
    }
    
    // placeholder
    // This should probably throw an exception if the data is unparseable.  I'm not sure whether there's a prebuilt exception
    // for that or if we need to write our own, or how I would go about writing our own.
    private String parsePacket(String packet) {
      String data = "";
      // CHANGE NEEDED: replace with code to parse the packet string into the data string
      data = packet;
      return data;
    }
    
    // placeholder
    // This probably should throw an IOException if the data on the disc is corrupted.  Not sure, will check.
    private String packData(String data) {
      String packet = "";
      // CHANGE NEEDED: replace with code to pack the data into a packet
      packet = data;
      return packet;
    }
    
    public void listen() throws IOException {

        LOG.info("Client is now listening");
        
        try {
            while (true) {
                exec.execute(new ClientWorker(socket.accept()));

            }
        } finally {
            socket.close();
        }
    }

    //Unneeded, but kept for reference
    /*
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
    */

    //CHANGE NEEDED: The code is wrong for the client side, but the method definitely needs to exist, just need to clarify the protocol.
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
/*
    private void addNewClient(String clientName, String portNumber) throws UnknownHostException, IOException {
        try (Socket s = new Socket("localhost", ServerRouter.PORT)) {
            PrintWriter w = new PrintWriter(s.getOutputStream(), true);

            w.println("ADD " + clientName + " " + portNumber);

            w.close();
        }
    }*/
}
