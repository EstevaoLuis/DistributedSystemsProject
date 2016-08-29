package it.sdp.tecnici;

public class TecnicoInitializer {

	// RUN A NEW TECNICO
	public static void main(String arg[]) {

		String address = (arg.length >= 1)? arg[0] : "localhost";
		int port = (arg.length >= 2)? Integer.parseInt(arg[1]) : 8080;

		new Thread(new Tecnico(address, port)).start();
	}
}