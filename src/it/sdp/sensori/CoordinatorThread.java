package it.sdp.sensori;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class CoordinatorThread implements Runnable {

	private String message;
	
	public CoordinatorThread(String coordMessage) throws Exception{
		message = coordMessage;
	}

	// SEND A COORDINATOR MESSAGE TO THE NEXT NODE OF THE RING NET
	// COORDINATOR MESSAGE FORMAT: coordinator newsinkid [list of nodes]
	@Override
	public void run() {
		if (Debug.Coordinator) System.out.println("Nodo " + Nodo.getNode().getType() + " coordination ");
		if (Debug.ElectionSleep)
			try { Thread.sleep(6000); } catch (InterruptedException e1) { e1.printStackTrace();	}
		
		NodoType nextNode = getNextNode(Nodo.getNode().getType());
		boolean okay;
		do {
			okay = true;			
			try {		
				
				Socket nodeSocket = new Socket(nextNode.getAddress(), nextNode.getPort());
		        DataOutputStream outToNode = new DataOutputStream(nodeSocket.getOutputStream());
		        outToNode.writeBytes(message);
		        
		        if (Debug.Coordinator) System.out.println("Nodo " + Nodo.getNode().getType() + " send coord to " + nextNode.getType());
	
	            nodeSocket.close();
	            Nodo.getNode().updateBatteria(Nodo.consumoTrasmissione);
	
			} catch (ConnectException e) {
				okay = false;
				if (Debug.Coordinator) System.out.println("Nodo " + Nodo.getNode().getType() + " Coordinator no connection to " + nextNode.getType());
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
