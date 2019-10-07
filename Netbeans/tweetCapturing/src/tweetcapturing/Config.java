package tweetcapturing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Class providing access to the application configurations, defined in *.conf files.
 * Any configuration can be accessed by appropriate getter of this class, which returns
 * the corresponding value (of the correct type).
 * 
 * The {@link #store()} method can be used to store any modification made by call to setters
 * permanent to the configuration files.
 * 
 * @author stefano
 *
 */
public class Config {
	private String localConfigFile = "localConfig.conf";

        
	// actual properties
	
        //main property
    	private Properties localConfiguration;
    
	// platform
	private String file;
	private String dbAddress;
	private String dbUser;
	private String dbPassword;
	private String dbSchema;
	private String dbTable;
	
	// capturing
	private WordList keywords;
	private boolean keepReply;
	private boolean keepRetweet;
	private String lng;
	private long duration;
        private boolean offline;
        private boolean offlineDB; //Dario F.
        
            //pastCapturing
        private boolean past;
        private String since;
        private String until;
            //Lorenzo R. Used for pastCapturing interval controls (see Filtering) 
        private String sinceTime;
        private String untilTime;
            //*Lorenzo R.
	
	// filtering (refer to former 'selection' phase)
	private WordList punteggiatura;
	private WordList stopWords;
	private WordList baseline;
	private WordList badWords;
	
	// assessment
	private int minWordLength;
	private int frequencyThreshold;
	private int wordNumber;
	private int cloudWidth;
	private int cloudHeight;
	private double angleInclination;
	private int inclinationStep;
	private int refreshTime;
        
	/**
	 * Easily handle the conversion between space-separated list of words and its
	 * ArrayList<String> representation.
	 * 
	 * @author stefano
	 *
	 */
        
	@SuppressWarnings("serial")
	private class WordList extends ArrayList<String> {
		
		private String pattern = "\\s+";
		
		public WordList(String list) {
			if(list != null) {
				addAll(Arrays.asList(list.split(pattern)));
			}
		}
		
		@Override
		public String toString() {
			StringBuilder ret = new StringBuilder();
			for(String s : this) {
				ret.append(s + " ");
			}
			ret.deleteCharAt(ret.length()-1);
			return ret.toString();
		}
	}

	public Config(String configFile) throws IOException {
		InputStream input = null;
	
		// load configurations from localConfig
		localConfiguration=  new Properties();
                
                
		input = new FileInputStream(new File(configFile));
                localConfiguration.load(input);
		
                //dump file
		file = localConfiguration.getProperty("file", "test.txt");
		
                //db parameters
                dbAddress = localConfiguration.getProperty("dbAddress", "localhost");
		dbUser = localConfiguration.getProperty("dbUser", "root");
		dbPassword = localConfiguration.getProperty("dbPassword", "");
		dbSchema = localConfiguration.getProperty("dbSchema", "test");
		dbTable = localConfiguration.getProperty("dbTable", "time_series");
		
		
		// capturing options
		keywords = new WordList(localConfiguration.getProperty("keywords", ""));
		keepReply = Boolean.parseBoolean(localConfiguration.getProperty("keepReply", "false"));
		keepRetweet = Boolean.parseBoolean(localConfiguration.getProperty("keepRetweet", "false"));
		lng = localConfiguration.getProperty("lng", "it");
		duration = Long.parseLong(localConfiguration.getProperty("duration", "60000"));
                offline = Boolean.parseBoolean(localConfiguration.getProperty("offline", "true"));
                offlineDB = Boolean.parseBoolean(localConfiguration.getProperty("offlineDB", "true"));
		
                // filtering words list
		punteggiatura = new WordList(localConfiguration.getProperty("punteggiatura", ""));
		stopWords = new WordList(localConfiguration.getProperty("stopWords", ""));
		baseline = new WordList(localConfiguration.getProperty("baseline", ""));
		badWords = new WordList(localConfiguration.getProperty("badWords", ""));
		
		// wordcloud configuration
		minWordLength = Integer.parseInt(localConfiguration.getProperty("minWordLength", "4"));
		frequencyThreshold = Integer.parseInt(localConfiguration.getProperty("frequencyThreshold", "15"));
		wordNumber = Integer.parseInt(localConfiguration.getProperty("wordNumber", "20"));
		cloudWidth = Integer.parseInt(localConfiguration.getProperty("cloudWidth", "600"));
		cloudHeight = Integer.parseInt(localConfiguration.getProperty("cloudHeight", "200"));
		angleInclination = Double.parseDouble(localConfiguration.getProperty("angleInclination", "0"));
		inclinationStep = Integer.parseInt(localConfiguration.getProperty("inclinationStep", "0"));
		refreshTime = Integer.parseInt(localConfiguration.getProperty("refreshTime", "60000"));
                
		// pastCapturing configuration 
		since=localConfiguration.getProperty("since","");
		until=localConfiguration.getProperty("until","");
		sinceTime=localConfiguration.getProperty("sinceTime","");
		untilTime=localConfiguration.getProperty("untilTime","");

		past=Boolean.parseBoolean(localConfiguration.getProperty("past","false"));
                		
                input.close(); 

	}

	private void store(String filename, Properties toSave) {
		try (OutputStream output = new FileOutputStream(filename)){
			toSave.store(output, filename);
			output.flush();
		} catch (IOException e) {
			//TOIMPLEMENT
		}
	}
        
        /*inizio Lorenzo B*/
        public void storeByFileName(String filename) {
		try (OutputStream output = new FileOutputStream(filename)){
			localConfiguration.store(output, filename);
			output.flush();
		} catch (IOException e) {
			//TOIMPLEMENT
		}
	}
        /*fine Lorenzo B*/
        
	
	public void store() {
		// store configuration file
		store(localConfigFile, localConfiguration);
	}

	public boolean isOffline() {  
		return offline;
	}
        
        public boolean isOfflineDB() {  //Dario F.
                return offlineDB;
        }

	public String getFile() {
		return file;
	}

	public String getDbAddress() {
		return dbAddress;
	}

	public String getDbUser() {
		return dbUser;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public String getDbSchema() {
		return dbSchema;
	}

	public String getDbTable() {
		return dbTable;
	}

	public ArrayList<String> getKeywords() {
		return keywords;
	}

	public boolean isKeepReply() {
		return keepReply;
	}

	public boolean isKeepRetweet() {
		return keepRetweet;
	}

	public String getLng() {
		return lng;
	}

	public ArrayList<String> getPunteggiatura() {
		return punteggiatura;
	}

	public ArrayList<String> getStopWords() {
		return stopWords;
	}

	public ArrayList<String> getBaseline() {
		return baseline;
	}

	public ArrayList<String> getBadWords() {
		return badWords;
	}

	public String getPlatform() {
		return localConfigFile;
	}

	public int getMinWordLength() {
		return minWordLength;
	}

	public int getFrequencyThreshold() {
		return frequencyThreshold;
	}

	public int getWordNumber() {
		return wordNumber;
	}

	public int getCloudWidth() {
		return cloudWidth;
	}

	public int getCloudHeight() {
		return cloudHeight;
	}

	public double getAngleInclination() {
		return angleInclination;
	}

	public int getInclinationStep() {
		return inclinationStep;
	}

        public long getDuration() {
		return duration;
	}
        
	public void setOffline(boolean offline) {
		this.offline = offline;
		localConfiguration.setProperty("offline", String.valueOf(offline));
	}
        
        public void setOfflineDB(boolean offlineDB) {  //Dario F.
		this.offlineDB = offlineDB;
		localConfiguration.setProperty("offlineDB", String.valueOf(offlineDB));
	}

	public void setFile(String file) {
		this.file = file;
		localConfiguration.setProperty("file", file);
	}

	public void setDbAddress(String dbAddress) {
		this.dbAddress = dbAddress;
		localConfiguration.setProperty("dbAddress", dbAddress);
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
		localConfiguration.setProperty("dbUser", dbUser);
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
		localConfiguration.setProperty("dbPassword", dbPassword);
	}

	public void setDbSchema(String dbSchema) {
		this.dbSchema = dbSchema;
		localConfiguration.setProperty("dbSchema", dbSchema);
	}

	public void setDbTable(String dbTable) {
		this.dbTable = dbTable;
		localConfiguration.setProperty("dbTable", dbTable);
	}

	public void setKeywords(String keywords) {
		this.keywords = new WordList(keywords);
		localConfiguration.setProperty("keywords", this.keywords.toString());
	}

	public void setKeepReply(boolean keepReply) {
		this.keepReply = keepReply;
		localConfiguration.setProperty("keepReply", String.valueOf(keepReply));
	}

	public void setKeepRetweet(boolean keepRetweet) {
		this.keepRetweet = keepRetweet;
		localConfiguration.setProperty("keepRetweet", String.valueOf(keepRetweet));
	}

	public void setLng(String lng) {
		this.lng = lng;
		localConfiguration.setProperty("lng", lng);
	}

	public void setDuration(long duration) {
		this.duration = duration;
		localConfiguration.setProperty("duration", String.valueOf(duration));
	}

	public void setPunteggiatura(String punteggiatura) {
		this.punteggiatura = new WordList(punteggiatura);
		localConfiguration.setProperty("punteggiatura", this.punteggiatura.toString());
	}

	public void setStopWords(String stopWords) {
		this.stopWords = new WordList(stopWords);
		localConfiguration.setProperty("stopWords", this.stopWords.toString());
	}

	public void setBaseline(String baseline) {
		this.baseline = new WordList(baseline);
		localConfiguration.setProperty("baseline", this.baseline.toString());
	}

	public void setBadWords(String badWords) {
		this.badWords = new WordList(badWords);
		localConfiguration.setProperty("badWords", this.badWords.toString());
	}

	public void setMinWordLength(int minWordLength) {
		this.minWordLength = minWordLength;
		localConfiguration.setProperty("minWordLength", String.valueOf(minWordLength));
	}

	public void setFrequencyThreshold(int frequencyThreshold) {
		this.frequencyThreshold = frequencyThreshold;
		localConfiguration.setProperty("frequencyThreshold", String.valueOf(frequencyThreshold));
	}

	public void setWordNumber(int wordNumber) {
		this.wordNumber = wordNumber;
		localConfiguration.setProperty("wordNumber", String.valueOf(wordNumber));
	}

	public void setCloudWidth(int cloudWidth) {
		this.cloudWidth = cloudWidth;
		localConfiguration.setProperty("cloudWidth", String.valueOf(cloudWidth));
	}

	public void setCloudHeight(int cloudHeight) {
		this.cloudHeight = cloudHeight;
		localConfiguration.setProperty("cloudHeight", String.valueOf(cloudHeight));
	}

	public void setAngleInclination(double angleInclination) {
		this.angleInclination = angleInclination;
		localConfiguration.setProperty("angleInclination", String.valueOf(angleInclination));
	}

	public void setInclinationStep(int inclinationStep) {
		this.inclinationStep = inclinationStep;
		localConfiguration.setProperty("inclinationStep", String.valueOf(inclinationStep));
	}

	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
		localConfiguration.setProperty("refreshTime", String.valueOf(refreshTime));
	}

	public int getRefreshTime() {
		return refreshTime;
	}
        
        public void setSince(String since){
                this.since=since;
                localConfiguration.setProperty("since",since);
        }
        
        public String getSince(){
                return since;
        }
        
        public void setUntil(String until){
                this.until=until;
                localConfiguration.setProperty("until",until);
        }
        
        public String getUntil(){
                return until;
        }
		
        //Lorenzo R. Functions for since and until hour (hour,minutes,seconds)
        public void setSinceTime(String since){
                this.sinceTime=since;
                localConfiguration.setProperty("sinceTime",sinceTime);
        }
        
        public String getSinceTime(){
                return sinceTime;
        }
        
        public void setUntilTime(String untilHour){
                this.untilTime=untilHour;
                localConfiguration.setProperty("untilTime",untilHour);
        }
        
        public String getUntilTime(){
                return untilTime;
        }
        //*Lorenzo R.
        
        public void setPast(boolean past){
                this.past = past;
                localConfiguration.setProperty("past", String.valueOf(past)); 
        }
        
        public boolean getPast(){
                return past;
        }
}
