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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ping
 */
public class CSRouter {

    private ServerSocket socket;
    public static final int PORT = 5999;
    private PrintWriter writer;
    private BufferedReader reader;
    private static final Logger LOG = Logger.getLogger(CSRouter.class.getName());

    public CSRouter() throws IOException {
        socket = new ServerSocket(PORT);
        LOG.log(Level.INFO, "Starting up CSRouter on port {0}", PORT);
    }

    public void listen() throws IOException {
        Socket client = socket.accept();
        writer = new PrintWriter(client.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        String request = "";

        while (request != null && !request.contentEquals("end") ) {
            request = reader.readLine();
            LOG.log(Level.INFO, "Message from client: {0}", request);

            LOG.info("Responding to request");

            writer.println("Request acknowledged: " + request);
        }
        if(request == null || request.contentEquals("end")  ){
            writer.close();
            reader.close();
            client.close();
        }
    }
}
