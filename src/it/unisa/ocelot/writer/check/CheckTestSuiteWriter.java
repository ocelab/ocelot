package it.unisa.ocelot.writer.check;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;

import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.util.Utils;
import it.unisa.ocelot.writer.TestCaseWriter;
import it.unisa.ocelot.writer.TestSuiteWriter;
import it.unisa.ocelot.writer.TestWritingException;

public class CheckTestSuiteWriter extends TestSuiteWriter<CheckTestCaseWriter> {
	private String suiteName;
	private ConfigManager config;
	
	@Override
	public String write(ConfigManager pConfigManager) throws TestWritingException {
		String uuid = UUID.randomUUID().toString();
		this.suiteName = "ocelot_generated_" + uuid.split("\\-")[0]; 
		this.config = pConfigManager;
		
		try {
			String code = Utils.readFile(config.getTestFilename());
			IASTTranslationUnit translationUnit = GCC.getTranslationUnit(code.toCharArray(), config.getTestFilename());
			
			String suite = "";
			
			suite += writeHeaders(translationUnit);
			suite += writeTestCases(translationUnit);
			suite += writeTestSuite();
			suite += writeMain();

			
			return suite;
		} catch (CoreException|IOException e) {
			throw new TestWritingException(e.getMessage());
		}
	}
	
	private String writeHeaders(IASTTranslationUnit pTranslationUnit) {
		String headers = "";
		
		headers += "#include <stdlib.h>\n";
		headers += "#include <check.h>\n";
		
		
		return headers;
	}
	
	private String writeTestCases(IASTTranslationUnit pTranslationUnit) throws TestWritingException {
		String testCases = "";
		
		for (TestCaseWriter testCaseWriter : this.testSuite) {
			//This has to be always true!
			if (testCaseWriter instanceof CheckTestCaseWriter)
				((CheckTestCaseWriter) testCaseWriter).setTranslationUnit(pTranslationUnit);
			
			testCases += testCaseWriter.write(this.config);
			testCases += "\n";
		}
		
		return testCases;
	}
	
	private String writeTestSuite() {
		String testSuite = "";
		
		testSuite += "Suite * " + this.suiteName + "(void)\n";
		testSuite += "{\n";
		testSuite += "Suite *s;\n";
		testSuite += "TCase *temp_tc;\n";
		testSuite += "\n";
		testSuite += "s = suite_create(\"" + this.suiteName + "\");";
		testSuite += "\n\n";
		
		for (TestCaseWriter testCaseWriter : this.testSuite) {
			testSuite += "temp_tc = tcase_create(\"" + testCaseWriter.getName() + "\");\n";
			testSuite += "tcase_add_test(temp_tc, " + testCaseWriter.getName() + ");\n";
			testSuite += "suite_add_tcase(s, temp_tc);\n\n";
		}
		
		testSuite += "return s;\n";
		testSuite += "}\n\n";

		
		return testSuite;
	}
	
	private String writeMain() {
		String main = "";
		
		main += "int main(void) {\n";
		main += "int number_failed;\n";
		main += "Suite *s;\n";
		main += "SRunner *sr;\n\n";
		main += "s = " + this.suiteName + "();\n";
		main += "sr = srunner_create(s);\n\n";
		main += "srunner_run_all(sr, CK_NORMAL);\n";
		main += "number_failed = srunner_ntests_failed(sr);\n";
		main += "srunner_free(sr);\n";
		main += "return (number_failed == 0) ? EXIT_SUCCESS : EXIT_FAILURE;\n";
		main += "}";
		
		return main;
	}
}
