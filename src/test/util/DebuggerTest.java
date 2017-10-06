package test.util;

import it.unisa.ocelot.util.Debugger;
import it.unisa.ocelot.util.Utils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Utils.class)
public class DebuggerTest {
    private Debugger debugger;

    @Before
    public void setUp() throws Exception {
        debugger = new Debugger();
    }

    @Test
    public void write() throws Exception {
        debugger.write("test_line");
        String test = Whitebox.getInternalState(debugger, "content");
        assertEquals("test_line\n", test);
    }

    @Test
    public void trace() throws Exception {
        PowerMockito.spy(Debugger.class);
        Debugger.trace("test_2");
        Map<String, Integer> trace = Whitebox.getInternalState(Debugger.class, "traceMap");

        assertEquals(1, trace.get("test_2").intValue());
    }

    @Test
    public void save() throws Exception {
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.doNothing().when(Utils.class, "writeFile","src/test/resources/test.h","test");
    }

    @Test
    public void clear() throws Exception {
        debugger.write("test_line");
        debugger.clear();
        String test = Whitebox.getInternalState(debugger, "content");
        assertEquals("", test);
    }

}