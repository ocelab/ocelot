package it.unisa.ocelot.c;

import it.unisa.ocelot.c.makefile.JNIMakefileGenerator;

import java.io.IOException;
import java.io.PrintStream;


public abstract class Builder {
	protected PrintStream stream;
	protected JNIMakefileGenerator makefileGenerator;

	public abstract void build() throws IOException, BuildingException;
	
	public void setOutput(PrintStream pStream) {
		this.stream = pStream;
	}
	
	public void setMakefileGenerator(JNIMakefileGenerator makefileGenerator) {
		this.makefileGenerator = makefileGenerator;
	}
}
