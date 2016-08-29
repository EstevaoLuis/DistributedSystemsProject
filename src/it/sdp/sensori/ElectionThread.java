package it.sdp.sensori;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ElectionThread implements Runnable {

	private String candidates;
	
	public ElectionThread(String othercandidates) throws Exception{
		candidates = othercandidates;
		if (!candidates.equals("")) candidates = candidates + " ";
	}

	// SEND AN ELECTION MESSAGE TO THE NEXT NODE OF THE RING NET
	// ELECTION MESSAGE FORMAT: election [nodeId batteryLevel]
	@Override
	public void run() {
		if (Debug.Election) System.out.println("Nodo " + Nodo.getNode().getType() + " Election ");
		if (Debug.ElectionSleep)
			try { Thread.sleep(6000); } catch (InterruptedException e1) { e1.printStackTrace();	}

		String message = "election " + candidates +
				Nodo.getNode().getType() + " " + Nodo.getNode().getBatteria() + "\n";
		
		NodoType nextNode = getNextNode(Nodo.getNode().getType());
		boolean okay;
		do {
			okay = true;
			try {		
				
				Socket nodeSocket = new Socket(nextNode.getAddress(), nextNode.getPort());
		        DataOutputStream outToNode = new DataOutputStream(nodeSocket.getOutputStream());
		        outToNode.writeBytes(message);
		        
		        if (Debug.Election) System.out.println("Election: nodo " + Nodo.getNode().getType() + " send request to " + nextNode.getType());
	
	            nodeSocket.close();
	            Nodo.getNode().updateBatteria(Nodo.consumoTrasmissione);
	
			} catch (ConnectException e) {
				okay = false;
				if (Debug.Election) System.out.println("Election Thread no connection to " + nextNode.getType());
				if (Debug.ElectionSleep)
					try { Thread.sleep(6000); } catch (InterruptedException e1) { e1.printStackTrace();	}
				String unreachableNode = nextNode.getType();
				nextNode = getNextNode(unreachableNode);
				Nodo.getNode().UnreachableNode(unreachableNode);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} while (!okay);
	}
	
	// NEXT NODE OF THE RING NET
	private NodoType getNextNode(String nodeType){
		return NetManager.getNet().nextNode(nodeType);
	}

}
