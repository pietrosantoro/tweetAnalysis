package wordcloudsgenerator;

import java.io.*;
import java.util.*;
import com.kennycason.kumo.*;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.image.AngleGenerator;
import com.kennycason.kumo.palette.ColorPalette;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.logging.*;
/**
 *
 * @author Pietro Santoro
 */
public class WordCloudsGenerator {
    
    //********** INIZIO PARAMETRI DI CONFIGURAZIONE **********
    
    //word list principale
     private static final String FILE_WORD_FREQ ="1_lista_cluster.txt";
    //file raggruppamenti
     private static final String FILE_RESULT_CLUSTERING ="resultClustering.txt";
    //percorso file numero cluster
    private static final String FILE_NUM_CLUSTER ="numCluster.txt";
    //percorso file coordinate centroidi dei cluster
    private static final String FILE_COORDINATE_CENTROIDI ="coordinateCentroidi.txt";
    //cartella che conterà le word list dei vari cluster
    private static final String freqFolder = "freqWordFolderClustered";
    //cartella che conterà le word list ei vari cluster
    private static final String wordCloudFolder = "cloudFolder";
    
    //********** FINE PARAMETRI DI CONFIGURAZIONE **********
    
    private static List<String> termini;
    private static List<Integer> freqTermini;
    private static int numCluster;
    private static String[] resultClustering;
    private static String[] coordinateCentroidi_temp;
    private static Double[][] coordinateCentroidi;                  //matrice contenente per ogni cluster le sue coordinate
    private static final int singleWordCloudDimension = 600;              //dimensione di base di tutte le wordcloud
    private static final int spaceDimension = 2;                          //dimensione dello spazio dei centroidi
    private static final int moltiplicatoreDistanza = 1000;               //moltiplicatore che serve ad aumentare o diminuire la distanza tra i cluster
    private static final int moltiplicatoreColore = 3000;                 //moltiplicatore che serve ad aumentare o diminuire la differenza di colore tra i cluster
    private static final int basecolor1 = 0x4055F1;                       //colore del primo cluster
    
    
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
    
    
    public static void concatena()throws IOException{
        double min_height = coordinateCentroidi[0][1]*moltiplicatoreDistanza;
        double min_weight = coordinateCentroidi[0][0]*moltiplicatoreDistanza;
        double max_height = coordinateCentroidi[0][1]*moltiplicatoreDistanza;
        double max_weight = coordinateCentroidi[0][0]*moltiplicatoreDistanza;
        for(int i = 1;i<numCluster;i++){
            double weight = coordinateCentroidi[i][0]*moltiplicatoreDistanza;
            double height = coordinateCentroidi[i][1]*moltiplicatoreDistanza;
            if(weight< min_weight)
                min_weight = weight;
            if(weight > max_weight)
                max_weight = weight;
            if(height< min_height)
                min_height = height;
            if(height > max_height)
                max_height = height;
        }
        int dimension_weight = (int)(max_weight - min_weight) + (singleWordCloudDimension);         //cerco di creare un immagine finale il più compatta possibile in base alle coordinate x e y dei centroidi dei cluster
        int dimension_height = (int)(max_height - min_height) + (singleWordCloudDimension);
        int centro_weight = dimension_weight/2;
        int centro_height = dimension_height/2;
        BufferedImage[] img = new BufferedImage[numCluster];
        BufferedImage concatImage = new BufferedImage(dimension_weight + (singleWordCloudDimension), dimension_height + (singleWordCloudDimension), BufferedImage.TYPE_INT_RGB);    //immagine finale che conterrà tutti i cluster concatenati
        Graphics2D g = concatImage.createGraphics();
        g.setBackground(Color.WHITE);
        g.fillRect(0, 0, dimension_weight + (singleWordCloudDimension), dimension_height + (singleWordCloudDimension));
        for(int i=0;i<numCluster;i++){
            File wordCloudImg = new File(wordCloudFolder + "//wordCloud_"+ (i+1) + ".png");
            img[i] = ImageIO.read(wordCloudImg);
            g.drawImage(img[i], (int)(centro_weight+(coordinateCentroidi[i][0])* moltiplicatoreDistanza),(int)(centro_height + (coordinateCentroidi[i][1])* moltiplicatoreDistanza), null);
           // System.out.println((int)(centro_weight+(coordinateCentroidi[i][0])* moltiplicatoreDistanza));
            //System.out.println((int)(centro_height+(coordinateCentroidi[i][1])* moltiplicatoreDistanza));
          //  System.out.println();
        }
        g.dispose();
        ImageIO.write(concatImage, "png", new File(wordCloudFolder + "//output.png"));        
    }
    
