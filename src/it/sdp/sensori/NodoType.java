package it.sdp.sensori;

public class NodoType {
	// NODO DATA STRUCTURE
	private String type;
	private String address;
	private int port;
	
	private NodoType(String type, String address, int port){
		this.type = type;
		this.address = address;
		this.port = port;				
	}
	
	// GET DEFAULT NODO ADDRESSES BY TYPE
	public static NodoType getDefaultInfo(String type){
		if (type.equals("Light"))
			return new NodoType(type, "localhost", 9100);
		if (type.equals("PIR1"))
			return new NodoType(type, "localhost", 9101);
		if (type.equals("PIR2"))
			return new NodoType(type, "localhost", 9102);
		if (type.equals("Temperature"))
			return new NodoType(type, "localhost", 9103);
		return null;
	}
	
	// GET NODO TYPE
	public String getType(){
		return type;
	}
	
	// GET NODO ADDRESS
	public String getAddress(){
		return address;
	}
	
	// GET NODO PORT
	public int getPort(){
		return port;
	}
	

}
