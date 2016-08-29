package it.sdp.sensori;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class NodoServer implements Runnable {
	
	private ServerSocket welcomeSocket;
	public NodoServer(ServerSocket nodoSocket){
		welcomeSocket = nodoSocket;
	}

	// NODO P2P SERVER MULTITHREAD
	public void run(){

		while (!Nodo.getNode().isBatteriaEsaurita()){
			try {
				Socket connectionSocket = welcomeSocket.accept();
				NodoServerThread nodoThread = new NodoServerThread(connectionSocket);
				nodoThread.start();
			} catch (SocketException e) {
				if (Debug.NodoStatus)System.out.println("Nodo "+ Nodo.getNode().getType() + " closed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
