package it.sdp.sensori;

import java.util.ArrayList;
import java.util.Arrays;

// CLASS USED TO MANAGE THE NET OF NODES
public class NetManager {
	
	// GESTORE ADDRESS
	public static final String GestoreAdress = "localhost";
	public static final int GestorePort = 9000;
	
	// DEFAULT CONFIGURATION
	private NetManager(){
		net = new ArrayList<String>(
				Arrays.asList("Light", "PIR1", "PIR2", "Temperature"));
	}
	
	// SINGLETON INSTANCE
	private static NetManager instance = null;
	
	public synchronized static NetManager getNet(){
		if (instance == null)
			instance = new NetManager();
		return instance;
	}
	
	// LIST OF OPEN NODES
	private ArrayList<String> net = new ArrayList<String>(
			Arrays.asList("Light", "PIR1", "PIR2", "Temperature"));
	
	// UPDATE NET
	public synchronized void UpdateNet(ArrayList<String> newNet){
		/*net = newNet;
		Collections.sort(net);*/
		
		// SEARCH THE DIFFERENCES BETWEEN OLD AND NEW NET
		@SuppressWarnings("unchecked")
		ArrayList<String> oldNet = ((ArrayList<String>) newNet.clone());
		oldNet.removeAll(newNet);
		for (String node : oldNet) {
			Nodo.getNode().UnreachableNode(node);
		}
	}
	
	// GET THE NEXT NODE OF THE RING NET
	public synchronized NodoType nextNode(String node){
		int index = net.indexOf(node);
		int netSize = net.size();
		if (index == -1) System.out.println("NEXT NODE -1 !!!!! " + node); // CHECK
		String nextNodeType = net.get((index + 1) % netSize );
		return NodoType.getDefaultInfo(nextNodeType);
	}
	
	// RETURN A STRING CONTAINING ALL NODES OF THE NET
	public synchronized String getNetString(){
		String netString = "";
		for (String string : net) {
			netString = netString + " " + string;  
		}
		return netString;
	}
	
	// RETURN A LIST WITH ALL NODES
	public synchronized ArrayList<String> getAllNodes(){
		return net;
	}

	// REMOVE A CLOSED NODE
	public synchronized boolean Remove(String node) {
		return net.remove(node);		
	}
	
	
	

}
