package it.sdp.sensori;

public class Initializer {

	public static void main(String[] args) throws Exception {
		
		String type = (args.length >= 1)? args[0] : "Light";
		int battery = (args.length >= 2)? Integer.parseInt(args[1]) : 900;
		Boolean isSink = (args.length >= 3)? Boolean.parseBoolean(args[2]) : false;
		int freq = (args.length >= 4)? Integer.parseInt(args[3]) : 5000;
		
		Nodo.getNode().start(type, battery, isSink, freq);
	}

}
