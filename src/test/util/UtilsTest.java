package test.util;

import it.unisa.ocelot.util.Utils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void readFile() throws Exception {
        String reads = Utils.readFile("src/test/resources/test_read.h");
        assertEquals("// testing reading from file", reads);
    }

    @Test
    public void arrayContains() throws Exception {
        String[] list1 = {"elem1", "elem2", "elem3"};
        String elem1 = "elem3";
        int elem2 = 1;
        boolean flag = Utils.arrayContains(list1, elem1);
        assertEquals(true, flag);
        flag = Utils.arrayContains(list1, elem2);
        assertEquals(false, flag);
    }

    @Test
    public void writeFile() throws Exception {
        String destination = "src/test/resources/test_aux.h";
        Utils.writeFile(destination, "test string");
        String reads = Utils.readFile(destination);
        assertEquals("test string", reads);
        File file = new File(destination);
        file.delete();
    }

    @Test
    public void printParameters() throws Exception {
        Object[][][] parameters = {
                {{0.41,0.41,0.41,0.97}},
                {{0.0, 0.0, 0.0}},
                {{1,1,1}}
        };
        String res = Utils.printParameters(parameters);
        assertNotNull(res);
    }
}