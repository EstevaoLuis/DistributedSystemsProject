package it.sdp.sensori;

import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class SinkThread implements Runnable {
	
	private Socket connectionSocket;
	private DataOutputStream outToServer;
	private Sink sink;
	
	// MISURAZIONI RACCOLTE
	private List<String> misurazioni = new ArrayList<String>();
	// MISURAZIONI LOCK
	private Object misurLock = new Object();
	
	public SinkThread(Sink sink){
		this.sink = sink;
	}

	@Override
	public void run() {
		
			try {
				// SERVER CONNECTION SETUP
				connectionSocket = new Socket(NetManager.GestoreAdress,NetManager.GestorePort);
		        outToServer = new DataOutputStream(connectionSocket.getOutputStream());
				// END SERVER CONNECTION SETUP
		        
		        // MISURAZIONI REQUEST
		        List<Thread> threads = new ArrayList<Thread>();
		        for (String node : NetManager.getNet().getAllNodes()) {
					if (!node.equals(Nodo.getNode().getType()))
						threads.add(new Thread(new SinkRequestThread(this, node)));
				}
				for (Thread t: threads) {
					  t.start();
				}
				if (Debug.SinkRequest) System.out.println("Sink request all threads started ");
				for (Thread t : threads) {
					  t.join();
				}
				if (Debug.SinkRequest) System.out.println("Sink request all threads joined ");
				// END MISURE REQUEST
				
				// MISURE DEL SINK
				List<Misurazione> ms = Nodo.getNode().LetturaBuffer();
				for (Misurazione m : ms) {
					addMisurazione (m.getType()
							+ " " + m.getValue()
							+ " " + m.getTimestamp());
					if (Debug.NodoServerMisurazioni) System.out.println("Nodo "+ Nodo.getNode().getType() + " misura: " + m.getValue()
							+ " time: " +m.getTimestamp());
				}
				// FINE MISURE DEL SINK
				
				// INVIO SERVER
				if (Debug.SinkRequest) System.out.println("Sink send info to Server ");
				try {
					outToServer.writeBytes("misure " + sizeMisurazioni() + "\n");
				
					if (Debug.SinkRequest) System.out.println(sizeMisurazioni() + " ");
				
					if (Debug.SinkRequest) System.out.println("Sink send info to Server: size:  " + sizeMisurazioni());
					for (String m : getMisurazioni()) {
						if (Debug.SinkRequest) System.out.println("Sink send to Server: " + m);
							outToServer.writeBytes(m + "\n");
					} 
				} catch (SocketException s) {
					if (Debug.GatewayErrors) System.out.println("Message not sent! Socket Error!");
				}
				// FINE INVIO SERVER
				
				connectionSocket.close();
				Nodo.getNode().updateBatteria(4 * Nodo.consumoTrasmissione);
				sink.unBusy();
			} catch (Exception e) {
				sink.unBusy();
				e.printStackTrace(); // SOCKET EXCEPTION
			}
	}

	// ADD MISURAZIONI
	public void addMisurazione(String misura){
		synchronized (misurLock) {
			misurazioni.add(misura);
		}
	}
	
	// NUMBER OF MISURAZIONI
	public int sizeMisurazioni(){
		synchronized (misurLock) {
			return misurazioni.size();
		}
	}
	
	// GET ALL MISURAZIONI
	public List<String> getMisurazioni(){
		synchronized (misurLock) {
			return misurazioni;
		}
	}
}