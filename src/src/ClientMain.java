/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package src;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ping
 */
public class ClientMain {

    private static final Logger LOG = Logger.getLogger(ClientMain.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        System.out.println("Choose name");
        String name = scan.nextLine();
        System.out.println("Choose port");
        int port = scan.nextInt();
        try {
            Client client = new Client(name, port);
            boolean status = client.register();
            System.out.println(status ? "Client registered with CSRouter successfully" : "Client failed to register");
            if (status) {
               client.userService();
               client.listen();
            }


        } catch (IOException ex) {
            Logger.getLogger(ClientMain.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
