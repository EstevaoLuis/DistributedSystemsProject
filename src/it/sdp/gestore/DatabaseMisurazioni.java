package it.sdp.gestore;

import it.sdp.sensori.Misurazione;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatabaseMisurazioni {
	
	private static DatabaseMisurazioni instance;
	
	// DATABASES
	private List<Misurazione> LightDb = new ArrayList<Misurazione>();
	private List<Misurazione> TempDb = new ArrayList<Misurazione>();
	private List<Long> PIR1Db = new ArrayList<Long>();
	private List<Long> PIR2Db = new ArrayList<Long>();
	
	// SYNCH LOCKS
	private Object LightLock = new Object();
	private Object TempLock  = new Object();
	private Object PIR1Lock  = new Object();
	private Object PIR2Lock  = new Object();
	
	// SINGLETON GET INSTANCE
	public synchronized static DatabaseMisurazioni getInstance(){
		if (instance == null)
			instance = new DatabaseMisurazioni();
		return instance;
	}
	
	private DatabaseMisurazioni(){}
	
	// ADD NEW RECORD
	public void AddMisurazione(Misurazione m){
		String type = m.getType();
		if (type.equals("Light")){
			synchronized (LightLock) {
				LightDb.add(m);
				Collections.sort(LightDb);
			}
		}
		else if (type.equals("Temperature")){
			synchronized (TempLock) {
				TempDb.add(m);
				Collections.sort(TempDb);
			}
		}
		else if (type.equals("PIR1")){
			synchronized (PIR1Lock) {
				PIR1Db.add(m.getTimestamp());
				Collections.sort(PIR1Db);
			}
		}
		else if (type.equals("PIR2")){
			synchronized (PIR2Lock) {
				PIR2Db.add(m.getTimestamp());
				Collections.sort(PIR2Db);
			}
		}
	}
	
	// QUERY TEMP MEDIA
	public double TempMedia(long a, long b) throws Exception{
		int count = 0;
		double partial = 0;
		synchronized (TempLock) {
			for (Misurazione m : TempDb) {
				if (m.getTimestamp() >= a && m.getTimestamp() <= b) {
					count++;
					partial = ((count - 1) * partial + Double.parseDouble(m
							.getValue())) / (double) count;
				}
				if (m.getTimestamp() > b)
					break;
			}
		}
		if (count == 0) throw new Exception();
		return partial;
	}
	
	// QUERY TEMP MIN E MAX
	public double[] TempMinMax(long a, long b) throws Exception{
		double min = 0;
		double max = 0;
		boolean first = true;
		synchronized (TempLock) {
			for (Misurazione m : TempDb) {
				if (m.getTimestamp() >= a && m.getTimestamp() <= b) {
					if (first) {
						first = false;
						max = min = Double.parseDouble(m.getValue());
					} else {
						double value = Double.parseDouble(m.getValue());
						max = Math.max(max, value);
						min = Math.min(min, value);
					}
				}
				if (m.getTimestamp() > b)
					break;
			}
		}
		if (first) throw new Exception();
		return new double[] {min, max};
	}
	
	// QUERY TEMP PIU' RECENTE
	public Misurazione TempRecent(){
		synchronized (TempLock) {
			return TempDb.get(TempDb.size() - 1);
		}
	}
	
	// QUERY LIGHT MEDIA
	public double LightMedia(long a, long b) throws Exception{
		int count = 0;
		double partial = 0;
		synchronized (LightLock) {
			for (Misurazione m : LightDb) {
				if (m.getTimestamp() >= a && m.getTimestamp() <= b) {
					count++;
					partial = ((count - 1) * partial + Double.parseDouble(m
							.getValue())) / (double) count;
				}
				if (m.getTimestamp() > b)
					break;
			}
		}
		if (count == 0) throw new Exception();
		return partial;
	}
	
	// QUERY LIGHT MIN E MAX
	public double[] LightMinMax(long a, long b) throws Exception{
		double min = 0;
		double max = 0;
		boolean first = true;
		synchronized (LightLock) {
			for (Misurazione m : LightDb) {
				if (m.getTimestamp() >= a && m.getTimestamp() <= b) {
					if (first) {
						first = false;
						max = min = Double.parseDouble(m.getValue());
					} else {
						double value = Double.parseDouble(m.getValue());
						max = Math.max(max, value);
						min = Math.min(min, value);
					}
				}
				if (m.getTimestamp() > b)
					break;
			}
		}
		if (first) throw new Exception();
		return new double[] {min, max};
	}
	
	// QUERY LIGHT PIU' RECENTE
	public Misurazione LightRecent(){
		synchronized (LightLock) {
			return LightDb.get(LightDb.size() - 1);
		}
	}
	
	// QUERY LIGHT WHERE TEMP IS MAX
	public Misurazione LumWhereTempMax(long a, long b) throws Exception{
		double maxValue = 0;
		long maxTime = 0;
		boolean first = true;
		synchronized (TempLock) {
			// CERCO MASSIMA TEMP
			for (Misurazione m : TempDb) {
				if (m.getTimestamp() >= a && m.getTimestamp() <= b) {
					if (first) {
						first = false;
						maxValue = Double.parseDouble(m.getValue());
						maxTime = m.getTimestamp();
					} else {
						double value = Double.parseDouble(m.getValue());
						if (value > maxValue) {
							maxValue = value;
							maxTime = m.getTimestamp();
						}
					}
				}
				if (m.getTimestamp() > b)
					break;
			}
		}
		if (first) throw new Exception();
		// CERCO LUM PIU VICINA
		boolean firstLux = true;
		long timeDiff = 0;
		Misurazione mlux = null;
		synchronized (LightLock) {
			for (Misurazione m : LightDb) {
				if (m.getTimestamp() >= a && m.getTimestamp() <= b) {
					if (firstLux) {
						firstLux = false;
						timeDiff = Math.abs(m.getTimestamp() - maxTime);
						mlux = m;
					} else {
						if (Math.abs(m.getTimestamp() - maxTime) < timeDiff) {
							timeDiff = Math.abs(m.getTimestamp() - maxTime);
							mlux = m;
						} else {
							break;
						}
					}
					if (m.getTimestamp() > b)
						break;
				}
			}
		}
		if (firstLux) throw new Exception();
		return mlux;
	}
	
	// QUERY PIR COUNT
	public int ContaPresenze(long a, long b, int rilevatore){
		int count = 0;
		List<Long> pir = null;
		Object lock = null;
		if (rilevatore == 1){
			pir = PIR1Db;
			lock = PIR1Lock;
		}
		else if (rilevatore == 2){
			pir = PIR2Db;
			lock = PIR2Lock;
		}
		synchronized (lock) {
			for (Long l : pir) {
				if (l >= a && l <= b)
					count++;
				else if (l > b)
					break;
			}
		}
		return count;
	}
	
	
	

}
