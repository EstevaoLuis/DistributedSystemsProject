package it.sdp.gestore;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="user")
public class User {
	@XmlElement(name = "username")
	public String username;
	@XmlElement(name = "ip")
	public String ip; 
	@XmlElement(name = "port")
	public int port;
	
	public User(String user, String ipAddress, int portNumber) {
		username = user;
		ip = ipAddress;
		port = portNumber;
	}
	
	public User(){}

}
