package it.sdp.sensori;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Nodo{
	
	// NODO PROPERTIES
	private NodoType nodoType;
	private boolean hasStarted = false;
	private Buffer<Misurazione> buffer;
	private boolean isSink;
	private int freqSink;
	private Simulator simulator;
	private ElectionStatus electionStatus = ElectionStatus.NORMAL;
	
	// ACTUAL SINK
	private String sink;
	
	// SERVER SOCKET
	private ServerSocket welcomeSocket;
	
	// BATTERY INFO
	private final int maxBatteria = 1000;
	private int batteria = 1000;
	public static final int consumoLettura = 2;
	public static final int consumoTrasmissione = 5;
	private boolean batteriaScarica = false;
	private boolean batteriaEsaurita = false;
	private int sogliaBatteriaScarica;
	private int sogliaBatteriaEsaurita;
	
	// THRESHOLD FOR COMPRESSION BUFFER
	private double epsilonTemperature = 0.1;
	private double epsilonLight = 1;

	// SYNCH LOCKS
	private Object BatteriaLock = new Object();
	private Object ElectionLock = new Object();
	private Object isSinkLock = new Object();
	private Object SinkLock = new Object();
	
	// SINGLETON INSTANCE
	private static Nodo instance = null;
	
	public synchronized static Nodo getNode(){
		if (instance == null)
			instance = new Nodo();
		return instance;
	}
	
	private Nodo(){}
	
	// START RUNNING NODO
	@SuppressWarnings("unused")
	public void start(String type, int batteriaIniziale, boolean sink, int frequenzaSink) throws Exception{
		if (hasStarted) throw new Exception(); // MAKE SURE TO START ONLY ONCE
		
		hasStarted = true;		
		if (Debug.NodoStatus) System.out.println("Creazione nodo " + type);

		batteria = Math.min(maxBatteria, Math.abs(batteriaIniziale));
		sogliaBatteriaScarica = (int) (0.25 * maxBatteria);
		sogliaBatteriaEsaurita = Math.max(4 * consumoTrasmissione, consumoLettura);
		isSink = sink;
		freqSink = frequenzaSink;
		
		nodoType = NodoType.getDefaultInfo(type);
		if (type.equals("Temperature")){
			buffer = new BufferMisurazioniCompression(epsilonTemperature);
			simulator = new TemperatureSimulator(buffer);
		}
		else if (type.equals("PIR1")){
			buffer = new BufferMisurazioniSimple();
			simulator = new PIR1Simulator(buffer);
		}
		else if (type.equals("PIR2")){
			buffer = new BufferMisurazioniSimple();
			simulator = new PIR2Simulator(buffer);
		}
		else if (type.equals("Light")){
			buffer = new BufferMisurazioniCompression(epsilonLight);
			simulator = new LightSimulator(buffer);
		}
		new Thread(simulator).start();
		welcomeSocket = new ServerSocket(getPort());
		new Thread(new NodoServer(welcomeSocket)).start();
		
		// IF ISSINK START SINK
		if (isSink){
			if (Debug.NodoStatus || Debug.Election) System.out.println("Nodo " + getType() + " is the sink");
			new Thread(new Sink()).start();
			doCoordinator(type);
		}
	}
	
	// UPDATE BATTERY LEVEL
	public void updateBatteria(int energyConsumed){
		synchronized (BatteriaLock) {
			batteria -= energyConsumed;
			if (Debug.BatteryLevel) System.out.println("Nodo " + getType() + " Batteria: " + batteria);
			if (!batteriaScarica && batteria <= sogliaBatteriaScarica) {
				batteriaScarica = true;
				if (Debug.BatteryStatus) System.out.println("Nodo " + getType() + " Batteria scarica ");
			}
			if (!batteriaEsaurita && batteria <= sogliaBatteriaEsaurita) {
				batteriaEsaurita = true;
				if (Debug.BatteryStatus) System.out.println("Nodo " + getType() + " Batteria esaurita ");
				simulator.stopMeGently();
				try {
					welcomeSocket.close(); // TODO pay attenction!!!!
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (Debug.NodoStatus) System.out.println("Nodo " + getType() + " chiude le porte");
				if (Debug.SleepWhenFinish)
					try { Thread.sleep(Long.MAX_VALUE); } catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
	}
	
	// RETURN BATTERY LEVEL
	public int getBatteria(){
		synchronized (BatteriaLock) {
			return batteria;
		}
	}
	
	// CHECK IF BATTERIA SCARICA
	public boolean isBatteriaScarica(){
		synchronized (BatteriaLock) {
			return batteriaScarica;
		}
	}
	
	// CHECK IF BATTERIA ESAURITA
	public boolean isBatteriaEsaurita(){
		synchronized (BatteriaLock) {
			return batteriaEsaurita;
		}
	}
		
	// START A NEW ELECTION
	public void doElection() throws Exception{
		noSink();
		synchronized (ElectionLock) {
			electionStatus = ElectionStatus.ELECTION;
		}
		new Thread (new ElectionThread("")).start();
	}
	
	// CHECK ELECTION RESULTS BEFORE STARTING COORDINATION PHASE
	public void EndElection(String results) throws Exception{
		if (Debug.Election) System.out.println("End Election: " + results);
		if (Debug.ElectionSleep)
			try { Thread.sleep(6000); } catch (InterruptedException e1) { e1.printStackTrace();	}
		String[] split = results.split(" ");
		int maxBattery = 0;
		String winningNode = "";
		ArrayList<String> net = new ArrayList<String>();
		for (int i = 1; i < split.length - 1; i += 2){
			if (Integer.parseInt(split[i+1]) > maxBattery){
				maxBattery = Integer.parseInt(split[i+1]);
				winningNode = split[i];
			}
			net.add(split[i]);					
		}
		setNewSink(winningNode);
		doCoordinator(winningNode);
		NetManager.getNet().UpdateNet(net);
	}
	
	// AFTER ELECTION PHASE, START COORDINATION PHASE
	public void doCoordinator(String sinkNode) throws Exception{
		synchronized (ElectionLock) {
			electionStatus = ElectionStatus.COORDINATOR;
		}
		String message = "coordinator " + sinkNode + NetManager.getNet().getNetString();
		if (Debug.Coordinator) System.out.println("Coordinator message: " + message);
		new Thread (new CoordinatorThread(message)).start();
	}

	// UPDATE NET AND SINK AFTER RECEIVING A COORDINATOR MESSAGE
	public void updateCoordinator(String coordMessage) throws Exception {
		String[] split = coordMessage.split(" ");
		setNewSink(split[1]);
		ArrayList<String> net = new ArrayList<String>();
		for (int i = 2; i < split.length; i ++){
			net.add(split[i]);					
		}
		NetManager.getNet().UpdateNet(net);		
	}

	// END COORDINATION PHASE, RETURN TO NORMAL PHASE
	public void EndCoordinatorStatus() {
		synchronized (ElectionLock) {
			electionStatus = ElectionStatus.NORMAL;			
		}
	}
	
	// GET ELECTION STATUS
	public ElectionStatus getElectionStatus(){
		synchronized (ElectionLock) {
			return electionStatus;			
		}
	}
	
	// SET ME AS THE NEW SINK
	public void newSink() throws Exception{
		if (Debug.Election) System.out.println("Election result: Nodo "+ nodoType.getType() + " new sink");
		if (Debug.ElectionSleep)
			try { Thread.sleep(6000); } catch (InterruptedException e1) { e1.printStackTrace();	}
		synchronized (isSinkLock) {
			isSink = true;
		}
		if (isBatteriaScarica()){
			if (Debug.Election) System.out.println("Nodo "+ nodoType.getType() + " sink scarico");
			new Thread(new SinkGatewayError("Rete non disponibile!\n")).start();
		}
		else{
			if (Debug.Election) System.out.println("Nodo "+ nodoType.getType() + " sink running");
			new Thread(new Sink()).start();
		}
	}

	// GET THE SINK FREQUENCY
	public int getFreq(){
		return freqSink;
	}
	
	// I AM NOT THE SINK ANYMORE
	public void noSink(){
		synchronized (isSinkLock) {
			isSink = false;
		}
	}
	
	// AM I THE SINK?
	public boolean isSink(){
		synchronized (isSinkLock) {
			return isSink;
		}
	}
	
	// READ THE BUFFER
	public List<Misurazione> LetturaBuffer(){
		return buffer.leggi();
	}
	
	// WHO IS THE SINK?
	public String getSink(){
		synchronized (SinkLock) {
			return sink;
		}
	}
	
	// SET THE NEW SINK
	public void setNewSink(String newSink) throws Exception{
		synchronized (SinkLock) {
			sink = newSink;
		}
		if (newSink.equals(nodoType.getType()))
			newSink();
	}
	
	// GET NODE TYPE
	public String getType(){
		return nodoType.getType();
	}
	
	// GET NODE PORT
	public int getPort(){
		return nodoType.getPort();
	}
	
	// DETECTED AN UNREACHABLE NODE, UPDATE NET, AND IF SINK WARN GESTORE
	public void UnreachableNode(String node){
		if (NetManager.getNet().Remove(node) && isSink())
			SendErrorToGateway("Node closed " + node + "\n");
	}
	
	// IF SINK, SEND ERRORS TO GESTORE
	public void SendErrorToGateway(String error){
		if (isSink()) new Thread(new SinkGatewayError(error)).start(); 
	}
}
