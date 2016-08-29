package it.sdp.gestore;

import java.io.DataOutputStream;
import java.net.Socket;

public class TecnicoCommunication extends Thread {
	
	private String TecnicoAddress;
	private int TecnicoPort;
	private Socket connectionSocket;
	private DataOutputStream outToServer;
	private String message;
	
	TecnicoCommunication(String address, int porta, String tecnicoMessage){
		TecnicoAddress = address;
		TecnicoPort = porta;
		message = tecnicoMessage;		
	}
	
	// SEND MESSAGE TO TECNICO
	@Override
	public void run() {
		try {
			connectionSocket = new Socket(TecnicoAddress,TecnicoPort);
			outToServer = new DataOutputStream(connectionSocket.getOutputStream());
			outToServer.writeBytes(message);
			connectionSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
        
	}

}
