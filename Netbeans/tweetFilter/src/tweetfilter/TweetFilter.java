/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetfilter;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import tweetcapturing.Config;
import tweetcapturing.MySQLBridge;
import tweetcapturing.TweetCapturing;

/**
 *
 * @author luigi
 */
public class TweetFilter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Config config;
            try {
                config = new Config("localConfig.conf");
            } catch (IOException ex) {
                Logger.getLogger(TweetCapturing.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            MySQLBridge filteredDb;
            MySQLBridge originalDb;
            try {
                originalDb = new MySQLBridge(config.getDbAddress(),
                        config.getDbUser(),
                        config.getDbPassword(),
                        config.getDbSchema(),
                        config.getDbTable());
                filteredDb = new MySQLBridge(config.getDbAddress(),
                        config.getDbUser(),
                        config.getDbPassword(),
                        config.getDbSchema(),
                        config.getDbTable()+"filtered");
            } catch (ClassNotFoundException | SQLException ex) {
                Logger.getLogger(TweetFilter.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            Filtering filtering = new Filtering(config);

            ResultSet result;
            
            /*
            String test = "rt @ciao good one. some of the @ciao #excuses coming out of the trump camp are worse than ''my dog ate my homework'' https://t.co/8vtma3gvvh";
            Instant test2 = Instant.parse("2016-05-05T05:00:00Z");
            System.out.println(test);
            System.out.println(filtering.apply(test, test2));
            
            if(true) 
                return;
            */
            
            result = originalDb.retrieveData("SELECT * FROM `" + config.getDbTable() +"`");
            
            while(result.next()) {
                System.out.println(result.getString(1));
                
                //to parse timestamp string to Instant
                String a[] = result.getString(4).split(" ");
                String parsed = a[0] + "T" + a[1] + "Z";
                Instant i = Instant.parse(parsed);
                String filtered = filtering.apply(result.getString(3), i);
                //System.out.println("filtered test: " + filtered);
                
                
                if (!filtered.equals(" ")) {
                    String query = "INSERT INTO `"+config.getDbTable()+"filtered"+"` (`statusId`, `userId`, `text`, `timestamp`, `favoriteCount`, `latitude`, `longitude`, `lang`, `place`, `retweetCount`, `msgType`) VALUES ('"+
                            result.getLong(1)+"', '"+
                            result.getLong(2)+"', '"+
                            filtered+"', '"+
                            result.getString(4)+"', '"+
                            result.getInt(5)+"', '"+
                            result.getDouble(6)+"', '"+
                            result.getDouble(7)+"', '"+
                            result.getString(8)+"', '"+
                            result.getString(9)+"', '"+
                            result.getInt(10)+"', '"+
                            result.getLong(11)+"')";
                    //System.out.println(query);
                    try {
                        //System.out.println(query);
                        long res = filteredDb.insertData(query);
                        System.out.println("ret: " + res);
                    } catch (SQLException ex) {
                        //Logger.getLogger(TweetFilter.class.getName()).log(Level.SEVERE, null, ex);
                        System.err.println("duplicate key");
                    }
                }
                
            }
            
            originalDb.closeConnection();
            filteredDb.closeConnection();
            
        } catch (SQLException ex) {
            Logger.getLogger(TweetFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
