/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetsearch;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tweetcapturing.Config;
import tweetcapturing.CsvHandler;
import tweetcapturing.DbWriter;
import tweetcapturing.Dumper;
import tweetcapturing.Tweet;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author luigi
 */
public class TweetSearch {


    static long querySearch(Query query, 
            Twitter twitter, 
            Instant since, 
            Instant until,
            DbWriter dbWriter,
            CsvHandler csvHandler) throws TwitterException {
        
        QueryResult result;
        Status lastTweet = null;
        
        do {
            twitter = TwitterFactory.getSingleton(); 
            result = twitter.search(query);
            List<Status> tweets = result.getTweets();
            
            if(tweets.size() > 1) {
                System.out.println(tweets.get(0).getCreatedAt().toString());
            }
            for(Status tweet : tweets) {
                lastTweet = tweet;
                if(tweet.getCreatedAt().toInstant().isAfter(since) &&
                        tweet.getCreatedAt().toInstant().isBefore(until)) {

                    //valid tweet
//                    System.out.println("@" + tweet.getId() + " - " + tweet.getCreatedAt().toString());
                    System.out.print("!");

                    Tweet supportTweet = new Tweet(tweet);
                    dbWriter.enqueue(supportTweet);   
                    csvHandler.write(supportTweet);
                } else {
                    //not valid tweet
                    System.out.print(".");
                }

                /* TO BE TESTED*/
                if(tweet.getCreatedAt().toInstant().isBefore(since)){
                    System.out.println("Too early");
                }
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

            System.out.println("\n");

        } while ((query = result.nextQuery()) != null);
        
        long ret;
        if(lastTweet.getCreatedAt().toInstant().isBefore(since)) {
            ret = 0;
        } else {
            ret = lastTweet.getId();
        }
        System.out.println("ret in fun is: " + ret);
        return ret;
    }
    
    
    public static void main(String[] args) {
        
        /*
        fondamental variables
        everything is considered in utc-0 time zone
        */
        //----------------------------------------------------------------------
        //variables for the query
        String queryString = "aaaaaaaa";
        String sinceString = "2018-06-22";
        String untilString = "2018-06-24"; //this day is excluded by the search
        String langString = "en";
        
        //variables for the time frame
        String sinceClockString = "2018-06-22T00:00:00.00Z";
        String untilClockString = "2018-06-23T23:59:59.59Z";
        
        boolean keepRetweet = true;
        
        //this is to change in case the most recent id for the query is known
        //in any other case must remain 0
        //this is used in the query as maxId
        long result = 806546513891655680L;
        //----------------------------------------------------------------------
        
        
        /*
        initialization
        */
        Config config;
        try {
            config = new Config("localConfig.conf");
        } catch (IOException ex) {
            Logger.getLogger(TweetSearch.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        String now = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        System.out.println("test run at: " + now);
        
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey("_consumer_key_")
            .setOAuthConsumerSecret("consumer_secret")
            .setOAuthAccessToken("Access_token")
            .setOAuthAccessTokenSecret("access_token_secret");
        
        
        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        
	DbWriter dbWriter;
        try {
            dbWriter = new DbWriter(config, "INSERT INTO `" + config.getDbTable() +"` VALUES (?,?,?,?,?,?,?,?,?,?,?)", null);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TweetSearch.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
	dbWriter.start();
        
        CsvHandler csvHandler;
        try {
            csvHandler = new CsvHandler(queryString);
        } catch (IOException ex) {
            Logger.getLogger(TweetSearch.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        Instant since = Instant.parse(sinceClockString);
        Instant until = Instant.parse(untilClockString);
        
       /*
        start
        */ 
        
        do {
            Query query;
            if(keepRetweet) {
                query = new Query(queryString);
            } else {
                query = new Query(queryString + " +exclude:retweets");
            }
            
            query.setCount(100);
            query.setSince(sinceString);
            if(untilString!="") {
                query.setUntil(untilString);
            }
            query.setLang(langString);
            
            if( result != 0) {
                System.out.println("last id found: " + result + "\n update query");
                query.setMaxId(result);
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(TweetSearch.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            System.out.println(query.toString());
            
            try {
                result = querySearch(query, twitter, since, until, dbWriter, csvHandler);
            } catch (TwitterException ex) {
                Logger.getLogger(TweetSearch.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            System.out.println("result is: " + result);
            
            
            
        } while(result != 0);
        
        dbWriter.finish();
        try {
            csvHandler.close();
        } catch (IOException ex) {
            Logger.getLogger(TweetSearch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    
}
