package it.sdp.sensori;

public class Sink implements Runnable {

	private boolean isBusy = false; 

	// SINK THREAD
	@Override
	public synchronized void run() {
		if (Debug.SinkStatus) System.out.println("Sink: running ");
		while(!Nodo.getNode().isBatteriaScarica()){
			try {
				Thread.sleep(Nodo.getNode().getFreq());
				if (isBusy) wait(); // WAIT IF THE PREVIOUS THREAD HASN'T FINISHED YET
				isBusy = true;
				new Thread(new SinkThread(this)).start();
			} catch (Exception e) {
				e.printStackTrace(); // SOCKET EXCEPTION
			}
		}
		// FINE SINK: BATTERIA SCARICA
		try {
			if (Debug.SinkStatus) System.out.println("Sink: batteria scarica ");
			if (Debug.ElectionSleep) Thread.sleep(6000);
			Nodo.getNode().doElection();
		} catch (Exception e) {
				e.printStackTrace();
		}
	}
	
	// SINK THREAD FINISHED ITS TASK
	public synchronized void unBusy(){
		isBusy = false;
		notify();		
	}

}