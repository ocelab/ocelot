package it.unisa.ocelot.c;

import java.io.IOException;
import java.util.ArrayList;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGBuilder;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.c.compiler.writer.ASTWriter;
import it.unisa.ocelot.c.instrumentor.CFunctionGenerator;
import it.unisa.ocelot.c.instrumentor.ExternalReferencesVisitor;
import it.unisa.ocelot.c.instrumentor.InstrumentorVisitor;
import it.unisa.ocelot.c.instrumentor.MacroDefinerVisitor;
import org.apache.commons.io.IOUtils;

import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.util.Utils;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;

public class StandardBuilder extends Builder {
	private String testFilename;
	private String testFunction;
	private String[] testIncludes;

	private String callMacro;
	private String externDeclarations;

	private ConfigManager config;

	public StandardBuilder(String pTestFilename, String pTestFunction, String[] pTestIncludes) {
		super();
		setOutput(System.out);

		this.testFilename = pTestFilename;
		this.testFunction = pTestFunction;
		this.testIncludes = pTestIncludes;
	}

	@Override
	public void build() throws IOException, BuildingException {
		this.config = ConfigManager.getInstance();
		if (this.makefileGenerator == null)
			throw new BuildingException("No makefile generator specified");
		if (this.stream == null)
			throw new BuildingException("No output stream specified");

		try {
			//this.stream.print("Instrumenting C file... \n");
			instrument();
			//this.stream.println("Done!\n");
		} catch (Exception e) {
			//e.printStackTrace();
			throw new BuildingException(e.getMessage());
		}

		//Instrument function files, in order to compile the sockets
		CFG cfg = null;
		try {
			//this.stream.print("Instrumenting Function files... \n");
			cfg = CFGBuilder.build(config.getTestFilename(), config.getTestFunction());
			CFunctionGenerator cFunctionGenerator = new CFunctionGenerator(cfg.getTypeGraph());

			String headerFunction = cFunctionGenerator.generateHeaderFile();
			String sourceFunction = cFunctionGenerator.generateSourceCode();

			Utils.writeFile("socket/function.h", headerFunction);
			Utils.writeFile("socket/function.c", sourceFunction);

			//this.stream.print("Done!\n");
		} catch (CoreException e) {
			e.printStackTrace();
		}

		//Builds the library
		//this.stream.print("Building library... ");
		this.makefileGenerator.generate();

		Process proc = this.makefileGenerator.runCompiler();

		this.stream.println(IOUtils.toString(proc.getInputStream()));

		try {
			int result;
			if ((result = proc.waitFor()) == 0);
				//this.stream.println("Done!");
			else {
				//this.stream.println("ABORTED. An error occurred: " + result);
				throw new BuildingException(IOUtils.toString(proc.getErrorStream()));
			}
		} catch (InterruptedException e) {
			throw new BuildingException("Interrupted");
		}

		//this.stream.println("\nEverything done.");
	}

	private void instrument() throws Exception {
		String code = Utils.readFile(this.testFilename);

		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(
				this.testFilename,
				this.testIncludes).copy();

		IASTPreprocessorStatement[] macros =  translationUnit.getAllPreprocessorStatements();

		ExternalReferencesVisitor referencesVisitor = new ExternalReferencesVisitor(this.testFunction);
		translationUnit.accept(referencesVisitor);

		InstrumentorVisitor instrumentor = new InstrumentorVisitor(this.testFunction);
		MacroDefinerVisitor macroDefiner = new MacroDefinerVisitor(this.testFunction, referencesVisitor.getExternalReferences());

		//NOTE: macroDefine MUST preceed instrumentor in visit
		translationUnit.accept(macroDefiner);
		translationUnit.accept(instrumentor);

		ASTWriter writer = new ASTWriter();

		String outputCode = writer.write(translationUnit);

		StringBuilder result = new StringBuilder();
		for (IASTPreprocessorStatement macro : macros) {
			if (macro instanceof IASTPreprocessorIncludeStatement) {
				IASTPreprocessorIncludeStatement include = (IASTPreprocessorIncludeStatement)macro;
				if (include.isSystemInclude())
					result.append(macro.getRawSignature()).append("\n");
			} else
				result.append(macro.getRawSignature()).append("\n");
		}
		result.append("#include \"ocelot.h\"\n");
		result.append(outputCode);

		Utils.writeFile("socket/main.c", result.toString());

		StringBuilder mainHeader = new StringBuilder();
		mainHeader.append("#include \"ocelot.h\"\n");
		mainHeader.append("#include <stdio.h>\n");
		mainHeader.append("#include <math.h>\n");
		for (IASTNode typedef : instrumentor.getTypedefs()) {
			mainHeader.append(writer.write(typedef));
			mainHeader.append("\n");
		}

		mainHeader.append("#define OCELOT_TESTFUNCTION ").append(this.testFunction).append("\n");

		Utils.writeFile("socket/main.h", mainHeader.toString());

		this.callMacro = macroDefiner.getCallMacro();
		this.externDeclarations = referencesVisitor.getExternalDeclarations();
	}

}

