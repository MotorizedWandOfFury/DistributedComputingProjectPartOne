/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ping
 */
public class ServerRouterMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        ServerRouter router;
        
        try {
            router = new ServerRouter();
            router.listen();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServerRouterMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
