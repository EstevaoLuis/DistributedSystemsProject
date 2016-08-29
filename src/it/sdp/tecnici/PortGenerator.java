package it.sdp.tecnici;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PortGenerator {
	// THIS METHOD IS USED TO GET THE FIRST FREE DOOR FROM 9200
	public synchronized static int getNextPort(){
		// GET PORT NUMBER
		int port = 9200;
        try {
            if (new File("myPort.txt").exists()){
                BufferedReader br = new BufferedReader(new FileReader(new File("myPort.txt")));
                String s = br.readLine();
                port = Integer.parseInt(s);
                br.close();
            }                
        } catch(Exception e) {
            e.printStackTrace();
        }
        // SAVE NEXT PORT
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("myPort.txt")));
            bw.write(Integer.toString(++port));
            bw.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        // RETURN
        return port;
	}
}
