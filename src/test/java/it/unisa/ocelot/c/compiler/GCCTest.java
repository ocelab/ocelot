package it.unisa.ocelot.c.compiler;

import it.unisa.ocelot.util.Utils;
import org.apache.commons.io.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
/**
 * @author Simone Scalabrino.
 */
public class GCCTest {
    @Test
    public void testTranslationUnit() throws IOException {
        File testFilename = new File("__tmptestGCCTest.c");
        try {
            assertFalse(testFilename.exists());

            String helloWorld =
                    "#include<stdio.h>\n" +
                    "main()\n" +
                    "{\n" +
                    "    printf(\"Hello World\");\n" +
                    "}";

            FileUtils.writeStringToFile(testFilename, helloWorld);

            IASTTranslationUnit unit = GCC.getTranslationUnit(testFilename.getCanonicalPath());

            assertNotNull(unit);

            assertEquals(1, unit.getDeclarations().length);
            assertTrue(unit.getDeclarations()[0] instanceof CASTFunctionDefinition);
            assertTrue(((CASTFunctionDefinition) unit.getDeclarations()[0]).getBody() instanceof CASTCompoundStatement);
            assertEquals(1, ((CASTCompoundStatement) ((CASTFunctionDefinition) unit.getDeclarations()[0]).getBody()).getStatements().length);
        } catch (CoreException e) {
            e.printStackTrace();
        } finally {
            if (testFilename.exists())
                assertTrue(testFilename.delete());
        }
    }
}