package tweetcapturing;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.User;


public class Tweet implements Serializable{
    //status variable
    private final long statusId;
    private final User user;                        //can be null
    private String statusText;
    private final Instant statusTimestampCreation;
    private final int statusFavouriteCount;
    private final GeoLocation statusGeoLocation;    //can be null
    private final String statusLanguage;
    private final Place statusPlace;                //can be null
    private final int statusRetweetCount;

    //modifiers
    private final boolean isRetweet;
    private final long isReply;
    
    
    //class variable
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**TODO: is usefull to store deleted msgs or is better to delete as twitter 
     * recomend 
     * check here on deletion notice
     * http://twitter4j.org/javadoc/twitter4j/StatusListener.html
     */


    public Tweet(Status status) {
        this.statusId = status.getId();
        this.user = status.getUser();
        this.statusText = status.getText().toLowerCase();
        this.statusTimestampCreation = status.getCreatedAt().toInstant();
        this.statusFavouriteCount = status.getFavoriteCount();
        this.statusGeoLocation = status.getGeoLocation();
        this.statusLanguage = status.getLang();
        this.statusPlace = status.getPlace();
        this.statusRetweetCount = status.getRetweetCount();
        
        this.isRetweet = status.isRetweet();
        this.isReply = status.getInReplyToStatusId();
        
    }
    
    //only used for poison tweet
    public Tweet(Long id, String text, Instant timestamp) {
        this.statusId = id;
        this.user = null;
        this.statusText = text;
        this.statusTimestampCreation = timestamp;
        this.statusFavouriteCount = 0;
        this.statusGeoLocation = null;
        this.statusLanguage = null;
        this.statusPlace = null;
        this.statusRetweetCount = 0;
        
        this.isRetweet = false;
        this.isReply = 0;
    }
    
    /**
     * only for test purposes
     * @param i a generic int to simulate some field must be always >0
     */
    public Tweet(int i){
        this.statusId = i;
        this.user = null;
        this.statusText = "i";
        this.statusTimestampCreation = Instant.now();
        this.statusFavouriteCount = i;
        this.statusGeoLocation = null;
        this.statusLanguage = null;
        this.statusPlace = null;
        this.statusRetweetCount = i;
        
        this.isRetweet = false;
        this.isReply = 0;
    }
    
    private String returnStringReplyOrRetweet() {
        if(this.getIsReply()>0) {
            return "reply to: " + Long.toString(this.getIsReply());
        } else {
            if(this.getIsRetweet()) {
                return "retweet";
            }
        }
        return "";
    }
    
    @Override
    public String toString() {
            String res =  "status: " + statusId + "\n" 
                    + "\t" + "user: " + user.getId() + "\n" 
                    + "\t" + "text: " + statusText + "\n" 
                    + "\t" + "timestamp: " + statusTimestampCreation.toString() + "\n"
                    + "\t" + "favourite count: " + statusFavouriteCount + "\n";
            
            if(statusGeoLocation!=null) {
                res += "\t" + "geolocation: " + statusGeoLocation.getLatitude()
                        + ";" + statusGeoLocation.getLongitude() + "\n"; 
            }
            
            res += "\t" + "language: " + statusLanguage + "\n"; 
            
            if(statusPlace!=null) {
                res += "\t" + "place: " + statusPlace.getCountryCode() + "\n" ;
            }
            
            res += "\t" + "reply/retweet: " + this.returnStringReplyOrRetweet();
            
            return res;
    }

    public String getTimestampAsString() {
		return dateFormat.format(Date.from(statusTimestampCreation));
	} 
    
    /**
     * @return the statusId
     */
    public long getStatusId() {
        return statusId;
    }

    /**
     * @return the user
     * This can be null if the instance is from User.getStatus().
     * But we get user from status.getUser() so null can be an error.
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the statusText
     */
    public String getStatusText() {
        return statusText;
    }

    /**
     * @return the statusTimestampCreation
     */
    public Instant getStatusTimestampCreation() {
        return statusTimestampCreation;
    }

    /**
     * @return the statusFavouriteCount
     */
    public int getStatusFavouriteCount() {
        return statusFavouriteCount;
    }

    /**
     * @return the statusGeoLocation
     * can be null
     */
    public GeoLocation getStatusGeoLocation() {
        return statusGeoLocation;
    }

    /**
     * @return the statusLanguage
     */
    public String getStatusLanguage() {
        return statusLanguage;
    }

    /**
     * @return the statusPlace
     */
    public Place getStatusPlace() {
        return statusPlace;
    }

    /**
     * @return the statusRetweetCount
     */
    public int getStatusRetweetCount() {
        return statusRetweetCount;
    }

    /**
     * @return the isRetweet
     */
    public boolean getIsRetweet() {
        return isRetweet;
    }

    /**
     * @return the isReply
     */
    public long getIsReply() {
        return isReply;
    }

    /**
     * @param statusText the statusText to set
     */
    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

}
