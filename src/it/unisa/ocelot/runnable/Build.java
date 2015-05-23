package it.unisa.ocelot.runnable;

import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.c.instrumentor.InstrumentorVisitor;
import it.unisa.ocelot.c.instrumentor.MacroDefinerVisitor;
import it.unisa.ocelot.c.makefile.JNIMakefileGenerator;
import it.unisa.ocelot.c.makefile.LinuxMakefileGenerator;
import it.unisa.ocelot.c.makefile.MacOSXMakefileGenerator;
import it.unisa.ocelot.c.makefile.WindowsMakefileGenerator;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.util.Utils;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

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
	public static ConfigManager config;
	
	public static void main(String[] args) throws Exception {
		config = ConfigManager.getInstance();
		
		//Insturments the code and copies it in main.c
		System.out.print("Instrumenting C file... \n");
		String callMacro = instrument();
		System.out.println("Done!");
		
		//Adds extra macros in CBridge.c
		System.out.print("Defining test function call... ");
		enrichJNICall(callMacro);
		System.out.println("Done!");
		
		
		//Builds the makefile OS specific
		System.out.print("Building makefile... ");
		JNIMakefileGenerator generator = null; 
		String os = System.getProperty("os.name");
		if (os.contains("Win"))
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
		
		//Builds the library
		System.out.print("Building library... ");
		generator.generate();
		
		Process proc = Runtime.getRuntime().exec(new String[] {"mingw32-make", "--directory=jni"});
				
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
		String code = Utils.readFile(config.getTestFilename());
		
		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(
				code.toCharArray(),
				config.getTestFilename(),
				config.getTestIncludePaths()).copy();
		
		IASTPreprocessorStatement[] macros =  translationUnit.getAllPreprocessorStatements();
		
		InstrumentorVisitor instrumentor = new InstrumentorVisitor(config.getTestFunction());
		MacroDefinerVisitor macroDefiner = new MacroDefinerVisitor(config.getTestFunction());
		
		//NOTE: macroDefine MUST preceed instrumentor in visit
		translationUnit.accept(macroDefiner);
		translationUnit.accept(instrumentor);
		
		it.unisa.ocelot.c.compiler.writer.ASTWriter writer = new it.unisa.ocelot.c.compiler.writer.ASTWriter();
				
		String outputCode = writer.write(translationUnit);
		
		String result = "";
		for (IASTPreprocessorStatement macro : macros) {
			if (macro instanceof IASTPreprocessorIncludeStatement) {
				IASTPreprocessorIncludeStatement include = (IASTPreprocessorIncludeStatement)macro;
				if (include.isSystemInclude())
					result += macro.getRawSignature()+"\n";
			} else
				result += macro.getRawSignature() + "\n";
		}
		result += "#include \"ocelot.h\"\n";
		result += outputCode;
		
		Utils.writeFile("jni/main.c", result);
		
		String mainHeader = "";
		mainHeader += "#include \"ocelot.h\"\n";
		mainHeader += "#include <stdio.h>\n";
		mainHeader += "#include <math.h>\n";

		mainHeader += "#define OCELOT_TESTFUNCTION " + config.getTestFunction() + "\n";

		Utils.writeFile("jni/main.h", mainHeader);
		
		return macroDefiner.getCallMacro();
	}
	
	public static void enrichJNICall(String pCallMacro) throws IOException {
		String metaJNI = Utils.readFile("jni/CBridge.c");
		
		metaJNI = "#include \"CBridge.h\"\n" +
				pCallMacro + "\n\n" + 
				metaJNI;
		
		Utils.writeFile("jni/EN_CBridge.c", metaJNI);
	}
	
	public static JNIMakefileGenerator buildWindows() {
		return new WindowsMakefileGenerator();
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
