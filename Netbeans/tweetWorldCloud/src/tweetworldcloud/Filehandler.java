/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tweetworldcloud;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

/**
 *
 * @author luigi
 */
public class Filehandler {
    private Timestamp begin;
    private long duration;
    private int counter = 0;
    private PrintWriter writer;
    private String folder;
    
    public Filehandler(String f, long d, Timestamp b) throws FileNotFoundException{
        begin = b;
        duration = d;
        folder = f;
        
        System.out.println("start file with timestamp " + b.toString());
        writer = new PrintWriter(folder + "/" + Integer.toString(++counter) 
                + "_" + b.toString().replaceAll("\\W+", "") + ".txt");
    }
    
    public void write(String text, Timestamp nts ) throws FileNotFoundException {
        if(nts.getTime() - begin.getTime() > duration) { // 1)
            begin = nts; // 2)
            
            writer.close(); // 3)
            System.out.println("start file with timestamp " + nts.toString());
            writer = new PrintWriter(folder + "/" + Integer.toString(++counter) // 4)
                    + "_" + nts.toString().replaceAll("\\W+", "") + ".txt");
            
        } else {
            writer.print(text); // 5)
            //each tweet is separated by one space character
            writer.print(" ");
        }
    }
    
    public int close() {
        writer.close();
        return counter;
    }
    
}

/*
/////// COMMENTI DI Maurizio ////////////
1) se il lasso di tempo è troppo grande
2) aggiorna il campo begin che contiene il timestam relativo al tweet più vecchio
3) chiude il vecchio file dove scriveva le parole
4) crea un nuovo file dove scrivere le nuove parole, esempio : se apre il secondo file inizierà con "2_"
5) scrive il testo del tweet nel file
*/