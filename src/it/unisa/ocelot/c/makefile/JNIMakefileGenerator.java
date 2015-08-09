package it.unisa.ocelot.c.makefile;

import java.io.IOException;

import it.unisa.ocelot.util.Utils;

public abstract class JNIMakefileGenerator {
	private String filename;
	
	public JNIMakefileGenerator(String pFilename) {
		this.filename = pFilename;
	}
	
	public abstract String getCCompiler();
	public abstract String getJavaHome();
	public abstract String getSystemInclude();
	
	public abstract String[] getGlib2Paths();
	public abstract String[] getJavaPaths();
	public abstract String getMoreOptions();
	
	public abstract String getCFlags();
	public String[] getLibs() {
		return new String[]{"glib-2.0"};
	}
	
	public abstract String getLibName();
	
	public void generate() throws IOException {
		String glib2paths = "";
		for (String temp : this.getGlib2Paths())
			glib2paths += "-I"+temp+" ";
		
		String javapaths = "";
		for (String temp : this.getJavaPaths())
			javapaths += "-I"+temp+" ";
		
		String libspath = "";
		for (String temp : this.getLibs())
			libspath += "-l"+temp+" ";
		
		String moreOptions = this.getMoreOptions();
		
		String result = "CC = " + this.getCCompiler() + "\n\n" +
		"JAVA_HOME = " + this.getJavaHome() + "\n\n" +
		"SYSTEM_INCLUDE = " + this.getSystemInclude() + "\n\n" +
		"GLIB2_INCUDE = " + glib2paths + "\n" +
		"JAVA_INCLUDE = " + javapaths + "\n\n" +
		"CFLAGS = " + this.getCFlags() + "\n\n" + 
		"INCLUDES = $(GLIB2_INCUDE) $(JAVA_INCLUDE)\n" +
		"LIBS = " + libspath + "\n" +
		"SRCS = ocelot.c EN_CBridge.c main.c" + "\n\n" +
		"MOREOPTS = " + moreOptions + "\n\n"+
		"MAIN = ../" + this.getLibName() + "\n\n" +
		".PHONY: clean all\n" +
		"all:\n"+
		"\t$(CC) $(CFLAGS) $(INCLUDES) -o $(MAIN) $(LFLAGS) $(SRCS) $(LIBS) $(MOREOPTS)\n"+
		//"\t$(RM) EN_CBridge.c\n"+
		"clean:\n" +
		"\t$(RM) $(MAIN)\n";
		
		Utils.writeFile(this.filename, result);
	}
	
	public abstract Process runCompiler() throws IOException;
}
