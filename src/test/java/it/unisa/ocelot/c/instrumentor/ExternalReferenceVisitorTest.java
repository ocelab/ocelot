package it.unisa.ocelot.c.instrumentor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.util.Utils;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExternalReferenceVisitorTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	private IASTTranslationUnit writeTU(String code) throws Exception {
		String testFilename = "__TEST";
		
		Utils.writeFile(testFilename, code);
		
		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(
                testFilename,
				new String[] {}).copy();
		
		return translationUnit;
	}
	
	@Test
	public void basicTest() {
		try {
			String testfile = 
					"int c;\n"
					+ "typedef int gint;\n"
					+ "int test() {\n"
					+ "	int a;\n"
					+ " int b;\n"
					+ " int zz = a+b+c;\n"
					+ "}";
			
			
			IASTTranslationUnit translationUnit = writeTU(testfile);
			
			ExternalReferencesVisitor visitor = new ExternalReferencesVisitor("test");
			
			translationUnit.accept(visitor);
			
			Set<String> oracle = new HashSet<>();
			
			oracle.add("c");
			
			junit.framework.Assert.assertEquals(oracle, visitor.getExternalReferences().keySet());
		} catch (Exception e) {
			junit.framework.Assert.assertFalse(true);
		}
	}

	@Test
	public void moreFunctionsTest() {
		try {
			String testfile = 
					"int c;"
					+ "int z;"
					+ "typedef int gint;"
					+ "int test() {"
					+ "	int a;"
					+ " int b;"
					+ " int zz = a+b+c;"
					+ "}\n"
					+ "void test2() {"
					+ "gint a;"
					+ "c = a-z;"
					+ "}";
			
			
			IASTTranslationUnit translationUnit = writeTU(testfile);
			
			ExternalReferencesVisitor visitor = new ExternalReferencesVisitor("test2");
			
			translationUnit.accept(visitor);
			
			Set<String> oracle = new HashSet<>();
			
			oracle.add("c");
			oracle.add("z");
			
			junit.framework.Assert.assertEquals(oracle, visitor.getExternalReferences().keySet());
		} catch (Exception e) {
			junit.framework.Assert.assertFalse(true);
		}
	}
	
	@Test
	public void typesTest() {
		try {
			String testfile = 
					"int c;"
					+ "int z;"
					+ "typedef int gint;"
					+ "int test() {"
					+ "	int a;"
					+ " int b;"
					+ " int zz = a+b+c;"
					+ "}\n"
					+ "void test2() {"
					+ "gint a;"
					+ "c = a-z;"
					+ "}";
			
			
			IASTTranslationUnit translationUnit = writeTU(testfile);
			
			ExternalReferencesVisitor visitor = new ExternalReferencesVisitor("test2");
			
			translationUnit.accept(visitor);
			
			Map<String, String> oracle = new HashMap<String, String>();
			
			oracle.put("c", "int");
			oracle.put("z", "int");
			
			Map<String, IType> result = visitor.getExternalReferences();
			Map<String, String> artResult = new HashMap<>();
			for (Entry<String, IType> entry : result.entrySet()) {
				String typeName = entry.getValue().toString();
				artResult.put(entry.getKey(), typeName);
			}
			
			junit.framework.Assert.assertEquals(oracle, artResult);
		} catch (Exception e) {
			junit.framework.Assert.assertFalse(true);
		}
	}
	
	@Test
	public void typesPointersTest() {
		try {
			String testfile = 
					"int *c;"
					+ "int z;"
					+ "typedef int gint;"
					+ "int test() {"
					+ "	int a;"
					+ " int b;"
					+ " int zz = a+b+*c;"
					+ "}\n"
					+ "void test2() {"
					+ "gint a;"
					+ "c = a-z;"
					+ "}";
			
			
			IASTTranslationUnit translationUnit = writeTU(testfile);
			
			ExternalReferencesVisitor visitor = new ExternalReferencesVisitor("test2");
			
			translationUnit.accept(visitor);
			
			Map<String, String> oracle = new HashMap<String, String>();
			
			oracle.put("c", "int *");
			oracle.put("z", "int");
			
			Map<String, IType> result = visitor.getExternalReferences();
			Map<String, String> artResult = new HashMap<>();
			for (Entry<String, IType> entry : result.entrySet()) {
				String typeName = entry.getValue().toString();
				artResult.put(entry.getKey(), typeName);
			}
			
			junit.framework.Assert.assertEquals(oracle, artResult);
		} catch (Exception e) {
			junit.framework.Assert.assertFalse(true);
		}
	}
}
