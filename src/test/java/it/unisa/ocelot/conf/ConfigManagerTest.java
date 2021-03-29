package it.unisa.ocelot.conf;

import org.apache.commons.lang3.Range;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class ConfigManagerTest {

    public ConfigManager config;

    @Before
    public void setUp() throws IOException {
        ConfigManager.setFilename("resources/tests/test.properties");
        config = ConfigManager.getInstance();
    }

    @Test
    public void getPopulationSize() throws Exception {
        int populationSize = config.getPopulationSize();
        assertNotNull(populationSize);
        assertEquals(100, populationSize);
    }

    @Test
    public void getMaxEvaluations() throws Exception {
        int max = config.getMaxEvaluations();
        assertNotNull(max);
        assertEquals(200000, max);
    }

    @Test
    public void getCrossoverProbability() throws Exception {
        double crossover = config.getCrossoverProbability();
        assertNotNull(crossover);
        assertEquals(0.9, crossover, 0.0);
    }

    @Test
    public void getMutationProbability() throws Exception {
        double mutationProbability = config.getMutationProbability();
        assertNotNull(mutationProbability);
        assertEquals(1.2, mutationProbability, 0.0);
    }

    @Test
    public void isDynamicMutation() throws Exception {
        boolean isDynamic = config.isDynamicMutation();
        assertNotNull(isDynamic);
        assertEquals(true, isDynamic);
    }

    @Test
    public void getThreads() throws Exception {
        int threads = config.getThreads();
        assertNotNull(threads);
        assertEquals(1, threads);
    }

    @Test
    public void getTestFilename() throws Exception {
        String filename = config.getTestFilename();
        assertNotNull(filename);
        assertEquals("testobject/tesi.c", filename);
    }

    @Test
    public void getTestFunction() throws Exception {
        String testFunction = config.getTestFunction();
        assertNotNull(testFunction);
        assertEquals("ptrtest", testFunction);
    }


    @Test
    public void getTestRanges() throws Exception {
        Range<Double>[] ranges = config.getTestRanges();
        assertNotNull(ranges);
        assertEquals(4, ranges.length);
    }

    @Test
    public void getTestIncludePaths() throws Exception {
        String[] includes = config.getTestIncludePaths();
        assertNotNull(includes);
        assertEquals(4, includes.length);
        assertEquals("testobject/gimp/", includes[1]);
    }

    @Test
    public void getOutputFolder() throws Exception {
        String output = config.getOutputFolder();
        assertNotNull(output);
        assertEquals("./outputs/", output);
    }

    @Test
    public void getPrintResults() throws Exception {
        boolean print = config.getPrintResults();
        assertNotNull(print);
        assertEquals(true, print);
    }

    @Test
    public void getResultsFolder() throws Exception {
        String folder = config.getResultsFolder();
        assertNotNull(folder);
        assertEquals("RESULTS/GASSBSE_FULL", folder);
    }

    @Test
    public void getExperimentRuns() throws Exception {
        int runs = config.getExperimentRuns();
        assertNotNull(runs);
        assertEquals(30, runs);
    }

    @Test
    public void getTestBasedir() throws Exception {
        String basedir = config.getTestBasedir();
        assertNotNull(basedir);
        assertEquals("testobject/", basedir);
    }

    @Test
    public void getDebug() throws Exception {
        boolean debug = config.getDebug();
        assertNotNull(debug);
        assertEquals(false, debug);
    }

    @Test
    public void getUI() throws Exception {
        boolean ui = config.getUI();
        assertNotNull(ui);
        assertEquals(false, ui);
    }

    @Test
    public void getTestSuiteGenerator() throws Exception {
        String suite = config.getTestSuiteGenerator();
        assertNotNull(suite);
        assertEquals("AllEdges", suite);
    }

    @Test
    public void getTestSuiteMinimizer() throws Exception {
        String minimizer = config.getTestSuiteMinimizer();
        assertNotNull(minimizer);
        assertEquals("AdditionalGreedy", minimizer);
    }

    @Test
    public void getReducedMcCabeCoverageTimes() throws Exception {
        int mc = config.getReducedMcCabeCoverageTimes();
        assertNotNull(mc);
        assertEquals(1, mc);
    }

    @Test
    public void getRandomSizeLimit() throws Exception {
        int limit = config.getRandomSizeLimit();
        assertNotNull(limit);
        assertEquals(1000000, limit);
    }

    @Test
    public void getRandomTimeLimit() throws Exception {
        int limit = config.getRandomTimeLimit();
        assertNotNull(limit);
        assertEquals(-1, limit);
    }

    @Test
    public void getRandomGranularity() throws Exception {
        int granularity = config.getRandomGranularity();
        assertNotNull(granularity);
        assertEquals(1000, granularity);
    }

    @Test
    public void getRequiredCoverage() throws Exception {
        Double coverage = config.getRequiredCoverage();
        assertNotNull(coverage);
        assertEquals(1.1, coverage, 0.0);
    }

    @Test
    public void getTestArraysSize() throws Exception {
        int size = config.getTestArraysSize();
        assertNotNull(size);
        assertEquals(1, size);
    }

    @Test
    public void getAlgorithm() throws Exception {
        String algorithm = config.getAlgorithm();
        assertNotNull(algorithm);
        assertEquals("GeneticAlgorithm", algorithm);
    }

    @Test
    public void getAvmEpsilon() throws Exception {
        double epsilon = config.getAvmEpsilon();
        assertNotNull(epsilon);
        assertEquals(1, epsilon,0.0);
    }

    @Test
    public void getAvmDelta() throws Exception {
        double epsilon = config.getAvmDelta();
        assertNotNull(epsilon);
        assertEquals(1, epsilon,0.0);
    }

    @Test
    public void getSerendipitousCoverage() throws Exception {
        boolean serendipitous = config.getSerendipitousCoverage();
        assertNotNull(serendipitous);
        assertEquals(false, serendipitous);
    }

    @Test
    public void getBudgetManager() throws Exception {
        String budget = config.getBudgetManager();
        assertNotNull(budget);
        assertEquals("Basic", budget);
    }

    @Test
    public void getCascadeGenerators() throws Exception {
        List<String> cascade = config.getCascadeGenerators();
        assertNotNull(cascade);
        assertEquals(2, cascade.size());
    }

    @Test
    public void isMetaMutatorEnabled() throws Exception {
        boolean meta = config.isMetaMutatorEnabled();
        assertNotNull(meta);
        assertEquals(false, meta);
    }

    @Test
    public void setProperty() throws Exception {
        config.setProperty("test.link", "test");
        assertEquals("test", config.getTestLink()[0]);
    }

    @Test
    public void getTestLink() throws Exception {
        String[] link = config.getTestLink();

        assertEquals(0, link.length);
    }

    @Test
    public void isDMCSeed() throws Exception {
        boolean seed = config.isDMCSeed();
        assertNotNull(seed);
        assertEquals(true, seed);
    }

    @Test
    public void getDMCSeedSize() throws Exception {
        int seed = config.getDMCSeedSize();
        assertNotNull(seed);
        assertEquals(50, seed);
    }

    @Test
    public void getExperimentGenerators() throws Exception {
        String[] generators = config.getExperimentGenerators();
        assertNotNull(generators);
        assertEquals(2, generators.length);
    }

    @Test
    public void isExperimentMinimization() throws Exception {
        boolean minimized = config.isExperimentMinimization();
        assertNotNull(minimized);
        assertEquals(true, minimized);
    }

    @Test
    public void getJavaHome() throws Exception {
        String javahome = config.getJavaHome();
        assertNotNull(javahome);
        assertEquals("/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home", javahome);
    }

    @Test
    public void getSystemInclude() throws Exception {
        String include = config.getSystemInclude();
        assertNotNull(include);
        assertEquals("/usr/include", include);
    }

    @Test
    public void getGlib2Paths() throws Exception {
        String[] glibs = config.getGlib2Paths();
        assertNotNull(glibs);
        assertEquals(2, glibs.length);
    }

    @Test
    public void getJavaPaths() throws Exception {
        String[] javas = config.getJavaPaths();
        assertNotNull(javas);
        assertEquals(2, javas.length);
    }

    @Test
    public void getCFlags() throws Exception {
        String flags = config.getCFlags();
        assertNotNull(flags);
        assertEquals("-g -shared -fpic", flags);
    }

    @Test
    public void getMakeCommand() throws Exception {
        String make = config.getMakeCommand();
        assertNotNull(make);
        assertEquals("make", make);
    }

    @Test
    public void getMoreOptions() throws Exception {
        String options = config.getMoreOptions();
        assertNotNull(options);
        assertEquals("", options);
    }

}