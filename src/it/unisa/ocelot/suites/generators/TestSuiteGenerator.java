package it.unisa.ocelot.suites.generators;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.edges.LabeledEdge;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.genetic.OcelotExperiment;
import it.unisa.ocelot.genetic.VariableTranslator;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;
import it.unisa.ocelot.genetic.encoding.graph.ScalarNode;
import it.unisa.ocelot.genetic.encoding.manager.GraphGenerator;
import it.unisa.ocelot.genetic.encoding.manager.GraphManager;
import it.unisa.ocelot.simulator.CoverageCalculator;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.benchmarks.BenchmarkCalculator;
import it.unisa.ocelot.suites.budget.BudgetManager;
import it.unisa.ocelot.suites.budget.BudgetManagerHandler;

import java.util.*;

import jmetal.core.Solution;
import jmetal.encodings.variable.Int;
import jmetal.util.JMException;
import org.apache.commons.lang3.Range;

public abstract class TestSuiteGenerator {
	@SuppressWarnings("rawtypes")
	protected List<BenchmarkCalculator> benchmarkCalculators;
	protected ConfigManager config;
	public CoverageCalculator calculator;
	public CFG cfg;
	protected BudgetManager budgetManager;
	private int fixedBudget;

	private Range<Double>[] ranges;

	protected ArrayList<Graph> graphList;
	protected Map<Integer, Integer> scalarNodeIndexMap;
	
	@SuppressWarnings("rawtypes")
	public TestSuiteGenerator(CFG pCFG) {
		this.benchmarkCalculators = new ArrayList<BenchmarkCalculator>();
		this.cfg = pCFG;
		this.calculator = new CoverageCalculator(pCFG);
		this.fixedBudget = -1;
	}

	public TestSuiteGenerator(CFG pCFG, ConfigManager pConfigManager) {
		this.config = pConfigManager;
		this.ranges = this.config.getTestRanges();

		this.benchmarkCalculators = new ArrayList<BenchmarkCalculator>();
		this.cfg = pCFG;
		this.calculator = new CoverageCalculator(pCFG);
		this.fixedBudget = -1;

		this.graphList = generateStartingGraphList();


		GraphManager graphManager = new GraphManager();
		ArrayList<ScalarNode> scalarNodes = graphManager.getScalarNodes(graphList.get(0));
		this.scalarNodeIndexMap = new HashMap<>();

		for (int i = 0; i < scalarNodes.size(); i++) {
			this.scalarNodeIndexMap.put(i, scalarNodes.get(i).getId());
		}
	}
	
	@SuppressWarnings("rawtypes")
	public void addBenchmark(BenchmarkCalculator pCalculator) {
		this.benchmarkCalculators.add(pCalculator);
	}
	
