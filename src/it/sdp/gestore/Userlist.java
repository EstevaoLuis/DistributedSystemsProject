package it.sdp.gestore;

import java.util.ArrayList;
import java.util.List;

public class Userlist {
	
	// DATABASE OF LOGGED USERS
	private List<User> userlist = new ArrayList<User>();
	
	// SINGLETON INSTANCE
	private static Userlist instance;
	
	public synchronized static Userlist getInstance(){
		if (instance == null)
			instance = new Userlist();
		return instance;
	}
	
	private Userlist(){}
	
	// TRY TO ADD A NEW USER
	public synchronized boolean TryAdd(User newUser){
		for (User user : userlist) {
			if (user.username.equals(newUser.username)) return false;
		}
		userlist.add(newUser);
		return true;
	}
	
	// DELETE A LOGGED USER
	public synchronized boolean Delete(String userToDelete){
		int index = -1;
		int currentIndex = 0;
		for (User user : userlist) {
			if (user.username.equals(userToDelete)){
				//new TecnicoCommunication(user.ip, user.port, "close").start();
				index = currentIndex;
				break;
			}
			currentIndex++;	
		}
		if (index == -1) return false;
		userlist.remove(index);
		return true;
	}
	
	// GET ALL LOGGED USERS
	public synchronized List<User> GetAll(){
		return userlist;
	}
	
}
