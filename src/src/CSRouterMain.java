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
public class CSRouterMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        CSRouter server;
        try {
            server = new CSRouter();
            server.listen();

        } catch (IOException ex) {
            Logger.getLogger(CSRouterMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
