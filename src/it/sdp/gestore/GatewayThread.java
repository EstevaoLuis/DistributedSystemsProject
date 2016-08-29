package it.sdp.gestore;

import it.sdp.sensori.Misurazione;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GatewayThread extends Thread {
	
	private Socket socket;
	private BufferedReader inFromClient;
	
	public GatewayThread(Socket connectionSocket) {
		socket = connectionSocket; 
		try{
            inFromClient = new BufferedReader(
                           new InputStreamReader(connectionSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void run(){
		System.out.println("Gateway Thread is running!!! ");

		try {
			// READ FROM CLIENT
			String message = inFromClient.readLine();
			System.out.println("Gateway: message read: " + message);
			String[] splitMessage = message.split(" ");
			String typeOfService = splitMessage[0];
			
			// MISURE RICEVUTE
			if (typeOfService.equals("misure")){
				int amount = Integer.parseInt(splitMessage[1]);
				System.out.println("Gateway: message read int: " + amount);
				salvaMisure(amount);
			}
			
			// ERRORE
			else if (typeOfService.equals("error")){
				if (!(splitMessage[2].equals("closed") &&
						!TryAddNode(splitMessage[3]))) {
					List<User> userLogged = Userlist.getInstance().GetAll();
					for (User user : userLogged) {
						new TecnicoCommunication(user.ip, user.port, message).start();
					}
					System.out.println(message);
				}
			}
			// CHIUSURA SOCKET
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// ADD MISURAZIONI TO DATABASE
	private void salvaMisure(int amount) throws IOException{
		for (int i = 0; i < amount; i++) {
			String misura = inFromClient.readLine();
			System.out.println("Gateway: misura: " + misura);
			String[] misuraSplit = misura.split(" ");
			Misurazione m = new Misurazione(misuraSplit[0], misuraSplit[1],Long.parseLong(misuraSplit[2]));
			DatabaseMisurazioni.getInstance().AddMisurazione(m);
		}
	}
	
	// DATABASE OF CLOSED NODES
	private static List<String> nodesClosed = new ArrayList<String>();
	private static synchronized boolean TryAddNode(String node){
		if (nodesClosed.contains(node))
			return false;
		nodesClosed.add(node);
		return true;		
	}
	
	

}
