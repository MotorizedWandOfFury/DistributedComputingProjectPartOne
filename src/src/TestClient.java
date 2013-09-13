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
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ping
 */
public class TestClient {

    private static final Logger LOG = Logger.getLogger(TestClient.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Socket s;
        try {
            s = new Socket("localhost", CSRouter.PORT);
            LOG.log(Level.INFO, "TestClient started on port {0}", s.getLocalPort());
            PrintWriter writer = new PrintWriter(s.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));

            Scanner scan = new Scanner(System.in);

            String request = scan.nextLine();

            while (!request.contentEquals("end")) {
                writer.println(request);
                LOG.log(Level.INFO, "Sent request to server: {0}", request);
                
                String response = reader.readLine();
                
                LOG.log(Level.INFO, "Server response: {0}", response);
                
                request = scan.next();
            }
            
            if(request.contentEquals("end")){
            writer.close();
            reader.close();
            s.close();
        }


        } catch (UnknownHostException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }


    }
}
