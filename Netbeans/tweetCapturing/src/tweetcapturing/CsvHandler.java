package tweetcapturing;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import tweetcapturing.Tweet;


public class CsvHandler {
    private CSVWriter writer;
    private String path;
    final int bufferSize = 100;
    private ArrayList<String[]> list;
    

    public CsvHandler(String keywords) throws IOException {
        Date timestamp = Date.from(Instant.now());
        String id = new SimpleDateFormat("HHmmss").format(timestamp);
        
        list = new ArrayList<String[]>();
        
        path = "Tweets_K_" + keywords + id + "_dump.csv";
    }
    
    public void write(Tweet t) {
        list.add( new String[] {Long.toString(t.getStatusId()),
                    t.getStatusTimestampCreation().toString(),
                    t.getStatusText()});
        
        if(list.size() >= 100) {
            realWrite();
        }
    }
    
    private void realWrite(){
        try {
            writer = new CSVWriter(new FileWriter(path, true), ',');
        } catch (IOException ex) {
            Logger.getLogger(CsvHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        writer.writeAll(list);
        list.clear();
        
        try {
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(CsvHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void close() throws IOException {
        realWrite();
    }
}
