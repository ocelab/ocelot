package it.unisa.ocelot.writer.check;

import java.util.Arrays;

import it.unisa.ocelot.writer.TestCaseWriter;

public class CheckTestCaseWriter extends TestCaseWriter {
	public CheckTestCaseWriter(int id) {
		this.setName("ocelot_testcase" + id);
	}
	@Override
	public String write() {
		String testCase = "";
		
		testCase += "START_TEST(" + this.getName() + ")\n";
		testCase += "{\n";
		
		testCase += this.writeInitialization();
		
		testCase += this.writeFunctionCall();
		
		testCase += "\n";
		testCase += "	 /* REPLACE THE ASSERTION BELOW */";
		testCase += "    ck_assert_str_eq(\"OK\", \"OK\");\n";
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
			initialization += "double* __ptr" + i + " = &__array" + pointers[i] + ";\n";
		}
		initialization += "\n";
		
		return initialization;
	}
	
	private String writeFunctionCall() {
		String functionCall = "";
		
		return functionCall;
	}
}
