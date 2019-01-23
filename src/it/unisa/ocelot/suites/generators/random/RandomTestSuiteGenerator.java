package it.unisa.ocelot.suites.generators.random;

import java.util.*;

import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;
import it.unisa.ocelot.genetic.encoding.graph.ScalarNode;
import org.apache.commons.lang3.Range;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.generators.CascadeableGenerator;
import it.unisa.ocelot.suites.generators.TestSuiteGenerator;

/**
 * Randomly generates test cases, keeping only the ones that improve coverage. The maximum number of generations
 * is set in the config file.
 * @author simone
 *
 */
public class RandomTestSuiteGenerator extends TestSuiteGenerator implements CascadeableGenerator {
	private Random random;
	private Range<Double>[] ranges;
	private boolean satisfied;
	
	public RandomTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		super(pCFG);
		this.config = pConfigManager;
		this.random = new Random();
		this.ranges = this.config.getTestRanges();
	}
	
	@Override
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite) throws TestSuiteGenerationException {
		Set<TestCase> suite = new HashSet<>(pSuite);
		
		this.startBenchmarks();
		
		calculator.calculateCoverage(suite);
		
		this.generateRandomSuite(suite);
		
		calculator.calculateCoverage(suite);
		if (calculator.getBranchCoverage() >= this.config.getRequiredCoverage()) {
			this.satisfied = true;
		}
		
		return suite;
	}
	
	public boolean isSatisfied() {
		return satisfied;
	}
	
	private void generateRandomSuite(Set<TestCase> suite) throws TestSuiteGenerationException {
		long time = 0;
		long timeout = System.currentTimeMillis() + this.config.getRandomTimeLimit()*1000;
		if (this.config.getRandomTimeLimit() < 0)
			timeout = Long.MAX_VALUE;
		
		int sizeout = config.getRandomSizeLimit();
		if (sizeout < 0)
			sizeout = Integer.MAX_VALUE;
		
		double lastCoverage = 0.0;
		while (calculator.getBranchCoverage() < this.config.getRequiredCoverage() &&
				suite.size() <= sizeout &&
				time < timeout) {
			coverRandom(suite);
			calculator.calculateCoverage(suite);
			
			if (calculator.getBranchCoverage() > lastCoverage) {
				this.measureBenchmarks("Random", suite, null);
				lastCoverage = calculator.getBranchCoverage();
				
				this.println(calculator.getBranchCoverage());
				this.println(suite.size());
				this.printSeparator();
			}
			if (suite.size() % 10000 == 0) {
				this.print("Temporary size: ");
				this.println(suite.size());
			}
			
			time = System.currentTimeMillis();
			this.println("Time: " + (timeout-time));
		}
		
		this.measureBenchmarks("End", suite, null);
	}
	
	private void coverRandom(Set<TestCase> suite) throws TestSuiteGenerationException {
		for (int i = 0; i < this.config.getRandomGranularity(); i++) {
			//Object[][][] numericParams = random(cfg.getParameterTypes());
			Graph graph = random(cfg.getTypeGraph());
			TestCase testCase = this.createTestCase(graph, suite.size());
			suite.add(testCase);
		}
	}
	
	//TODO Make this method work. Now it doesn't, due to the major change of parameters

	/**
	 * This function write random values into ScalarNode
	 * @param typeGraph
	 * @return
	 */
	private Graph random(Graph typeGraph) {
		List<Node> nodes = new ArrayList<>();

		for (Node node : typeGraph.getNodes()) {
			if (node instanceof ScalarNode) {
				ScalarNode tmpNode = new ScalarNode(node.getId(), node.getCType());

				double param = random.nextDouble() * (ranges[0].getMaximum() - ranges[0].getMinimum());
				param += ranges[0].getMinimum();
				tmpNode.setValue(param);

				nodes.add(tmpNode);
			}
		}


		Graph graph = null;
		try {
			graph = (Graph) cfg.getTypeGraph().clone();
		} catch (CloneNotSupportedException e) {
			System.err.println("Impossible to clone Graph");;
		}

		for (int i = 0; i < graph.getNodes().size(); i++) {
			int j = 0;
			while (j < nodes.size() && nodes.get(j).equals(graph.getNode(i))) j++;

			((ScalarNode)graph.getNode(i)).setValue(((ScalarNode)nodes.get(j)).getValue());
		}
		
		return graph;
	}
	
	@Override
	public int getNumberOfEvaluations() {
		// TODO Auto-generated method stub
		return 0;
	}
}