    public static void main(String[] args) throws IOException{
        
        //folder declaration
        File freqDir = new File(freqFolder);
        if(!freqDir.exists()) {
            freqDir.mkdirs();
        }
        File wordCloudDir = new File(wordCloudFolder);
        if(!wordCloudDir.exists()) {
            wordCloudDir.mkdirs();
        }
        // cancella tutti i file vecchi
        purgeFolder(freqDir, "txt");
        purgeFolder(wordCloudDir, "png");
        
        termini=new ArrayList<String>();
        freqTermini=new ArrayList<Integer>();
         try{
             //inizializzo termini e freqTermini
             Scanner sc=new Scanner(new File(FILE_WORD_FREQ));
             for(int i=0; sc.hasNext(); ++i){
                termini.add(sc.next());
                sc.next();
                freqTermini.add(Integer.parseInt(sc.next()));
             }
             sc.close();
             
             //inizializzo numCluster
             sc=new Scanner(new File(FILE_NUM_CLUSTER));
             numCluster=Integer.parseInt(sc.next());
             sc.close();
             
             //inizializzo resultClustering
             sc=new Scanner(new File(FILE_RESULT_CLUSTERING));
             resultClustering= sc.next().split(",");
             sc.close();
             
             //inizializzo coorinateCentroidi
             coordinateCentroidi=new Double[numCluster][spaceDimension];                             //sarà una matrice con "numCluster" righe e 2 colonne perche siamo in 2 dimensioni
             sc = new Scanner(new File(FILE_COORDINATE_CENTROIDI));
             for(int j = 0; j<numCluster; j++){
                coordinateCentroidi_temp= sc.nextLine().split(",");                                  //questa stringa temporanea contiene ad ogni passo, i termini della riga j-esima (ogni riga ha 2 termini in questo caso)
                for(int i = 0; i<spaceDimension; i++)                                               //in questo caso farà sempre 2 iterazioni perche ogni riga contiene 2 termini essendo in uno spazio in 2 dimensioni
                    coordinateCentroidi[j][i] = Double.parseDouble(coordinateCentroidi_temp[i]);
             }
             sc.close();
             
             final List<List<WordFrequency>> wordFrequencies = new ArrayList<>(); // 1)
             for(int i=0; i<numCluster; i++){
                 wordFrequencies.add(new ArrayList<>());
             }        
             
             //assegno le parole al cluster
             for(int i=0; i<resultClustering.length; i++){
                 WordFrequency w= new WordFrequency(termini.get(i),freqTermini.get(i));
                 int cluster=Integer.parseInt(resultClustering[i])-1;
                 wordFrequencies.get(cluster).add(w);
             }
             for(int i=1; i<=numCluster; i++){
                 PrintWriter printWriter;
                 printWriter = new PrintWriter(freqFolder + "//" + i +"_lista_cluster.txt");
                 for(WordFrequency w: wordFrequencies.get(i-1)) { 
                    printWriter.println(w.getWord() + " = " + w.getFrequency());
                 }
                 printWriter.close();
             }
             
             for(int i=0; i<numCluster; i++){
                final Dimension dimension = new Dimension(singleWordCloudDimension, singleWordCloudDimension);
                final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
                wordCloud.setPadding(2);
                wordCloud.setAngleGenerator(new AngleGenerator(0));
                wordCloud.setBackground(new CircleBackground(200));
                wordCloud.setBackgroundColor(Color.WHITE);
                wordCloud.setColorPalette(new ColorPalette(new Color(basecolor1 + (i * moltiplicatoreColore))));
                wordCloud.setFontScalar(new SqrtFontScalar(20, 50));
                wordCloud.build(wordFrequencies.get(i));
                wordCloud.writeToFile(wordCloudFolder + "//wordCloud_" + (i+1) + ".png");    
             }
             
             concatena();                            //chiamo la funzione che crea l'immagine finale di tutti i cluster concatenati in base alle loro distanze
             
         }catch(FileNotFoundException ex){
             System.err.println("FILE NON TROVATO!");
             ex.printStackTrace();
         }
         

    }
    
}


/*
1)  ogni elemento è una lista di WordFrequncy, in particolare l'i-esimo elemento
    conterrà la lista di WordFrequency relativa all'i-esimo cluster

*/