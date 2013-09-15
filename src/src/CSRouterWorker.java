/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ping
 */
public class CSRouterWorker extends Thread {

    private PrintWriter writer;
    private BufferedReader reader;
    private static final Logger LOG = Logger.getLogger(CSRouterWorker.class.getName());
    private Socket client;
    private int clientPort;
    private int clientCount = 0;

    public CSRouterWorker(Socket s) {
        this.client = s;
        clientPort = s.getPort();
                
    }

    @Override
    public void run() {
        LOG.log(Level.INFO, "New Thread spawned for client at port: {0}", clientPort);
        try {
            writer = new PrintWriter(client.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String request;

            while (true) {
                request = reader.readLine();
                
                if(request == null || request.contentEquals("end")){
                    break;
                }
                
                LOG.log(Level.INFO, "Recieved request from client {0}: {1}", new Object[]{clientPort, request});

                //LOG.log(Level.INFO, "Responding to request from client {0}", clientPort);

                //writer.println("Request acknowledged: " + request);
                
                //router.add("client" + clientCount, clientPort);
                
            }

        } catch (IOException ex) {
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
