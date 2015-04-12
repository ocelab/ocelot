package it.unisa.ocelot.c.compiler;

import it.unisa.ocelot.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.scanner.FileCodeReaderFactory;

public class GCC implements Compiler {

	@Override
	public String preprocess(File pInput) {
		return commandLine(new String[] {"bash",  "-c", "gcc -E \"" + pInput.getAbsolutePath() + "\" | sed '/^\\#/d'"});
	}
	
	public String preprocess(File pInput, File pOutput) {
		return commandLine(new String[] {"gcc",  "-E", pInput.getPath()});
	}

	@Override
	public void compile(File pInput, File pOutput) {
		// TODO Auto-generated method stub
		
	}

	private String commandLine(String[] pCommand) {
		try {
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(pCommand);
			
			return IOUtils.toString(pr.getInputStream());
		} catch (IOException e) {
			System.err.println("Unknown error. Could not execute \"" + pCommand + "\"");
			return "";
		//} catch (InterruptedException e) {
		//	System.err.println("Unknown error. Could not execute \"" + pCommand + "\"");
		//	return "";
		}
	}
	
	public static IASTTranslationUnit getTranslationUnit(char[] code, String pSourceFilename) throws Exception {
		it.unisa.ocelot.c.compiler.Compiler gcc = new GCC();
		
		//String codeString = gcc.preprocess(new File(pSourceFilename));
		String codeString = Utils.readFile(pSourceFilename);
		FileContent fc = FileContent.create(pSourceFilename, codeString.toCharArray());
		Map<String, String> macroDefinitions = new HashMap<String, String>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = FileCodeReaderFactory.getInstance();
		
		IIndex idx = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		
		return GCCLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
	}
}
