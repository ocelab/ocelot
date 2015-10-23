package it.unisa.ocelot.suites.generators;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.suites.TestSuiteGenerationException;
import it.unisa.ocelot.suites.budget.BasicBudgetManager;
import it.unisa.ocelot.suites.budget.BudgetManager;
import it.unisa.ocelot.suites.budget.BudgetManagerHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Test suite generator which combines multiple generators in cascade. The generators must
 * implement CascadeableGenerator. 
 * @author simone
 *
 */
public class CascadeTestSuiteGenerator extends TestSuiteGenerator {
	private List<TestSuiteGenerator> generators;
	
	
	public CascadeTestSuiteGenerator(ConfigManager pConfigManager, CFG pCFG) {
		super(pCFG);
		this.generators = new ArrayList<TestSuiteGenerator>();
		this.config = pConfigManager;
	}
	
	public void addTestSuiteGenerator(TestSuiteGenerator pGenerator) {
		this.generators.add(pGenerator);
	}
	
	public Set<TestCase> generateTestSuite(Set<TestCase> pSuite) throws TestSuiteGenerationException {
		Set<TestCase> suite = new HashSet<>(pSuite);
		
		int splitSize = 0;
		for (TestSuiteGenerator generator : this.generators) {
			if (!(generator instanceof CascadeableGenerator))
				throw new TestSuiteGenerationException("Error: non-cascadeable generator " + generator.getClass().getSimpleName());
			
			if (generator.needsBudget())
				splitSize++;
		}
		
		this.setupBudgetManager(splitSize);
		try {
			this.budgetManager = this.budgetManager.changeTo(BasicBudgetManager.class);
		} catch (InstantiationException e) {
			throw new TestSuiteGenerationException(e.getMessage());
		}
		
		this.startBenchmarks();
		for (TestSuiteGenerator generator : this.generators) {
			if (generator.needsBudget())
				generator.setFixedBudget(this.budgetManager.getExperimentBudget(null));
			suite = generator.generateTestSuite(suite);
			if (generator.needsBudget())
				this.budgetManager.reportConsumedBudget(null, generator.budgetManager.getConsumedBudget());
			
			this.measureBenchmarks(generator.getClass().getSimpleName(), suite, 0);
			
			CascadeableGenerator cascadeable = (CascadeableGenerator)generator;
			if (cascadeable.isSatisfied())
				return suite;
		}
		
		return suite;
	}
}
