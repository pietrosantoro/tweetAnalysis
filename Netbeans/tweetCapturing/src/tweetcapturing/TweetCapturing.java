/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetcapturing;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author luigi
 */
public class TweetCapturing {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Config config;
        try {
            config = new Config("localConfig.conf");
        } catch (IOException ex) {
            Logger.getLogger(TweetCapturing.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        try {
            OnlineIterator iterator = new OnlineIterator(config, Instant.now());
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TweetCapturing.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.err.println("end");
    }
    
}
