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
    private static final Logger LOG = Logger.getLogger(ServerRouter.class.getName());
    private ExecutorService exec = Executors.newSingleThreadExecutor();

    private ServerRouter() throws IOException {
        routingTable = new HashMap<>();
        service = new ServerSocket(PORT);
        LOG.log(Level.INFO, "Starting up ServerRouter on port {0}", PORT);
    }

    public void listen() throws IOException, ClassNotFoundException {
        LOG.info("ServerRouter is now listening");

        Runnable clientResolver = new Runnable() {
            @Override
            public void run() {
                try {
                    Socket client = service.accept();
                    
                    LOG.log(Level.INFO, "Servicing request from client on port {0}", client.getPort());
                    
                    
                    LOG.log(Level.INFO, "Response sent to client at port {0}, closing socket", client.getPort());   
                    client.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        };

        try {
            while (true) {
                exec.execute(clientResolver);
            }
        } finally {
            exec.shutdown();
        }
    }

    public synchronized void add(String name, int port) {
        if (!isInTable(name)) {
            routingTable.put(name, port);
        }
    }

    public synchronized int find(String name) {
        if (isInTable(name)) {
            return routingTable.get(name);
        } else {
            return 0;
        }

    }

    public boolean isInTable(String name) {
        return routingTable.containsKey(name);
    }
}
