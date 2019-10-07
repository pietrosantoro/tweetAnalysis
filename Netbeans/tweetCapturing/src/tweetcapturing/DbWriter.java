package tweetcapturing;

import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Handle the interaction with the database. This class implements a parallel thread that receive some tweets
 * and execute the specified prepared query on them.
 * 
 * @author stefano
 *
 */
public class DbWriter extends Thread {

    private LinkedBlockingQueue<Tweet> queue;

    private MySQLBridge dbConn;
    private boolean stop = false;

    private int values = 0;
    private final int threshold = 200;
    private PreparedStatement query;
    
    /**
     * 
     * @param config
     * @param preparedQuery
     * @param filter if !=null means that i want to use a table with filter as end name
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    public DbWriter(Config config, String preparedQuery, String filter) throws ClassNotFoundException, SQLException {
        // connect to database
        if(filter==null) {
            dbConn = new MySQLBridge( config.getDbAddress(), 
                                    config.getDbUser(), 
                                    config.getDbPassword(), 
                                    config.getDbSchema(), 
                                    config.getDbTable());
        } else {
            dbConn = new MySQLBridge( config.getDbAddress(), 
                                    config.getDbUser(), 
                                    config.getDbPassword(), 
                                    config.getDbSchema(), 
                                    config.getDbTable()+"-"+filter);
        }

        // prepare query
        query = dbConn.getConnection().prepareStatement(preparedQuery);

        queue = new LinkedBlockingQueue<>();
    }
	
    
    private String removeBadChar(String s) {
        if (s == null) return null;
        StringBuilder sb = new StringBuilder();
        for(int i = 0 ; i < s.length() ; i++){ 
            if (Character.isHighSurrogate(s.charAt(i))) {
                continue;
            }
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }
    
    
    /**
     * Store the received tweet in the database. This method actually performs the query only when either a threshold number
     * of tweets have been received or when the tweet input queue is empty, i.e. further waiting would waste time.
     * 
     * @param tweet the tweet to be stored in the database
     */
    private void storeInDB(Tweet tweet) {
//        System.out.println("store: " + tweet.getStatusId());
        try {
            // ignore 'poison' element, see https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/BlockingQueue.html
            if(tweet.getStatusId()> 0) {
                
                /**
                 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                 * the fields User, statusGeolocation and statusPlace
                 * can be null
                 */
                
//                System.out.println("id>0");
               
                query.setLong(1, tweet.getStatusId());
                
                if(tweet.getUser()==null) {
                    query.setNull(2, java.sql.Types.BIGINT);
                } else {
                    query.setLong(2, tweet.getUser().getId());
                }
                
                /**
                 * encoding problem with tweet text since tweet are ancoded in 
                 * utf8mb4 rather than utf8
                 */
                //query.setString(3, tweet.getStatusText());
                query.setString(3, removeBadChar(tweet.getStatusText()));
                        
                query.setString(4, tweet.getTimestampAsString());
                query.setInt(5, tweet.getStatusFavouriteCount());
                
                if(tweet.getStatusGeoLocation()==null) {
                    query.setNull(6, java.sql.Types.FLOAT);
                    query.setNull(7, java.sql.Types.FLOAT);
                } else {
                    query.setDouble(6, tweet.getStatusGeoLocation().getLatitude());
                    query.setDouble(7, tweet.getStatusGeoLocation().getLongitude());
                }
                
                query.setString(8, tweet.getStatusLanguage());
                
                if(tweet.getStatusPlace()==null) {
                    query.setNull(9, java.sql.Types.VARCHAR);
                } else {
                    query.setString(9, tweet.getStatusPlace().getCountryCode());
                }
                
                query.setInt(10, tweet.getStatusRetweetCount());
                
                if(tweet.getIsRetweet()) {
                    // if it's a retweet value is -1
                    query.setLong(11, -1);
                } else {
                    if(tweet.getIsReply()>0) {
                        // if it's a reply value is the id of the original tweet
                        query.setLong(11, tweet.getIsReply());
                    } else {
                        // if it's a normal tweet value is 0
                        query.setLong(11, 0);
                    }
                }
                
                query.addBatch();

                values++;
            }

            // this check must be done outside the above if, otherwise the last tweets won't be written in the DB
            if(values >= threshold || queue.isEmpty()) {
                System.out.println("eb");   //executebatch
                try{
                    query.executeBatch();
                } catch (BatchUpdateException be){
                    System.err.print("DI  ");
                }
                dbConn.getConnection().commit();

                query.clearBatch();
                values = 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public ResultSet retrieveData(String query) throws SQLException {
        return dbConn.retrieveData(query);
    }
    
    /**
     * Add a tweet to the queue of tweets waiting to be written in the database.
     * 
     * @param tweet tweet to add to the database
     */
    public void enqueue(Tweet tweet) {
        try {
            queue.put(tweet);
//            System.out.println("enqueued");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
	
    /**
     * Signal the end of tweet stream. This forces the flush of the enqueued tweets to be stored in the database, if any.
     */
    public void finish() {
        stop = true;
        queue.add(new Tweet(-1L, "", Instant.now())); // insert 'poison' element to unlock the waiting queue, see https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/BlockingQueue.html
    }

    @Override
    public void run() {
        while(!stop || !queue.isEmpty()) {
            try {
                storeInDB(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        dbConn.closeConnection();
    }
	
}
