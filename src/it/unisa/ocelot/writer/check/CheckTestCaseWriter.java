package it.unisa.ocelot.writer.check;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import it.unisa.ocelot.c.instrumentor.CallGeneratorVisitor;
import it.unisa.ocelot.c.instrumentor.ExternalReferencesVisitor;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.writer.TestCaseWriter;
import it.unisa.ocelot.writer.TestWritingException;

public class CheckTestCaseWriter extends TestCaseWriter {
	private ConfigManager config;
	private IASTTranslationUnit translationUnit;
	
	public CheckTestCaseWriter(int id) {
		this.setName("ocelot_testcase" + id);
	}
	
	public void setTranslationUnit(IASTTranslationUnit translationUnit) {
		this.translationUnit = translationUnit;
	}
	
	@Override
	public String write(ConfigManager pConfigManager) throws TestWritingException {
		this.config = pConfigManager;
		String testCase = "";
		
		testCase += "START_TEST(" + this.getName() + ")\n";
		testCase += "{\n";
		
		testCase += this.writeInitialization();
		
		testCase += this.writeFunctionCall();
		
		testCase += "\n";
		testCase += "/* REPLACE THE ASSERTION BELOW */\n";
		testCase += "ck_assert_str_eq(\"OK\", \"OK\");\n";
		testCase += "}\n";
		testCase += "END_TEST\n\n";
		
		return testCase;
	}
	
	private String writeInitialization() {
		String initialization = "";
		
		Object[] values = this.testCase.getParameters()[0][0];
		Object[][] arrays = this.testCase.getParameters()[1];
		Object[] pointers = this.testCase.getParameters()[2][0];
		
		for (int i = 0; i < values.length; i++) {
			initialization += "double __val"+i+" = " + values[i] + ";\n";
		}
		initialization += "\n";
		
		for (int i = 0; i < arrays.length; i++) {
			String stringArray = Arrays.toString(arrays[i]);
			stringArray = stringArray.replace('[', '{');
			stringArray = stringArray.replace(']', '}');
			initialization += "double __array"+i+"[" + arrays[i].length + "] = " + stringArray + ";\n";
		}
		initialization += "\n";
		
		for (int i = 0; i < pointers.length; i++) {
			initialization += "void* __ptr" + i + " = &__array" + pointers[i] + ";\n";
		}
		initialization += "\n";
		
		return initialization;
	}
	
	private String writeFunctionCall() throws TestWritingException {
		ExternalReferencesVisitor referenceVisitor = new ExternalReferencesVisitor(config.getTestFunction());
		translationUnit.accept(referenceVisitor);
		
		CallGeneratorVisitor callGeneratorVisitor = new CallGeneratorVisitor(config.getTestFunction(), referenceVisitor.getExternalReferences());
		translationUnit.accept(callGeneratorVisitor);
		
		return callGeneratorVisitor.getCall();
	}
}
