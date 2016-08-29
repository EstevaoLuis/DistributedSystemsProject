package it.sdp.sensori;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

// TESTING FOR THE SIMPLE MISURAZIONI BUFFER
public class BufferTest {

	private BufferMisurazioniSimple buffer;
	
	
	@Before
	public void setUp() throws Exception {
		 buffer = new BufferMisurazioniSimple();
	}
	
	// SET DEBUG.BATTERYLEVEL = FALSE

	@Test
	public void sizeBufferSimple() {
		for (int i = 1; i <= 3; i++){
			Misurazione m = new Misurazione("Foo", Integer.toString(i), i);
			buffer.aggiungi(m);
		}
		assertEquals(3, buffer.leggi().size());
	}
	
	@Test
	public void BufferSimpleLimit() {
		int limit = 10;
		for (int i = 1; i <= 12; i++){
			Misurazione m = new Misurazione("Foo", Integer.toString(i), i);
			buffer.aggiungi(m);
		}
		assertEquals(limit, buffer.leggi().size());
	}

}
