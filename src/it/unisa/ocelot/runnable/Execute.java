package it.unisa.ocelot.runnable;

import it.unisa.ocelot.simulator.CBridge;
import it.unisa.ocelot.simulator.EventsHandler;

public class Execute {
	static {
		System.loadLibrary("Test");
	}
	
	public static void main(String[] args) {	
		CBridge bridge = new CBridge();
		EventsHandler h = new EventsHandler();
		
		Object[] arguments = new Object[4];
		arguments[0] = new Integer(3);
		arguments[1] = new Integer(4);
		arguments[2] = new Integer(54);
		
		bridge.getEvents(h, arguments);
		
		System.out.println(h);
	}
}