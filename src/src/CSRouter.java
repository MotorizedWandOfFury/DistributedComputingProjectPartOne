/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.IOException;
import java.net.ServerSocket;
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
    public CSRouter() throws IOException {
        socket = new ServerSocket(PORT);
        LOG.log(Level.INFO, "Starting up CSRouter on port {0}", PORT);     
    }

    public void listen() throws IOException {

        LOG.info("Server is now listening");
        try {
            while (true){
                new CSRouterWorker(socket.accept()).start();  
            }
        } finally {
            socket.close();
        }
    }
}
