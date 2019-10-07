/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetworldcloud;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.image.AngleGenerator;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import morfologik.util.FileUtils;
import tweetcapturing.Config;
import tweetcapturing.MySQLBridge;
import tweetcapturing.TweetCapturing;

/**
 *
 * @author luigi
 */
public class TweetWorldCloud {

    static void purgeFolder(File dir, String extension) {
        File [] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith("." + extension);
            }
        });
        
        if(files == null)
            return;
            
        for (File file : files) {
            System.out.println("Delete " + file.getName());
            file.delete();
        }
    }
    
    static String stripExtension(String fileName) {
        if(fileName == null)
            return null;
        
        int pos = fileName.lastIndexOf(".");
        if(pos == -1)
            return fileName;
        
        return fileName.substring(0, pos);
    }

    public static void main(String[] args) {
        /**
         * IF REFRESH TIME IS LESS THAN 0
         * THEN STATISTICS ARE EVALUATED WITH ALL THE DB
         */
        
        Config config;
        try {
            config = new Config("localConfig.conf");
        } catch (IOException ex) {
            Logger.getLogger(TweetCapturing.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        MySQLBridge originalDb; // 4)
        try {
            originalDb = new MySQLBridge(config.getDbAddress(),
                    config.getDbUser(),
                    config.getDbPassword(),
                    config.getDbSchema(),
                    config.getDbTable());
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(TweetWorldCloud.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        //folder declaration
        String txtFolder = "tempTxt"; // 5)
        File txtDir = new File(txtFolder);
        if(!txtDir.exists()) {
            txtDir.mkdirs();
        }
        
        String freqFolder = "freqFolder";
        File freqDir = new File(freqFolder);
        if(!freqDir.exists()) {
            freqDir.mkdirs();
        }
        
        String wordCloudFolder = "cloudFolder";
        File wordCloudDir = new File(wordCloudFolder);
        if(!wordCloudDir.exists()) {
            wordCloudDir.mkdirs();
        }
        
        //delete all old files
        purgeFolder(txtDir, "txt");
        purgeFolder(freqDir, "txt");
        purgeFolder(wordCloudDir, "png");
        
        ResultSet result;
        String min = null;
        Filehandler filehandler;
        int createdTxt = 0;
        
        System.out.println("FETCH DB");
        
        try {
            result = originalDb.retrieveData("SELECT min(timestamp) FROM `" + config.getDbTable() +"`"); // 6)
            while(result.next()) {
                min = result.getString(1);
                System.out.println(min); // 7)
            }
            Timestamp referenceTs = Timestamp.valueOf(min); // 8)
            System.out.println("start at: " + referenceTs.toString());
            
            long windowDuration = (config.getRefreshTime()<0)?Long.MAX_VALUE:config.getRefreshTime(); // 9) 
            
            try {
                filehandler = new Filehandler(txtFolder, windowDuration, referenceTs); // 10)
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TweetWorldCloud.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            result = originalDb.retrieveData("SELECT * FROM `" + config.getDbTable() +"` order by timestamp"); // 11)
            
            while(result.next()) { // 12)
                Timestamp tweetTs = Timestamp.valueOf(result.getString(4)); // 13) 
                
                try {
                    filehandler.write(result.getString(3), Timestamp.valueOf(result.getString(4))); // 14)
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TweetWorldCloud.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
            }
            
            createdTxt = filehandler.close();
        } catch (SQLException ex) {
            Logger.getLogger(TweetWorldCloud.class.getName()).log(Level.SEVERE, null, ex);
        }
        originalDb.closeConnection(); // 15) 
        
        // now i have all txt divided in refreshtime block
        final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setWordFrequenciesToReturn(50);
        frequencyAnalyzer.setMinWordLength(4);
        
        File [] files = txtDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        }); // 16) 
        
        for(File file: files) { // 3)
            final List<WordFrequency> wordFrequencies; // 1)
            try {
                System.out.println("FREQUENIES FOR: " + file.getName());
                wordFrequencies = frequencyAnalyzer.load(file); // 17) 
            } catch (IOException ex) {
                Logger.getLogger(TweetWorldCloud.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            PrintWriter printWriter; // 18) , 2)
            try {
                printWriter = new PrintWriter(freqFolder + "//" + stripExtension(file.getName()) +".txt"); // 19) 
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TweetWorldCloud.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            
            for(WordFrequency w: wordFrequencies) { // 20) 
                printWriter.println(w.getWord() + " = " + w.getFrequency());
            }
            printWriter.close();
                    // 21) 
            System.out.println("CREATE WORD CLOUD FOR: " + file.getName());
            final Dimension dimension = new Dimension(600, 600);
            final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
            wordCloud.setPadding(2);
            wordCloud.setAngleGenerator(new AngleGenerator(0));
            wordCloud.setBackground(new CircleBackground(300));
            wordCloud.setBackgroundColor(Color.WHITE);
            wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1)));
            wordCloud.setFontScalar(new SqrtFontScalar(10, 60));
            wordCloud.build(wordFrequencies);
            wordCloud.writeToFile(wordCloudFolder + "//" + stripExtension(file.getName()) + ".png");
        }
        
        
        
    }
    
}


/*
//////////////// COMMENTI DI MAURIZIO ///////////
1) https://github.com/kennycason/kumo/blob/master/kumo-api/src/main/java/com/kennycason/kumo/WordFrequency.java
    WordFrequency è un oggetto contenente una stringa word e un intero frequency

2) https://www.tutorialspoint.com/java/io/java_io_printwriter.htm

3) per ogni file temporaneo precedentemente creato si crea un file con la lista di termini (e rispettivo conteggio) e una immagine wordcloud

4)  prepara la connessione al db
5)  crea il collegamento con le directory temporanea nella quale scriverà in 
    sequenza tutte le parole che trova nei tweet durante una prima scansione
6)  esegue query
7)  stampa a video il timestamp minimo
8)  inizializza col timestamp minimo
9)  inizializza il lasso di tempo massimo che ci può essere tra il tweet più vecchio e tutti gli altri  
10) lo inizializza con la directory temporanea, lasso di temp sopracitato, 
    e timestamp del tweet "piu' vecchio"
11) carica tutti i tweet in ordine di timestamp
12) scorre tutti i tweet (per scriverli in sequenza nel file temporaneo)
13) prende il timestamp del tweet corrente (ma sembra non usarlo)
14) di base questa funzione scrive il tweet nel file contenuto nella directory tempTxt
15) chiude la connessione col db perchè non serve più, adesso tutti i testi dei 
    tweet sono contenuti nei file dentro la directory tempTxt
16) si crea un array di (puntatori ai) file temporanei precedentemente creati
17) analizza il file temporaneo e setta la lista di WordFrequency
18) tale classe stampa le rappresentazioni formattate degli oggetti in un flusso di output di testo
19) crea il file che verrà riempito con i termini piu frequenti e il rispettivo numero di ocorrenze
20) si scorre la lista di wordfrequency e si scrive nel file appropriato
21) di seguito viene creato il word cloud a partire dalla lista wordFrequencies 
    e viene inserito nella directory appropriata 
*/