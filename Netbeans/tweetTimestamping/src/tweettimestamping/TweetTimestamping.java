/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweettimestamping;

import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class TweetTimestamping {

    
    public static void main(String[] args) {
        
        ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
        exec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                //folder creation
                String csvFolder = "csv";
                File csvDir = new File(csvFolder);
                if(!csvDir.exists()) {
                    csvDir.mkdirs();
                }
                
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date todayDate = new Date();
                
                String fileName = dateFormat.format(todayDate) + ".csv";
                
                File checkExistance = new File(csvFolder + "//" + fileName);
                if(!checkExistance.exists()) {
                    System.out.println("new day");
                    /*
                    The file doesn't exist yet, this means that i need to 
                    eliminate files older than one week
                    */
                    File [] files = csvDir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".csv");
                        }
                    });
                    
                    for(File file: files) {
                        System.out.print("file: " + file.getName());
                        //delete extension
                        String dateOfFile = file.getName().substring(0, file.getName().lastIndexOf("."));
                        try {
                            Date fileDate = dateFormat.parse(dateOfFile);
                            if( TimeUnit.MILLISECONDS.toDays(todayDate.getTime() - fileDate.getTime()) > 7 ) {
                                System.out.print("\tdeleted");
                                file.delete();
                            }
                            System.out.println("");
                        } catch (ParseException ex) {
                            Logger.getLogger(TweetTimestamping.class.getName()).log(Level.SEVERE, null, ex);
                            return;
                        }
                    }
                }
                
                CSVWriter writer;
                try {
                    writer = new CSVWriter(new FileWriter(csvFolder + "//" + fileName, true), ';');
                } catch (IOException ex) {
                    System.out.println("problem with csv");
                    return;
                }
        
                ConfigurationBuilder cb = new ConfigurationBuilder();
                cb.setDebugEnabled(true)
                    .setOAuthConsumerKey("_consumer_key_")
                    .setOAuthConsumerSecret("consumer_secret")
                    .setOAuthAccessToken("access_token")
                    .setOAuthAccessTokenSecret("access_token_secret");

                // The factory instance is re-useable and thread safe.
                Twitter twitter = new TwitterFactory(cb.build()).getInstance();
                Query query = new Query("non");
                query.setLang("it");
                query.setCount(1);
                QueryResult result;
                try {
                    result = twitter.search(query);
                } catch (TwitterException ex) {
                    System.out.println("error with query");
                    return;
                }
                for (Status status : result.getTweets()) {
                    String[] tmp = new String[] {status.getCreatedAt().toInstant().toString(), 
                        Long.toString(status.getId()),
                        status.getCreatedAt().toInstant().atZone(ZoneId.of("Europe/Rome")).toString()};
                    System.out.println(tmp[0] + "\t" + tmp[1] + "\t" + tmp[2]);
                    writer.writeNext(tmp);
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        System.out.println("Error closing csv");
                    }
                }
            }
        }, 0, 5, TimeUnit.MINUTES);
        
    }
    
}
