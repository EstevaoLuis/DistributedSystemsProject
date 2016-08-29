package it.sdp.gestore;

import java.net.ServerSocket;

public class GatewayMultiThread implements Runnable {

	@Override
	public void run(){
		
		System.out.println("Gateway MultiThread is running!!! ");
    	int port = 9000;
        ServerSocket welcomeSocket;
		
        try {
			welcomeSocket = new ServerSocket(port);
	        while(true) {
	            new GatewayThread(welcomeSocket.accept()).start();
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
