package tweetcapturing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;


public class OnlineIterator {

    private Config config;
    private TwitterStream twitterStream;
    private FilterQuery filter; 
    private Instant stopInstant; 
    private DbWriter dbWriter; 
    
    private String[] keywords;
    private String[] languages;
    
    private Status lastTweet;
    private boolean isFirstTweet = false;
    
    private StatusListener listener = new StatusListener() {
        @Override
        public void onStatus(Status status) {
            lastTweet = status;
            if(isFirstTweet) {
                isFirstTweet = false;
                
                BufferedWriter bufferedWriter;
                try {
                    bufferedWriter = new BufferedWriter(new FileWriter("hole.txt"));
                    bufferedWriter.write("end  : " + 
                            lastTweet.getCreatedAt().toInstant().toString() + 
                            ",," + 
                            lastTweet.getId());
                    bufferedWriter.close();
                    System.out.println("store end");
                } catch (IOException ex) {
                    Logger.getLogger(OnlineIterator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            Tweet tweet = new Tweet(status);
            
            if(tweet.getStatusTimestampCreation().isAfter(stopInstant)) {
                twitterStream.clearListeners();
                twitterStream.cleanUp();
                twitterStream.shutdown();

                dbWriter.finish();
            } else {
                dbWriter.enqueue(tweet);
//                    System.out.println(tweet.toString());
                System.out.println("!");
            }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice sdn) {
            System.out.println("onDeletionNotice");
        }

        @Override
        public void onTrackLimitationNotice(int i) {
            System.out.println("onTrackLimitationNotice");
        }

        @Override
        public void onScrubGeo(long l, long l1) {
            System.out.println("onScrubGeo");
        }

        @Override
        public void onStallWarning(StallWarning sw) {
            System.out.println("onStallWarning: " + sw.getPercentFull());
            
            if(sw.getPercentFull() > 75) {
                //close all stream
                twitterStream.clearListeners();
                twitterStream.cleanUp();
                twitterStream.shutdown();
                
                BufferedWriter bufferedWriter;
                try {
                    bufferedWriter = new BufferedWriter(new FileWriter("hole.txt"));
                    bufferedWriter.write("stall: " + sw.getPercentFull() + "\nstart: " + 
                            lastTweet.getCreatedAt().toInstant().toString() + 
                            ",," + 
                            lastTweet.getId());
                    bufferedWriter.close();
                    System.out.println("store start");
                } catch (IOException ex) {
                    Logger.getLogger(OnlineIterator.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //wait 1min
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(OnlineIterator.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                //restart stream
                isFirstTweet = true;
                startTwitterStream();
            }
        }

        @Override
        public void onException(Exception excptn) {
            System.out.println("onException");
            excptn.printStackTrace();
        }
    };
    
    
    private void startTwitterStream() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
          .setOAuthConsumerKey("Consumer Key")
          .setOAuthConsumerSecret("Consumer Secret")
          .setOAuthAccessToken("Access Token")
          .setOAuthAccessTokenSecret("Access Token Secret");
        
        System.out.println("START STREAM");
        
        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);

        filter = new FilterQuery(0, new long[0], keywords, new double[0][0], languages);
        twitterStream.filter(filter);
    }
    
    
    protected OnlineIterator(Config config, Instant startInstant) throws ClassNotFoundException, SQLException {

        this.config = config;

        if (config.getDuration()<0)
            stopInstant = Instant.MAX;
        else
            stopInstant = startInstant.plusMillis(config.getDuration());
        
        System.out.println("End at: " + stopInstant.toString());
        
        keywords = config.getKeywords().toArray(new String[0]);
        languages = new String[]{ config.getLng() };

        dbWriter = new DbWriter(config, "INSERT INTO `" + config.getDbTable() +"` VALUES (?,?,?,?,?,?,?,?,?,?,?)", null);
        dbWriter.start();
        
        startTwitterStream();
    }
}
