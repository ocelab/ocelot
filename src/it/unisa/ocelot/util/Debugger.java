package it.unisa.ocelot.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Saves debug information on a file when required
 * @author simone
 */
public class Debugger {
	private static Map<String, Integer> traceMap = new HashMap<String, Integer>();
	private String content;
	private String filename;
	
	/**
	 * Instantiate a standard debugger. All the debug information will be saved on a
	 * file called "debug".
	 */
	public Debugger() {
		this("debug");
	}
	
	/**
	 * Instantiate a debugger with the specified filename
	 * @param pFilename Name of the file on which debug information will be saved
	 */
	public Debugger(String pFilename) {
		this.filename = pFilename;
	}
	
	/**
	 * Adds a line to the debug information to be saved
	 * @param pLine Debug line
	 */
	public void write(String pLine) {
		if (content==null)
			content = "";
		this.content += pLine+"\n";
	}

	public static void trace(String methodName) {
		if (!traceMap.containsKey(methodName))
			traceMap.put(methodName, 0);
		
		traceMap.put(methodName, traceMap.get(methodName) + 1);
	}
	
	public static void printAll() {
		System.out.println(traceMap);
	}
	
	/**
	 * Saves the information stored in a file with the name specified in the 
	 * constructor. If an error occurs, it will be notified on the standard
	 * output.
	 */
	public void save() {
		try {
			Utils.writeFile(this.filename, this.content);
		} catch (IOException e) {
			System.out.println("Debugger failed!");
		}
	}

	public void clear() {
		this.content = "";
	}
}
