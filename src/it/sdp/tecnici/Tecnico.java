package it.sdp.tecnici;

import it.sdp.gestore.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

public class Tecnico implements Runnable {

	private String username = "";
	private String myIp;
	private int myPort;

	private String serverAddress;
	private int serverPort;
	private ServerSocket welcomeSocket;
	private BufferedReader br;

	// CONSTRUCTOR
	public Tecnico(String ServerIp, int ServerPort){
		serverAddress = ServerIp;
		serverPort = ServerPort;

		myPort = PortGenerator.getNextPort();
		myIp = getLocalAddress();
		br = new BufferedReader(new InputStreamReader(System.in));
	}

	// GET MY IP ADDRESS
	private static String getLocalAddress(){
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("Error!!!!!");
			return "localhost";
		}
	}	

	@Override
	public void run() {
		// SERVER LISTENER
		try {
			welcomeSocket = new ServerSocket(myPort);
			new Thread(new TecnicoListener(welcomeSocket)).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// FINE INIT LISTENER

		// INSERIMENTO USERNAME
		System.out.println("Insert your username: ");
		try {
			while (username.equals(""))
				username = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// CREATE USER JAXB ELEMENT
		User u = new User(username, myIp, myPort);
		JAXBElement<User> user1 = new JAXBElement<User>(new QName("user"), User.class, u);

		// POST HTTP REQUEST
		Client c = ClientBuilder.newClient();
		WebTarget target = c.target("http://"+serverAddress+":"+serverPort+"/ProgettoSDP/user/add");
		Response response = target.request().post(Entity.entity(user1, MediaType.APPLICATION_XML));
		
		// IF USERNAME ALREADY EXISTS
		while (response.getStatus() != 200){
			System.out.println("Already existing username");
			System.out.println("Insert a new username: ");
			try {
				username = br.readLine();
				while (username.equals(""))
					username = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			u.username = username;
			user1 = new JAXBElement<User>(new QName("user"), User.class, u);
			response = c.target("http://"+serverAddress+":"+serverPort+"/ProgettoSDP/user/add")
					.request().post(Entity.entity(user1, MediaType.APPLICATION_XML));
		}
		// FINE INSERIMENTO USERNAME

		// LOOP OPERAZIONI
		System.out.println("Quale operazione vuoi eseguire?");
		System.out.println("0 -> Esci");
		System.out.println("1 -> Interrogazione");
		String answer = "";
		try {
			answer = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}


		while (!answer.equals("0")){
			if (answer.equals("1")){
				String uri = "http://"+serverAddress+":"+serverPort+"/ProgettoSDP/query/";

				int interrogazione = Interrogazione1();
				switch (interrogazione) {
				case 0:
					uri += InterrogazioneTemp();
					break;
				case 1:
					uri += InterrogazioneLum();
					break;
				case 2:
					uri += InterrogazioneTempLum();
					break;
				case 3:
					uri += InterrogazionePIR();
					break;
				}

				response = c.target(uri).request().get();
				if (response.getStatus() == 200){
					String s = (String) response.readEntity(String.class);
					System.out.println(s);
					try {
						br.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				else{ System.out.println("Informazione non disponibile!"); }
			}
			else { 
				System.out.println("Input invalido!");
			}

			System.out.println("Quale operazione vuoi eseguire?");
			System.out.println("0 -> Esci");
			System.out.println("1 -> Interrogazione");
			try {
				answer = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		// FINE LOOP

		// INIZIO LOGOUT
		try {
			welcomeSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		response = c.target("http://"+serverAddress+":"+serverPort+"/ProgettoSDP/user/delete/"+username).request().delete();
		if (response.getStatus() == 200)
			System.out.println("LogOut Successful!");
		else
			System.out.println("LogOut Error");
		// FINE LOGOUT
	}



	public int Interrogazione1(){
		int risposta = -1;
		while (risposta < 0 || risposta > 3){
			System.out.println("Quale tipo di interrogazione vuoi eseguire?");
			System.out.println("0 -> Temperatura");
			System.out.println("1 -> Luminosità");
			System.out.println("2 -> Luminosità dove Temperatura max");
			System.out.println("3 -> Presenza rilevata");
			try {
				risposta = Integer.parseInt(br.readLine());
			} catch (Exception e) {
				System.out.println("Input invalido!");
				risposta = -1;
			}
		}
		return risposta;
	}

	private String InterrogazionePIR() {
		int risposta = -1;
		while (risposta < 0 || risposta > 2){
			System.out.println("Interrogazioni presenza");
			System.out.println("0 -> Presenze zona est");
			System.out.println("1 -> Presenze zona ovest");
			System.out.println("2 -> Presenza media due zone");
			try {
				risposta = Integer.parseInt(br.readLine());
			} catch (Exception e) {
				System.out.println("Input invalido!");
				risposta = -1;
			}
		}
		switch (risposta) {
		case 0:
			return PresenzaEst();
		case 1:
			return PresenzaOvest();
		default: // 2
			return PresenzaMedia();
		}
	}

	private String InterrogazioneTempLum() {
		long a = 1;
		long b = 0;
		while (a >= b){
			System.out.print("Inserisci due tempi: ");
			try {
				String[] input = br.readLine().split(" ");
				a = Long.parseLong(input[0]) * 1000L;
				b = Long.parseLong(input[1]) * 1000L;
			} catch (Exception e) {
				System.out.println("Input invalido!");
				a = 1;
				b = 0;
			}
		}
		return "lumtempmax/"+a+"/"+b;
	}

	private String InterrogazioneLum() {
		int risposta = -1;
		while (risposta < 0 || risposta > 2){
			System.out.println("Interrogazioni luminosità");
			System.out.println("0 -> Misurazione più recente");
			System.out.println("1 -> Media per intervallo di tempo");
			System.out.println("2 -> Minimo e massimo per intervallo");
			try {
				risposta = Integer.parseInt(br.readLine());
			} catch (Exception e) {
				System.out.println("Input invalido!");
				risposta = -1;
			}
		}
		switch (risposta) {
		case 0:
			return LuminositaPiuRecente();
		case 1:
			return LuminositaMediaIntervallo();
		default: // 2
			return LuminositaMinMaxIntervallo();
		}
	}

	private String InterrogazioneTemp() {
		int risposta = -1;
		while (risposta < 0 || risposta > 2){
			System.out.println("Interrogazioni temperatura");
			System.out.println("0 -> Misurazione più recente");
			System.out.println("1 -> Media per intervallo di tempo");
			System.out.println("2 -> Minimo e massimo per intervallo");
			try {
				risposta = Integer.parseInt(br.readLine());
			} catch (Exception e) {
				System.out.println("Input invalido!");
				risposta = -1;
			}
		}
		switch (risposta) {
		case 0:
			return TemperaturaPiuRecente();
		case 1:
			return TemperaturaMediaIntervallo();
		default: // 2
			return TemperaturaMinMaxIntervallo();
		}
	}

	private String TemperaturaMinMaxIntervallo() {
		long a = 1;
		long b = 0;
		while (a >= b){
			System.out.print("Inserisci due tempi: ");
			try {
				String[] input = br.readLine().split(" ");
				a = Long.parseLong(input[0]) * 1000L;
				b = Long.parseLong(input[1]) * 1000L;
			} catch (Exception e) {
				System.out.println("Input invalido!");
				a = 1;
				b = 0;
			}
		}
		return "tempminmax/"+a+"/"+b;
	}

	private String TemperaturaMediaIntervallo() {
		long a = 1;
		long b = 0;
		while (a >= b){
			System.out.print("Inserisci due tempi: ");
			try {
				String[] input = br.readLine().split(" ");
				a = Long.parseLong(input[0]) * 1000L;
				b = Long.parseLong(input[1]) * 1000L;
			} catch (Exception e) {
				System.out.println("Input invalido!");
				a = 1;
				b = 0;
			}
		}
		return "tempmedia/"+a+"/"+b;
	}

	private String TemperaturaPiuRecente() {
		return "temprecent";
	}

	private String LuminositaMinMaxIntervallo() {
		long a = 1;
		long b = 0;
		while (a >= b){
			System.out.print("Inserisci due tempi: ");
			try {
				String[] input = br.readLine().split(" ");
				a = Long.parseLong(input[0]) * 1000L;
				b = Long.parseLong(input[1]) * 1000L;
			} catch (Exception e) {
				System.out.println("Input invalido!");
				a = 1;
				b = 0;
			}
		}
		return "lightminmax/"+a+"/"+b;
	}

	private String LuminositaMediaIntervallo() {
		long a = 1;
		long b = 0;
		while (a >= b){
			System.out.print("Inserisci due tempi: ");
			try {
				String[] input = br.readLine().split(" ");
				a = Long.parseLong(input[0]) * 1000L;
				b = Long.parseLong(input[1]) * 1000L;
			} catch (Exception e) {
				System.out.println("Input invalido!");
				a = 1;
				b = 0;
			}
		}
		return "lightmedia/"+a+"/"+b;
	}

	private String LuminositaPiuRecente() {
		return "lightrecent";
	}

	private String PresenzaMedia() {
		long a = 1;
		long b = 0;
		while (a >= b){
			System.out.print("Inserisci due tempi: ");
			try {
				String[] input = br.readLine().split(" ");
				a = Long.parseLong(input[0]) * 1000L;
				b = Long.parseLong(input[1]) * 1000L;
			} catch (Exception e) {
				System.out.println("Input invalido!");
				a = 1;
				b = 0;
			}
		}
		return "pirmedia/"+a+"/"+b;
	}

	private String PresenzaOvest() {
		long a = 1;
		long b = 0;
		while (a >= b){
			System.out.print("Inserisci due tempi: ");
			try {
				String[] input = br.readLine().split(" ");
				a = Long.parseLong(input[0]) * 1000L;
				b = Long.parseLong(input[1]) * 1000L;
			} catch (Exception e) {
				System.out.println("Input invalido!");
				a = 1;
				b = 0;
			}
		}
		return "pirovest/"+a+"/"+b;
	}

	private String PresenzaEst() {
		long a = 1;
		long b = 0;
		while (a >= b){
			System.out.print("Inserisci due tempi: ");
			try {
				String[] input = br.readLine().split(" ");
				a = Long.parseLong(input[0]) * 1000L;
				b = Long.parseLong(input[1]) * 1000L;
			} catch (Exception e) {
				System.out.println("Input invalido!");
				a = 1;
				b = 0;
			}
		}
		return "pirest/"+a+"/"+b;
	}
}
