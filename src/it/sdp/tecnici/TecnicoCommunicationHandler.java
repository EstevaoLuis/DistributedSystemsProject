package it.sdp.tecnici;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TecnicoCommunicationHandler extends Thread {

	private Socket connectionSocket;
    private BufferedReader inFromClient;
	
	public TecnicoCommunicationHandler(Socket s) {
        connectionSocket = s;
        try{
            inFromClient = new BufferedReader(
            		       new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void run() {
	
		try {
			String request = inFromClient.readLine();
			
			// MESSAGGIO ERRORE
			if (request.startsWith("error")){ 
				System.out.print("Errore Gestore: ");
				System.out.println(request.substring(6));
			}
			
			// MESSAGGIO CLOSE
			else { 
				System.out.println("Close!!!");
			}
			
			connectionSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
}
