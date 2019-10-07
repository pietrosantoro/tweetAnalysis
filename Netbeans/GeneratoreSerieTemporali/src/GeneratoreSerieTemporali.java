
import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.util.Scanner;
import java.util.logging.*;

/**
 *
 * @author Maurizio Pulizzi
 */
public class GeneratoreSerieTemporali {

    //********** INIZIO PARAMETRI DI CONFIGURAZIONE **********
    
    private static final String INIZIO ="2018-09-19T00:00:00.00Z";
    private static final String FINE = "2018-09-20T00:00:00.00Z";
    private static final long PASSO = 600000;//10 m
    private static long AMPIEZZA_INTERVALLO = 18000000; //5h
    private static int LIMITE_TERMINI=50;
    
    private static final String DB_ADDR="localhost:3306/twitter_test";
    private static final String DB_TAB="testfiltered";
    private static final String USERNAME="root";
    private static final String PASSWD="";
    //********** FINE PARAMETRI DI CONFIGURAZIONE **********
    
    private static Connection connessioneArchivioCatture;
    private static String[] termini;
    private static int[] freqTermini;
    private static int[] freqTermInterval;
  
    static{
        try{
            connessioneArchivioCatture= DriverManager.getConnection("jdbc:mysql://"+DB_ADDR,USERNAME,PASSWD); //connessione al db
        } catch (SQLException ex) {
            Logger.getLogger(GeneratoreSerieTemporali.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
  
    public static void main(String[] args) {
        
        if(AMPIEZZA_INTERVALLO<0) AMPIEZZA_INTERVALLO = Long.MAX_VALUE;
        
        File freqDir = new File("freqFolder");
        File [] fileDisordinati = freqDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });
        int numIntervalli = fileDisordinati.length;
        File [] files = new File[numIntervalli];
        for(int i=0; i<numIntervalli; i++){
            int pos=Integer.parseInt(fileDisordinati[i].getName().split("_")[0])-1;
            files[pos]=fileDisordinati[i];
        }     
         for(int i=0; i<numIntervalli; i++){
             System.out.println(files[i]);
         }
        //obbligo l'intervallo scelto per generare la matrice ad essere multiplo del passo
        //if((AMPIEZZA_INTERVALLO%PASSO)!=0){
          //  AMPIEZZA_INTERVALLO+=(AMPIEZZA_INTERVALLO%PASSO);
        //}
        
        termini=new String[LIMITE_TERMINI];
        freqTermini=new int[LIMITE_TERMINI]; //si modificherà in modo tale che contenga PER OGNI INTERVALLO TEMPORALE la frequenza dei termini
        freqTermInterval=new int[LIMITE_TERMINI];
        
        Instant istanteIniziale=Instant.parse(INIZIO).minusMillis(7200000);  // istante dal quale si cominicano ad analizzare i tweet dal db, si diminuisce di due ore prechè quando si converte in timestamp per il db vengono aggiunte 2 ore
        Instant istanteFinale=Instant.parse(FINE).minusMillis(7200000);      // istante temporale oltre il quale i tweet del db non vengono più presi in considerazione
        
        System.out.println(Timestamp.from(istanteIniziale) + " " + Timestamp.from(istanteFinale) );
        Instant istanteCorrente;
        Instant istanteSuccessivo;
        Instant intervalloCorrente;
        Instant intervalloSuccessivo;
       
        try(BufferedWriter bwTimesT=new BufferedWriter(new FileWriter("intervalli_temporali.txt"));
            Statement statement=connessioneArchivioCatture.createStatement();
            ResultSet rs=statement.executeQuery("SELECT text,timestamp FROM "+DB_TAB+" WHERE timestamp BETWEEN '"+ Timestamp.from(istanteIniziale) + 
                    "' AND '"+ Timestamp.from(istanteFinale)+"' ORDER BY timestamp;");
        ){
            Scanner sc=new Scanner(files[0]);  
            int numTermini = 0;
           // bw.write("t = [");
            for(int i=0;i<LIMITE_TERMINI && sc.hasNext();++i){
                termini[i]=sc.next();
               // bw.write("\""+termini[i]+"\" ");
                System.out.println(termini[i]);
                sc.nextLine();
                ++numTermini;
            } //adesso il vettore termini contiene tutti i termini
           // bw.write("];");
           // bw.newLine();
           sc.close();
           
            if(numTermini < LIMITE_TERMINI) LIMITE_TERMINI=numTermini;
                        
            rs.next();
            istanteCorrente=rs.getTimestamp("timestamp").toInstant(); 
            intervalloCorrente=istanteCorrente;
            intervalloSuccessivo=intervalloCorrente.plusMillis(AMPIEZZA_INTERVALLO);
            istanteSuccessivo=istanteCorrente.plusMillis(PASSO);
            
            //le seguenti due righe servono perchè l'oggetto Istant sfasa i timestamp di 2 ore
            Instant daScrivere= intervalloCorrente.plusMillis(7200000);
            Instant daScrivere1= intervalloSuccessivo.plusMillis(7200000);
            
            bwTimesT.write("Intervallo 1: "+daScrivere+" "+daScrivere1);
            bwTimesT.newLine();
            Instant is;
            String text;
            
            int numeroMatrice=1;
            //BufferedWriter bwWordF;
            BufferedWriter bw=new BufferedWriter(new FileWriter("serietemporali_"+numeroMatrice+".m"));
            bw.write("S = [");
            bw.newLine();
            do{
                is=rs.getTimestamp("timestamp").toInstant(); 
                
                if(is.compareTo(istanteSuccessivo)>=0 || is.compareTo(intervalloSuccessivo)>=0){
                    //aggiorna istanti
                    istanteCorrente=(istanteSuccessivo.compareTo(intervalloSuccessivo)>=0)? intervalloSuccessivo: istanteSuccessivo;
                    istanteSuccessivo=istanteCorrente.plusMillis(PASSO);
                    
                    //scrivi riga e azzera arayfreqTermini
                    for(int j=0;j<LIMITE_TERMINI;j++){
                        bw.write(freqTermini[j]+" ");
                        System.out.println(termini[j]+": "+ freqTermini[j]);
                        freqTermini[j]=0;
                        if(j==(LIMITE_TERMINI -1)){
                             bw.write(";");
                             bw.newLine();
                             System.out.println();
                        }
                    }
                }
                if(is.compareTo(intervalloSuccessivo)>=0){
                    
                    //bwWordF = new BufferedWriter(new FileWriter("word_frequencies_list"+numeroMatrice+".txt"));
                   /* for(int j=0;j<LIMITE_TERMINI;j++){
                        bwWordF.write(termini[j]+" = "+freqTermInterval[j]);
                        bwWordF.newLine();
                        freqTermInterval[j]=0;
                    }
                    bwWordF.close();*/
                    
                    bw.write("];");
                    bw.close();
                    ++numeroMatrice;
                    
                    if(numeroMatrice>numIntervalli) return;
                    
                    bw=new BufferedWriter(new FileWriter("serietemporali_"+numeroMatrice+".m")); ;
                    bw.write("S = [");
                    bw.newLine();
                    intervalloCorrente=intervalloSuccessivo;
                    intervalloSuccessivo=intervalloCorrente.plusMillis(AMPIEZZA_INTERVALLO);
                    daScrivere= intervalloCorrente.plusMillis(7200000);
                    daScrivere1= intervalloSuccessivo.plusMillis(7200000);
                    bwTimesT.write("Intervallo " +numeroMatrice+": "+daScrivere+" "+daScrivere1);
                    bwTimesT.newLine();
                    
                    //cambio lista
                    
                    sc=new Scanner(files[(numeroMatrice-1)]);           
                    numTermini = 0;
                   
                    for(int i=0;i<LIMITE_TERMINI && sc.hasNext();++i){
                        termini[i]=sc.next();
                        System.out.println(termini[i]);
                        sc.nextLine();
                        ++numTermini;
                    }
                    sc.close();
                }
                
                text=rs.getString("text");
                String[] parole=text.split(" ");
                for(int i=0; i<LIMITE_TERMINI; i++){
                    
                    
                    for(int j=0; j<parole.length; j++){
                       if(parole[j].equals(termini[i])){
                            ++freqTermini[i]; 
                            ++freqTermInterval[i];
                        }
                    }
                    /*if(text.matches(".*"+termini[i]+".*")){
                       freqTermini[i]++;
                    }*/
                }
                
            }while(rs.next());
            
            bw.write("];");
            bw.close();
            sc.close();
        } catch (IOException | SQLException ex) {
           Logger.getLogger(GeneratoreSerieTemporali.class.getName()).log(Level.SEVERE, null, ex);
        }
  
    }
    
}


/*
    Questo file arte da una wordlist passata e genera la matrice delle serie temporali,
    in questa matrice le colonne rappresentano i termini e le righe gli intervalli temporali

*/
