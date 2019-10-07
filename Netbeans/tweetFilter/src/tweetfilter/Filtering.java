package tweetfilter;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.time.Instant;
import tweetcapturing.Config;
import tweetcapturing.Tweet;

/**
 * Filter the stream of tweets using the configurations specified in filtering.conf.
 * 
 * The words in punctuation, stopWords, badWords and keywords (from capturing.conf) are 
 * removed from the tweet text, along with any link.
 * These words are always removed, even when they are embedded in other words.
 * 
 * When the tweet text contains any of the words in baseline, the entire tweet is 
 * flagged as discarded. This allow to easily remove the tweet from the stream.  
 * 
 * This class implements the {@link Function} interface, so it can be directly used as intermediate operation
 * in a stream.
 * 
 * @author stefano
 *
 */
public class Filtering{

	private Pattern punteggiatura;
	private Pattern stopWords;
	private Pattern baseline;
	private Pattern badWords;
	private Pattern keywords;
        private Pattern username;
	
        private Pattern retweet; //to eliminate the rt string at the beginning of each tweet
        
	//Instants to control tweets timestamps according to the hour (since and until) specified in configuration mode
	private Instant bottom_time;
        private Instant top_time;
	
	
	public Filtering(Config config) {
            // initialize the regexp patterns to apply the filters.
            // the words in punteggiatura and keywords configuration options
            // will be filtered only when found as a whole in the twitter text, while the other ones are filtered
            // even if they are found as part of bigger words

            // always remove links (add link regexp to punteggiatura, so they will be automatically removed when punteggiatura filter is applied) 
            String linkRegExp = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]\\b";
            String patternPunteggiatura = buildAlternativePattern(config.getPunteggiatura(), false);
            if(!patternPunteggiatura.equals("")) 
                patternPunteggiatura += "|";
            patternPunteggiatura += linkRegExp;
            //punteggiatura pattern find every punctuation and links in the tweets
            punteggiatura = Pattern.compile(patternPunteggiatura);
            
            stopWords = Pattern.compile(buildAlternativePattern(config.getStopWords(), true));
            baseline = Pattern.compile(buildAlternativePattern(config.getBaseline(), true));
            badWords = Pattern.compile(buildAlternativePattern(config.getBadWords(), true));
            keywords = Pattern.compile(buildAlternativePattern(config.getKeywords(), true));
            
            username = Pattern.compile("@\\s*(\\w+)");
            
            retweet = Pattern.compile("^rt");
            
            /**
             * if getPast is set to false filter all
             */
            
            if(config.getPast()){
                System.out.println("getPast: true");
                //Need to control if some fields of since/until are not set (Solved in config with default value)
                String until = config.getUntil();	
                String since = config.getSince();
                String untilHour = config.getUntilTime();
                String sinceHour = config.getSinceTime();
                //Timestamps for Instant parsing
                String sinceTimeStamp = since + "T" + sinceHour + ":00.00Z";
                String untilTimeStamp = until + "T" + untilHour + ":00.00Z";

                bottom_time = Instant.parse(sinceTimeStamp); 
                top_time = Instant.parse(untilTimeStamp);
            } else {
                System.out.println("getPast: false");
                bottom_time = null;
                top_time = null;
            }
            //*Lorenzo R.
	}
	
	/**
	 * Build a regexp string that matches any of the words in {@code list}. Passing {@code false}
	 * as {@code singleWord} the words are matched even if they are found as part of bigger words.
	 * Pass true to match the words as a whole.
	 * Note that the strings in {@code list} are assumed to be without spaces.
	 * 
	 * @param list list of words the returned regexp will match
	 * @param singleWord true if the words in list should be matched only as a whole
	 * @return a string that can be used to build a regular expression that will match any of the words in list
	 */
	private String buildAlternativePattern(ArrayList<String> list, boolean singleWord) {
            StringBuilder ret = new StringBuilder();
            for(String p : list) {
                if(!p.equals("")) {
                    ret.append("|");
                    if(singleWord) {
                        ret.append("\\b");
                        ret.append(Pattern.quote(p));
                        ret.append("\\b");
                    } else {
                        ret.append(Pattern.quote(p));
                    }
                }
            }
            if(ret.length() > 0) ret.deleteCharAt(0); // remove first '|'
            return ret.toString();
	}

	
	public String apply(String text, Instant tweet_time) {
            //Tweet timestamp control. Control if it belongs to the pastCapturing interval using previous initialized values
            if(bottom_time != null){	 //Control for pastCapturing selection (config.getPast() implies bottom_time != null)
                //Instant tweet_time = tweet.getStatusTimestampCreation();                    
                if(tweet_time.compareTo(bottom_time) < 0 || tweet_time.compareTo(top_time) > 0){
                    //tweet.setDiscarded();
                    return " ";
                }
            }

            // remove any characters in punteggiatura, stopWords, badWords and keywords
            text = retweet.matcher(text).replaceAll(" ");
            text = username.matcher(text).replaceAll(" ");
            text = punteggiatura.matcher(text).replaceAll(" ");
            text = stopWords.matcher(text).replaceAll(" ");
            text = badWords.matcher(text).replaceAll(" ");
            text = keywords.matcher(text).replaceAll(" ");
            text = baseline.matcher(text).replaceAll(" ");
            
            
            //this return statement collapse all multiple spaces to one space
            text = text.replaceAll("\\s+", " ");
            return text;
	}
}
