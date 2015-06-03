package jmetal.operators.mutation;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import jmetal.core.Solution;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.wrapper.XReal;

/**
 * This mutation operator provides a classic (specified) mutation with a certain probability, and, instead, provides
 * a mutation in which the candidate is substituted by a constant in the program. 
 * @author simone
 *
 */
public class ConstantMetaMutation extends Mutation {
	private double mutationProbability_;
	private double metaMutationProbability_;
	private List<Double> mutationElements_;
	private Mutation realOperator_;
	
	public ConstantMetaMutation(HashMap<String, Object> pParameters) {
		super(pParameters);
		
		if (pParameters.get("probability") != null)
	  		mutationProbability_ = (Double) pParameters.get("probability") ;  		
	  	if (pParameters.get("metaMutationProbability") != null)
	  		metaMutationProbability_ = (Double) pParameters.get("metaMutationProbability") ;
	  	if (pParameters.get("mutationElements") != null)
	  		mutationElements_ = (List<Double>) pParameters.get("mutationElements") ;
	  	if (pParameters.get("realOperator") != null)
	  		realOperator_ = (Mutation)pParameters.get("realOperator");
	}
	
	public void doMutation(double probability, Solution solution) throws JMException {        
		XReal x = new XReal(solution) ;		
		for (int var=0; var < solution.numberOfVariables(); var++) {
			if (PseudoRandom.randDouble() <= metaMutationProbability_) {
				double y = x.getValue(var);
				if (this.mutationElements_.size() > 0) {
					int randIndex = PseudoRandom.randInt(0, this.mutationElements_.size()-1);
				
					x.setValue(var, this.mutationElements_.get(randIndex));
				}
			} else
				this.realOperator_.execute(solution);
		} // for

	} // doMutation


	@Override
	public Object execute(Object object) throws JMException {
		Solution solution = (Solution)object;

		doMutation(mutationProbability_, solution);
		return solution;
	}
	
}
