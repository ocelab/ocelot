package it.unisa.ocelot.c.compiler;

import it.unisa.ocelot.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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
import org.eclipse.core.runtime.CoreException;

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
		}
	}
	
	public static IASTTranslationUnit getTranslationUnit(String pSourceFilename, String[] pIncludePaths)
			throws IOException, CoreException {
		String codeString = Utils.readFile(pSourceFilename);
		FileContent fileContent = FileContent.create(pSourceFilename, codeString.toCharArray());
		Map<String, String> macroDefinitions = new HashMap<String, String>();
		IScannerInfo scannerInfo = new ScannerInfo(macroDefinitions, pIncludePaths);
		IncludeFileContentProvider includeContentProvider = FileCodeReaderFactory.getInstance();
		
		IIndex index = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		
		return GCCLanguage.getDefault().getASTTranslationUnit(
				fileContent, scannerInfo, includeContentProvider, index, options, log);
	}
	
	public static IASTTranslationUnit getTranslationUnit(String pSourceFilename)
			throws IOException, CoreException {		
		return getTranslationUnit(pSourceFilename, new String[0]);
	}
}
