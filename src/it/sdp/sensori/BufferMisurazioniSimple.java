package it.sdp.sensori;

import java.util.ArrayList;
import java.util.List;

// BUFFER MISURAZIONI
public class BufferMisurazioniSimple implements Buffer<Misurazione> {

	private final int size = 10;
	private List<Misurazione> lista = new ArrayList<Misurazione>();

	// ADD
	@Override
	public synchronized void aggiungi(Misurazione m) {
		if (lista.size() == size)
			lista.remove(0);
		Nodo.getNode().updateBatteria(Nodo.consumoLettura);
		lista.add(m);
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
}
