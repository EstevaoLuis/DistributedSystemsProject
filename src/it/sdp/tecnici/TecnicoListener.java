package it.sdp.tecnici;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

public class TecnicoListener implements Runnable {

	private ServerSocket welcomeSocket;
	
	public TecnicoListener(ServerSocket serverSocket) throws Exception {
		welcomeSocket = serverSocket;
	}
	
	// IL TECNICO RIMANE IN ASCOLTO DEI MESSAGGI DEL GESTORE
	@Override
	public void run() {
		try {
			while(true){
				new TecnicoCommunicationHandler(welcomeSocket.accept()).start();				
			}
		} catch (SocketException s){ // SOCKET CLOSED
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
