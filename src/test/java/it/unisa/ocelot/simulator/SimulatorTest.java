package it.unisa.ocelot.simulator;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.dominators.Dominators;
import it.unisa.ocelot.c.cfg.edges.FalseEdge;
import it.unisa.ocelot.c.cfg.edges.FlowEdge;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.c.cfg.edges.TrueEdge;
import it.unisa.ocelot.c.cfg.nodes.CFGNode;
import it.unisa.ocelot.genetic.edges.EdgeDistanceListener;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test the simulation on the CFG to calculate the fitness function
 * @author Giovanni Grano
 */
public class SimulatorTest {

    private static CFG cfg;

    private static CFGNode start;
    private static CFGNode n1;
    private static CFGNode n2;
    private static CFGNode n3;
    private static CFGNode end;

    private static LabeledEdge target;

    private static Set<CFGNode> dominatorNodes;

    @BeforeClass
    public static void setUp() throws Exception {
        start = new CFGNode("start");
        n1 = new CFGNode("a==5");
        n2 = new CFGNode("b==1");
        n3 = new CFGNode("b==2");
        end = new CFGNode("end");

        cfg = new CFG();
        cfg.addVertex(start);
        cfg.addVertex(n1);
        cfg.addVertex(n2);
        cfg.addVertex(n3);

        cfg.addVertex(end);

        cfg.setStart(start);
        cfg.setEnd(end);


        cfg.addEdge(start, n1, new FlowEdge());
        cfg.addEdge(n1, n2, new TrueEdge());
        cfg.addEdge(n1, n3, target=new FalseEdge());
        cfg.addEdge(n2, end, new FlowEdge());
        cfg.addEdge(n3, end, new FlowEdge());

//        this.target = cfg.getBranchesFromCFG().stream()
//                .filter(e ->
//                e.getID() == targetBranch.getID()).collect(Collectors.toList()).get(0);

        Dominators<CFGNode, LabeledEdge> dominators = new Dominators<>(cfg, cfg.getStart());
        CFGNode parent = cfg.getEdgeSource(target);
        dominatorNodes = dominators.getStrictDominators(parent);
    }

    @Test
    public void simulate() throws Exception {
        EdgeDistanceListener listener = new EdgeDistanceListener(cfg, target, dominatorNodes);
        EventsHandler events = new EventsHandler();
        events.add(1, 0.0, 1.5);
        Simulator simulator = new Simulator(cfg, events.getEvents());
        simulator.addListener(listener);
        simulator.simulate();
        double branchDistance = listener.getNormalizedBranchDistance();
        int approachLevel = listener.getApproachLevel();
        assertEquals(0.6, branchDistance, 0.0);
        assertEquals( 0, approachLevel);

        events = new EventsHandler();
        events.add(0, 1.5, 0.0);
        simulator = new Simulator(cfg, events.getEvents());
        simulator.addListener(listener);
        simulator.simulate();
        branchDistance = listener.getNormalizedBranchDistance();
        approachLevel = listener.getApproachLevel();
        assertEquals(0.0, branchDistance, 0.0);
        assertEquals( 0, approachLevel, 0);

        int currentEventIndex = Whitebox.getInternalState(simulator, "currentEventIndex");
        boolean correct = currentEventIndex == events.getEvents().size();
        assertEquals(correct, simulator.isSimulationCorrect());
    }

    @Test
    public void addListener() throws Exception {
        EdgeDistanceListener listener = new EdgeDistanceListener(cfg, target, dominatorNodes);
        EventsHandler events = new EventsHandler();
        events.add(1, 0.0, 1.5);
        Simulator simulator = new Simulator(cfg, events.getEvents());
        simulator.setListener(listener);
        List<SimulatorListener> listeners = Whitebox.getInternalState(simulator, "listeners");
        assertEquals(1, listeners.size());
    }
}