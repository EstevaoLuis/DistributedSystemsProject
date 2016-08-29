package it.sdp.sensori;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;

public class SinkRequestThread implements Runnable {

	private SinkThread sink;
	private String dest;
	
	public SinkRequestThread(SinkThread sinkNode, String nodeDest){
		sink = sinkNode;
		dest = nodeDest;
	}

	// SINK REQUEST FOR MISURAZIONI TO ALL OTHER NODES
	@Override
	public void run() {
		try{
			NodoType nodo = NodoType.getDefaultInfo(dest);
			Socket nodeSocket = new Socket(nodo.getAddress(), nodo.getPort());
			
	        DataOutputStream outToNode = new DataOutputStream(nodeSocket.getOutputStream());
	        outToNode.writeBytes("request\n");
	        
			BufferedReader fromNode = new BufferedReader(new InputStreamReader(nodeSocket.getInputStream()));
			int numberMisurazioni = Integer.parseInt(fromNode.readLine());
            for (int i = 0; i < numberMisurazioni; i++) {
            	String answer = fromNode.readLine();
	            sink.addMisurazione(answer);
			}
            nodeSocket.close();
            Nodo.getNode().updateBatteria(Nodo.consumoTrasmissione);
		} catch (ConnectException e) {
			// INVIO ERRORE NODO CHIUSO
			if (Debug.SinkRequest) System.out.println("Nodo indisponibile: " + dest);
			Nodo.getNode().UnreachableNode(dest);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
