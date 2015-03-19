package util;

import java.io.IOException;

/**
 * Saves debug information on a file when required
 * @author simone
 */
public class Debugger {
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
		this.content += pLine+"\n";
	}
	
	/**
	 * Clears the debug information
	 */
	public void reset() {
		this.content = "";
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