	@SuppressWarnings("rawtypes")
	protected void startBenchmarks() {
		for (BenchmarkCalculator benchmarkCalculator : this.benchmarkCalculators)
			benchmarkCalculator.start();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void measureBenchmarks(String pLabel, Set<TestCase> pSuite, Integer evaluations) {
		for (BenchmarkCalculator benchmarkCalculator : this.benchmarkCalculators)
			benchmarkCalculator.measure(pLabel, pSuite, evaluations);
	}
	
	@SuppressWarnings("rawtypes")
	protected void removeLastBenchmark() {
		for (BenchmarkCalculator benchmarkCalculator : this.benchmarkCalculators)
			benchmarkCalculator.removeLast();
	}

	public ArrayList<Graph> generateStartingGraphList () {
		ArrayList<Graph> graphList = new ArrayList<>();
		for (int i = 0; i < config.getPopulationSize(); i++) {
			Graph randomGraph = generateRandomGraph(cfg.getTypeGraph());
			graphList.add(randomGraph);
		}

		return graphList;
	}

	private Graph generateRandomGraph (Graph typeGraph) {
		Random random = new Random();

		List<Node> nodes = new ArrayList<>();

		for (Node node : typeGraph.getNodes()) {
			if (node instanceof ScalarNode) {
				ScalarNode tmpNode = new ScalarNode(node.getId(), node.getCType());

				//double param = random.nextDouble() * (ranges[0].getMaximum() - ranges[0].getMinimum());
				//param += ranges[0].getMinimum();
				double param = (random.nextDouble() * (ranges[0].getMaximum()*2)) + ranges[0].getMinimum();
				tmpNode.setValue(param);

				nodes.add(tmpNode);
			}
		}


		Graph graph = GraphGenerator.generateGraph(cfg.getParameterTypes());

		for (int i = 0; i < nodes.size(); i++) {
			int j = 0;

			while (j < graph.getNodes().size() && !nodes.get(i).equals(graph.getNode(j))) j++;

			((ScalarNode)graph.getNode(j)).setValue(((ScalarNode)nodes.get(i)).getValue());
		}

		return graph;
	}

	public Set<TestCase> generateTestSuite() throws TestSuiteGenerationException {
		Set<TestCase> suite = this.generateTestSuite(new HashSet<>());
		this.measureBenchmarks("End", suite, 0);
		return suite;
	}
	
	public abstract Set<TestCase> generateTestSuite(Set<TestCase> pSuite) throws TestSuiteGenerationException;
	
	protected void printSeparator() {
		if (this.config.getPrintResults())
			System.out
					.println("-------------------------------------------------------------------------------");
	}

	protected void print(Object pObject) {
		if (this.config.getPrintResults())
			System.out.print(pObject);
	}
	
	protected void printStat(OcelotExperiment experiment, String pStat) {
		if (experiment.getAlgorithmStats() != null)
			this.println(experiment.getAlgorithmStats().getStat(pStat));
		else
			System.err.println("No stats found! Please check the generator...");
	}

	protected void println(Object pObject) {
		if (this.config.getPrintResults())
			System.out.println(pObject);
	}

	protected List<LabeledEdge> getUncoveredEdges(Set<TestCase> suite) {
		List<LabeledEdge> uncoveredEdges = new ArrayList<LabeledEdge>(cfg.edgeSet()); 
		for (TestCase tc : suite) {
			uncoveredEdges.removeAll(tc.getCoveredEdges());
		}
		
		return uncoveredEdges;
	}

	protected TestCase createTestCase(Graph graph, int id) {
		this.calculator.calculateCoverage(graph);

		TestCase tc = new TestCase();
		tc.setId(id);
		tc.setCoveredPath(calculator.getCoveredPath());
		tc.setGraph(graph);

		return tc;
	}

	//MODIFICA NUOVA RAPPRESENTAZIONE
	/*protected TestCase createTestCase(Object[][][] pParams, int id) {
		this.calculator.calculateCoverage(pParams);
	
		TestCase tc = new TestCase();
		tc.setId(id);
		tc.setCoveredPath(calculator.getCoveredPath());
		tc.setParameters(pParams);
	
		return tc;
	}*/
	
	protected void addSerendipitousTestCases(OcelotExperiment exp, Set<TestCase> suite) {
		if (!config.getSerendipitousCoverage())
			return;
		
		Set<Solution> solutions = exp.getSerendipitousSolutions();
		
		for (Solution solution : solutions) {
			VariableTranslator translator = new VariableTranslator(this.graphList, this.scalarNodeIndexMap);
			
			//Object[][][] numericParams = translator.translateArray(cfg.getParameterTypes());
			Graph graph = translator.getGraphFromSolution(solution);

			//TestCase testCase = this.createTestCase(numericParams, suite.size());
			TestCase testCase = this.createTestCase(graph, suite.size());
			calculator.calculateCoverage(suite);
			double prevCoverage = calculator.getBranchCoverage();
			
			suite.add(testCase);
			calculator.calculateCoverage(suite);
			if (calculator.getBranchCoverage() == prevCoverage)
				suite.remove(testCase);
			else {
				this.print("Serendipitous coverage! ");
				this.measureBenchmarks("Serendipitous", suite, exp.getNumberOfEvaluation());
	
				//this.println("Parameters found: " + Utils.printParameters(numericParams));
			}
		}
	}
	
	public boolean needsBudget() {
		return true;
	}
	
	protected void setFixedBudget(int pFixedBudget) {
		this.fixedBudget = pFixedBudget;
	}
	
	public void setupBudgetManager(int pNumberOfExperiments) {
		if (this.budgetManager == null)
			if (this.fixedBudget == -1)
				this.budgetManager = BudgetManagerHandler.getInstance(config, pNumberOfExperiments);
			else
				this.budgetManager = BudgetManagerHandler.getInstance(config.getBudgetManager(), this.fixedBudget, pNumberOfExperiments);
	}
	
	public abstract int getNumberOfEvaluations();
}
