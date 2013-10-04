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
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ping
 */
public class ServerRouter {

    private HashMap<String, Integer> routingTable;
    public static final int PORT = 6000;
    private ServerSocket service;
    private PrintWriter writer;
    private BufferedReader reader;
    private static final Logger LOG = Logger.getLogger(ServerRouter.class.getName());
    private ExecutorService exec = Executors.newCachedThreadPool();

    public ServerRouter() throws IOException {
        routingTable = new HashMap<>();
        service = new ServerSocket(PORT);
        LOG.log(Level.INFO, "Starting up ServerRouter on port {0}", PORT);
    }

    class ServerRouterWorker implements Runnable {

        private Socket client;

        public ServerRouterWorker(Socket s) {
            client = s;
        }

        @Override
        public void run() {
            try {
                LOG.log(Level.INFO, "Servicing request from client on port {0}", client.getPort());

                writer = new PrintWriter(client.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String request = reader.readLine();
                String[] commands = request.split("\\s");

                dispatch(commands, writer);

                LOG.log(Level.INFO, "Response sent to client at port {0}. Closing socket.", client.getPort());

                writer.close();
                reader.close();
                client.close();

            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void listen() throws IOException, ClassNotFoundException {
        LOG.info("ServerRouter is now listening");

        try {
            while (true) {
                exec.execute(new ServerRouterWorker(service.accept()));
            }

        } finally {
            exec.shutdown();
        }
    }

    private void dispatch(String[] commands, PrintWriter writer) {

        switch (commands[0]) { //first element of string array must be one of the accepted commands
            case "ADD":
                if (commands.length != 3) {
                    LOG.warning("Insufficient number of parameters in ADD command");
                    break;
                }

                String clientName = commands[1];
                String portNumber = commands[2];
                int port = 0;
                try {
                    port = Integer.parseInt(portNumber);
                    add(clientName, port);
                } catch (NumberFormatException nfe) {
                    LOG.warning("portNumber parameter was not an integer");
                }

                LOG.log(Level.INFO, "client request serviced: ADD {0} {1}", new Object[]{clientName, portNumber});
                break;
            case "REMOVE":
                clientName = commands[1];

                remove(clientName);

                LOG.log(Level.INFO, "client request serviced: REMOVE {0}", clientName);
                break;
            case "FIND":
                clientName = commands[1];
                int portNumberResult = find(clientName);

                if (portNumberResult == 0) {
                    writer.println("NOTFOUND");
                    writer.flush();
                } else {
                    writer.println("FOUND " + portNumberResult);
                }

                LOG.log(Level.INFO, "client request serviced: FIND {0}", clientName);
                break;
            default:
                LOG.log(Level.WARNING, "Unknown command: {0}", commands[0]);
        }
    }

    private synchronized void add(String name, int port) {
        if (!isInTable(name)) {
            routingTable.put(name, port);
        }
    }

    private synchronized int find(String name) {
        if (isInTable(name)) {
            return routingTable.get(name);
        } else {
            return 0;
        }

    }

    private synchronized void remove(String name) {
        if (isInTable(name)) {
            routingTable.remove(name);
        }
    }

    private boolean isInTable(String name) {
        return routingTable.containsKey(name);
    }
}
