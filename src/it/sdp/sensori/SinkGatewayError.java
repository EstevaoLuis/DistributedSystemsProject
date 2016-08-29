package it.sdp.sensori;

import java.io.DataOutputStream;
import java.net.Socket;

public class SinkGatewayError implements Runnable {
	
	private Socket connectionSocket;
	private DataOutputStream outToServer;
	
	private String error;
	
	public SinkGatewayError(String errorDescription){
		error = errorDescription;
	}

	// SINK SENDS ERROR MESSAGE TO GESTORE
	@Override
	public void run() {

		try {
			connectionSocket = new Socket(NetManager.GestoreAdress,NetManager.GestorePort);
	        outToServer = new DataOutputStream(connectionSocket.getOutputStream());
			if (Debug.GatewayErrors) System.out.print("Node " + Nodo.getNode().getType() + " sink sending error: " + error);
			
			outToServer.writeBytes("error " + error);
			
			connectionSocket.close();
			Nodo.getNode().updateBatteria(4 * Nodo.consumoTrasmissione);
		
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
