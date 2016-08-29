package it.sdp.sensori;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// BUFFER MISURAZIONI WITH COMPRESSION
public class BufferMisurazioniCompression implements Buffer<Misurazione> {

	private final int size = 100;
	private List<Misurazione> lista = new ArrayList<Misurazione>();

	private double epsilon;
	
	public BufferMisurazioniCompression(double epsilon) {
		this.epsilon = epsilon;
	}
	
	// ADD
	@Override
	public synchronized void aggiungi(Misurazione m) {
		if (lista.size() == size)
			lista.remove(0);	
		Nodo.getNode().updateBatteria(Nodo.consumoLettura);
		lista.add(m);
		compression();
	}

	// READ
	@SuppressWarnings("unchecked")
	@Override
	public synchronized List<Misurazione> leggi() {
		List<Misurazione> newList = (List<Misurazione>) ((ArrayList<Misurazione>)lista).clone();
		Nodo.getNode().updateBatteria(Nodo.consumoLettura);
		lista.clear();
		return newList;
	}
	
	// POOR MAN'S COMPRESSION - MID RANGE
	private void compression(){
		Collections.sort(lista);
		List<Misurazione> pca = new ArrayList<Misurazione>();
		int n = 0;
		double m = getValue(n);
		double M = getValue(n);
		while (n < lista.size()) {
			if (Math.max(M, getValue(n))-Math.min(m, getValue(n)) > 2 * epsilon ){
				pca.add(new Misurazione(lista.get(n-1).getType(), Double.toString((m+M)/2), lista.get(n-1).getTimestamp()));
				m = getValue(n);
				M = getValue(n);
			}
			else {
				m = Math.min(m, getValue(n));
				M = Math.max(M, getValue(n));			
			}
			n = n + 1;
		}
		pca.add(new Misurazione(lista.get(n-1).getType(), Double.toString((m+M)/2), lista.get(n-1).getTimestamp()));
		lista = pca;
	}
	
	private double getValue(int index){
		return Double.parseDouble(lista.get(index).getValue());
	}
}
