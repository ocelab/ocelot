package it.unisa.ocelot.writer.check;

import java.util.UUID;

import it.unisa.ocelot.writer.TestCaseWriter;
import it.unisa.ocelot.writer.TestSuiteWriter;

public class CheckTestSuiteWriter extends TestSuiteWriter<CheckTestCaseWriter> {
	private String suiteName;
	
	@Override
	public String write() {
		this.suiteName = "ocelot_generated_" + UUID.randomUUID();
		
		String suite = "";
		suite += "#include <stdlib.h>";
		
		suite += writeHeaders();
		
		suite += writeTestCases();
		
		suite += writeTestSuite();
		
		suite += writeMain();

		
		return suite;
	}
	
	private String writeHeaders() {
		String headers = "";
		
		headers += "#include <stdlib.h>\n";
		//TODO INSERT TEST FUNCTION PROTOTYPE HERE
		
		return headers;
	}
	
	private String writeTestCases() {
		String testCases = "";
		
		for (TestCaseWriter testCaseWriter : this.testSuite) {
			testCases += testCaseWriter.write();
			testCases += "\n";
		}
		
		return testCases;
	}
	
	private String writeTestSuite() {
		String testSuite = "";
		
		testSuite += "Suite * " + this.suiteName + "(void)\n";
		testSuite += "{\n";
		testSuite += "   Suite *s;\n";
		testSuite += "   TCase *temp_tc;\n";
		testSuite += "\n";
		testSuite += "   s = suite_create(\"" + this.suiteName + "\");";
		testSuite += "\n\n";
		
		for (TestCaseWriter testCaseWriter : this.testSuite) {
			testSuite += "temp_tc = tcase_create(\"" + testCaseWriter.getName() + "\");\n";
			testSuite += "tcase_add_test(temp_tc, " + testCaseWriter.getName() + ");\n";
			testSuite += "suite_add_tcase(s, temp_tc);\n\n";
		}
		
		testSuite += "   return s;";
		testSuite += "}\n\n";

		
		return testSuite;
	}
	
	private String writeMain() {
		String main = "";
		
		main += "int main(void) {";
		main += "	int number_failed;\n";
		main += "	Suite *s;\n";
		main += "	SRunner *sr;\n\n";
		main += "	s = " + this.suiteName + "();\n";
		main += "	sr = srunner_create(s);\n\n";
		main += "	srunner_run_all(sr, CK_NORMAL);\n";
		main += "	number_failed = srunner_ntests_failed(sr);\n";
		main += "	srunner_free(sr);\n";
		main += "	return (number_failed == 0) ? EXIT_SUCCESS : EXIT_FAILURE;\n";
		main += "}";
		
		return main;
	}
}
