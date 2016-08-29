package it.sdp.sensori;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

// TESTING FOR THE MISURAZIONI BUFFER WITH COMPRESSION
public class BufferTestCompression {

	private BufferMisurazioniCompression buffer;
	
	
	@Before
	public void setUp() throws Exception {
		 buffer = new BufferMisurazioniCompression(20);
	}
	
	// SET DEBUG.BATTERYLEVEL = FALSE

	@Test
	public void bufferWithNoCompression1() {
		buffer = new BufferMisurazioniCompression(1);
		Misurazione m = new Misurazione("Foo", Integer.toString(7), 245);
		buffer.aggiungi(m);
		assertEquals(1, buffer.leggi().size());
	}
	
	@Test
	public void bufferWithCompression() {
		buffer = new BufferMisurazioniCompression(6);
		// 1 2 3 4 5
		for (int i = 1; i <= 5; i++){
			Misurazione m = new Misurazione("Foo", Integer.toString(i), i);
			buffer.aggiungi(m);
		}
		// 3 all togheter or 4.0625 one by one
		List<Misurazione> l = buffer.leggi();
		printValues(l);
		assertEquals(1, l.size());
	}
	
	@Test
	public void bufferWithNoCompression2() {
		buffer = new BufferMisurazioniCompression(1);
		for (int i = 1; i <= 5; i++){
			Misurazione m = new Misurazione("Foo", Integer.toString(3*i), i);
			buffer.aggiungi(m);
		}
		List<Misurazione> l = buffer.leggi();
		printValues(l);
		assertEquals(5, l.size());
	}
	
	@Test
	public void bufferWithSomeCompression() {
		buffer = new BufferMisurazioniCompression(1);
		// 2 4 6 8 10 12 14 16 18 20
		for (int i = 1; i <= 10; i++){
			Misurazione m = new Misurazione("Foo", Integer.toString(2*i), i);
			buffer.aggiungi(m);
		}
		// 3 7 11 15 19
		List<Misurazione> l = buffer.leggi();
		printValues(l);
		assertEquals(5, l.size());
	}
	
	private void printValues(List<Misurazione> list){
		System.out.println("Values:");
		for (Misurazione m : list) {
			System.out.println(m.getValue());
		}
	}
	

}
