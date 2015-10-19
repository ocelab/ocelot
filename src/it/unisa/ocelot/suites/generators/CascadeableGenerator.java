package it.unisa.ocelot.suites.generators;

/**
 * Interface that has to be implemented by all generators which can be put in cascade
 * @author simone
 *
 */
public interface CascadeableGenerator {
	public boolean isSatisfied();
}
