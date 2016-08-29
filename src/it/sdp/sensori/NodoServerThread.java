package it.sdp.sensori;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;

public class NodoServerThread extends Thread {

	private Socket connectionSocket = null;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;

    public NodoServerThread(Socket s) {
        connectionSocket = s;
        
        try{
            inFromClient = new BufferedReader(
                           new InputStreamReader(connectionSocket.getInputStream()));
            outToClient =  new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // NODO SERVER THREAD
	public void run() {
		
		try {
			String request = inFromClient.readLine();
			if (Debug.NodoServerMessaggi) System.out.println("NodoThread "+ Nodo.getNode().getType() + " legge: " + request);
			
			// RICHIESTA MISURAZIONI			
			if (request.equals("request")){ 
				List<Misurazione> ms = Nodo.getNode().LetturaBuffer();
				if (Debug.NodoServerMisurazioniInfo) System.out.println("NodoThread "+ Nodo.getNode().getType() + " lette misurazioni");
				outToClient.writeBytes(ms.size() + "\n");
				if (Debug.NodoServerMisurazioniInfo) System.out.println("NodoThread "+ Nodo.getNode().getType() + " size: "+ms.size());
				for (Misurazione m : ms) {
					outToClient.writeBytes(m.getType()
							+ " " + m.getValue()
							+ " " +m.getTimestamp() + "\n");
					if (Debug.NodoServerMisurazioni) System.out.println("Nodo "+ Nodo.getNode().getType() + " misura: " + m.getValue()
							+ " time: " +m.getTimestamp());
				}
				Nodo.getNode().updateBatteria(Nodo.consumoTrasmissione);
			}
			
			// ELEZIONE
			else if (request.startsWith("election")){ 
				if (Nodo.getNode().getElectionStatus() == ElectionStatus.ELECTION)
					Nodo.getNode().EndElection(request);
				else
					new Thread (new ElectionThread(request.substring(9))).start();
			}
			
			// COORDINATOR
			else if (request.startsWith("coordinator")){ 
				if (Debug.NodoServerMessaggi) System.out.println("Nodo "+ Nodo.getNode().getType() + " riceve coordinator!");
				if (Nodo.getNode().getElectionStatus() == ElectionStatus.COORDINATOR){
					Nodo.getNode().EndCoordinatorStatus();
				} else {
					Nodo.getNode().updateCoordinator(request);
					new Thread (new CoordinatorThread(request)).start();
				}
			}
			
			// CLOSE SOCKET
			connectionSocket.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
