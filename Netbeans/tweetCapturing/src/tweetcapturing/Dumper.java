package tweetcapturing;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;


public class Dumper {
    
    File file;
    FileOutputStream fos;
    ObjectOutputStream oos;
    int counter_group;
    String group = "";
    final int MAX_LENGTH = 5;			//It allows to make dumping (I/O) in groups and not for every tweet (??Already implemented in FileWriter??)
    final int MAX_KEYW_IN_FILENAME = 6;		//Limit on number of keywords in the filename
    
    
    public Dumper(String[] keywords) {
        try{
            Files.createDirectories(Paths.get("dumps"));	//create dumps directory
        }catch(IOException ie){
            System.out.println(ie.getMessage());
        }
        System.out.println("Dumper created");	
		
        //Operations for dump filename
        String filename_part = "";
        for(int i = 0; i<keywords.length && i<MAX_KEYW_IN_FILENAME;i++)    //Not plus of 6 keywords in file name to avoid infinite strings
            filename_part += keywords[i] + "_";
			
        Date timestamp = Date.from(Instant.now());      //copied from CloudGenerator creation of uniqueID for time_window
        String id = new SimpleDateFormat("HHmmss").format(timestamp);
        file = new File("dumps","Tweets_K_" + filename_part + id + "_dump.txt");  //Create the dumpFile
        
        try {
            //fw = new FileWriter(file);    Lorenzo B. (Tweet class is serialized so this is useless now) 
            fos = new FileOutputStream(file.getCanonicalPath(),true);
            oos = new ObjectOutputStream(fos);
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
	
    //Add a tweet to a group, if the group becomes full (counter_group == MAX_LENGTH), make the flush
    public void addTweetLine(Tweet tmp){		
        try {
            if(tmp.getStatusId()<0){	//Stop when it finds the poison tweet (-1L,"",Instant.now)    
                /*flushWideString();  Lorenzo B. (Tweet class is serialized so this is useless now)
                fw.close(); */
                oos.close();
            }
            
            //TweetLine format "ID \t TEXT \t TIMESTAMP
            oos.writeObject(tmp);
        }catch(IOException ie){
            	
        }
    }
}
