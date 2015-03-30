package it.unisa.ocelot.runnable;


import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;

import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.c.instrumentor.InstrumentorVisitor;
import it.unisa.ocelot.c.makefile.JNIMakefileGenerator;
import it.unisa.ocelot.c.makefile.LinuxMakefileGenerator;
import it.unisa.ocelot.c.makefile.MacOSXMakefileGenerator;
import it.unisa.ocelot.util.Utils;

/**
 * This runnable class builds the library that will contain the function to test. It performs several tasks:
 * 1) Instruments the code provided in testobject/main.c
 * 2) Adds the definition of the function and used arguments to C source files of the JNI
 * 3) Generates a makefile (depends on the operative system)
 * 4) Calls make in order to compile the library
 * @author simone
 *
 */
public class Build {
	public static void main(String[] args) throws Exception {
		System.out.print("Instrumenting C file... ");
		String callMacro = instrument();
		System.out.println("Done!");
		
		System.out.print("Defining test function call... ");
		enrichJNICall(callMacro);
		System.out.println("Done!");
		
		
		System.out.print("Building makefile... ");
		JNIMakefileGenerator generator = null; 
		String os = System.getProperty("os.name");
		
		if (os.contains("win"))
			generator = buildWindows();
		else if (os.contains("Mac"))
			generator = buildMac();
		else if (os.contains("nix") || os.contains("nux") || os.contains("aix"))
			generator = buildLinux();
		else if (os.contains("sunos"))
			generator = buildSolaris();
		else {
			System.err.println("Your operative system \"" + os + "\" is not supported");
			System.exit(-1);
		}
		System.out.println("Done!");
		
		System.out.print("Building library... ");
		generator.generate();
		
		Process proc = Runtime.getRuntime().exec(new String[] {"make", "--directory=jni"});
		
		System.out.println(IOUtils.toString(proc.getInputStream()));
		
		int result;
		if ((result=proc.waitFor()) == 0)
			System.out.println("Done!");
		else {
			System.err.println(IOUtils.toString(proc.getErrorStream()));
			System.out.println("ABORTED. An error occurred: " + result);
		}
		
		System.out.println("Done!");
		
		System.out.println("\nEverything done.");
	}
	
	public static String instrument() throws Exception {
		String code = Utils.readFile("testobject/main.c");
		
		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(code.toCharArray(), "testobject/main.c").copy();
		InstrumentorVisitor visitor = new InstrumentorVisitor();
		
		translationUnit.accept(visitor);
		
		ASTWriter writer = new ASTWriter();
		String outputCode = writer.write(translationUnit);
		
		Utils.writeFile("jni/main.c", "#include \"main.h\"\n" + outputCode);
		Utils.writeFile("jni/main.h", "#include \"ocelot.h\"\n" + Utils.readFile("testobject/main.h"));
		
		return visitor.getCallMacro();
	}
	
	public static void enrichJNICall(String pCallMacro) throws IOException {
		String metaJNI = Utils.readFile("jni/CBridge.c");
		
		metaJNI = "#include \"CBridge.h\"\n" +
				pCallMacro + "\n\n" + 
				metaJNI;
		
		Utils.writeFile("jni/EN_CBridge.c", metaJNI);
	}
	
	public static JNIMakefileGenerator buildWindows() {
		System.err.println("Sorry, we currently can't build the library for Windows.");
		System.exit(-1);
		return null;
	}
	
	public static JNIMakefileGenerator buildMac() {
		return new MacOSXMakefileGenerator();
	}
	
	public static JNIMakefileGenerator buildLinux() {
		return new LinuxMakefileGenerator();
	}
	
	public static JNIMakefileGenerator buildSolaris() {
		System.err.println("Sorry, we currently can't build the library for Solaris.");
		System.exit(-1);
		return null;
	}
}
