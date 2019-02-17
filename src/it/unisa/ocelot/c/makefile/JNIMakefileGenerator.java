package it.unisa.ocelot.c.makefile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.unisa.ocelot.c.BuildingException;
import it.unisa.ocelot.util.Utils;

public abstract class JNIMakefileGenerator {
	private String filename;
	private List<String> linkLibs;
	
	public JNIMakefileGenerator(String pFilename) {
		this.filename = pFilename;
		this.linkLibs = new ArrayList<>();
	}
	
	public abstract String getCCompiler();
	public abstract String getJavaHome();
	public abstract String getSystemInclude();

	public abstract String[] getJavaPaths() throws BuildingException;
	public abstract String getMoreOptions();
	
	public abstract String getCFlags() throws BuildingException;
	
	public void addLinkLibrary(String pLibrary) {
		this.linkLibs.add(pLibrary);
	}
	
	public abstract String getLibName() throws BuildingException;
	
	public void generate() throws IOException, BuildingException {
		String javapaths = "";
		for (String temp : this.getJavaPaths())
			javapaths += "-I"+temp+" ";
		
		String libspath = "";
		for (String temp : this.linkLibs)
			libspath += "-l"+temp+" ";
		
		String moreOptions = this.getMoreOptions();

		String result = "CC = " + this.getCCompiler() + "\n\n" +
		"JAVA_HOME = " + this.getJavaHome() + "\n\n" +
		"SYSTEM_INCLUDE = " + this.getSystemInclude() + "\n\n" +
		"JAVA_INCLUDE = " + javapaths + "\n\n" +
		"CFLAGS = " + this.getCFlags() + "\n\n" + 
		"INCLUDES = $(GLIB2_INCUDE) $(JAVA_INCLUDE)\n" +
		"LIBS = " + libspath + "\n" +
		"SRCS = clientSocket.c CNewBridge.c" + "\n\n" +
		"MOREOPTS = " + moreOptions + "\n\n"+
		"MAIN = ../" + this.getLibName() + "\n\n" +
		".PHONY: clean all\n" +
		"all:\n"+
		"\t$(CC) $(CFLAGS) $(INCLUDES) -o $(MAIN) $(LFLAGS) $(SRCS) $(LIBS) $(MOREOPTS)\n"+
		"clean:\n" +
		"\t$(RM) $(MAIN)\n";
		
		Utils.writeFile(this.filename, result);
	}
	
	public abstract Process runCompiler() throws IOException, BuildingException;
}
