package it.unisa.ocelot.runnable;

import it.unisa.ocelot.c.Builder;
import it.unisa.ocelot.c.BuildingException;
import it.unisa.ocelot.c.StandardBuilder;
import it.unisa.ocelot.c.makefile.JNIMakefileGenerator;
import it.unisa.ocelot.c.makefile.LinuxMakefileGenerator;
import it.unisa.ocelot.c.makefile.MacOSXMakefileGenerator;
import it.unisa.ocelot.c.makefile.WindowsMakefileGenerator;
import it.unisa.ocelot.conf.ConfigManager;

/**
 * This runnable class builds the library that will contain the function to test. It performs several tasks:
 * 1) Instruments the code provided in testobject/main.c
 * 2) Adds the definition of the function and used arguments to C source files of the JNI
 * 3) Generates a makefile (depends on the operative system)
 * 4) Calls make in order to compile the library
 * @author simone
 *
 */
@Deprecated
public class Build {	
	public static void main(String[] args) throws Exception {
		ConfigManager config = ConfigManager.getInstance();
		
		Builder builder = new StandardBuilder(
				config.getTestFilename(), 
				config.getTestFunction(), 
				config.getTestIncludePaths());
		
		JNIMakefileGenerator generator = null; 
		String os = System.getProperty("os.name");
		if (os.contains("Win"))
			generator = new WindowsMakefileGenerator();
		else if (os.contains("Mac"))
			generator = new MacOSXMakefileGenerator();
		else if (os.contains("nix") || os.contains("nux") || os.contains("aix"))
			generator = new LinuxMakefileGenerator();
		//else if (os.contains("sunos"))
		else {
			throw new BuildingException("Your operative system \"" + os + "\" is not supported");
		}
		
		for (String linkLibrary : config.getTestLink())
			generator.addLinkLibrary(linkLibrary);
		
		builder.setMakefileGenerator(generator);
		builder.setOutput(System.out);
		
		builder.build();
	}
}
