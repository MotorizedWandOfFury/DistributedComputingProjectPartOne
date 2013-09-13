/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Ping
 */
public class CSRouter {

    private Socket socket;
    private final int PORT = 5999;
    
    public CSRouter() throws UnknownHostException, IOException {
        socket = new Socket("localhost", PORT);
    }
    
}
