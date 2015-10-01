package it.unisa.ocelot.suites.generators;

import it.unisa.ocelot.TestCase;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.suites.TestSuiteGenerationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		
		this.startBenchmarks();
		for (TestSuiteGenerator generator : this.generators) {
			suite = generator.generateTestSuite(suite);
			
			this.measureBenchmarks(generator.getClass().getSimpleName(), suite, 0);
			
			if (generator instanceof CascadeableGenerator) {
				CascadeableGenerator cascadeable = (CascadeableGenerator)generator;
				if (cascadeable.isSatisfied())
					return suite;
			} else
				throw new TestSuiteGenerationException("Error: non-cascadeable generator " + generator.getClass().getSimpleName());
		}
		
		return suite;
	}
}
