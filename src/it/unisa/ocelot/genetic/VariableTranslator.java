package it.unisa.ocelot.genetic;

import it.unisa.ocelot.c.types.CDouble;
import it.unisa.ocelot.c.types.CPointer;
import it.unisa.ocelot.c.types.CType;
import it.unisa.ocelot.genetic.encoding.graph.Edge;
import it.unisa.ocelot.genetic.encoding.graph.Graph;
import it.unisa.ocelot.genetic.encoding.graph.Node;
import it.unisa.ocelot.genetic.encoding.graph.ScalarNode;
import it.unisa.ocelot.genetic.encoding.manager.GraphGenerator;
import jmetal.core.Solution;
import jmetal.core.Variable;
import jmetal.encodings.variable.ArrayParameters;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.List;

public class VariableTranslator {
	private Solution solution;

	public VariableTranslator(Solution pSolution) {
		this.solution = pSolution;
	}

	/**
	 * Returns a three-elements array:
	 * 1) 0 -> 0 -> Object[] -> Array of values
	 * 2) 1 -> Object[][] -> Array of arrays
	 * 3) 2 -> 0 -> Object[] -> Pointers
	 * @param pTypes
	 * @return
	 */
	public Object[][][] translateArray(CType[] pTypes) {
		int numberOfPointers = 0;
		for (int i = 0; i < pTypes.length; i++)
			if (pTypes[i] instanceof CPointer)
				numberOfPointers++;


		//Splits the types in two partitions: value types and pointer types (keeps the original order)
		CType[] valuesTypes = new CType[pTypes.length - numberOfPointers];
		CType[] pointerTypes = new CType[numberOfPointers];
		int valueId = 0;
		int pointerId = 0;
		for (int i = 0; i < pTypes.length; i++)
			if (!(pTypes[i] instanceof CPointer)) {
				valuesTypes[valueId] = pTypes[i];
				valueId++;
			} else {
				pointerTypes[pointerId] = pTypes[i];
				pointerId++;
			}

		//Sets up the three parameters return types
		int numberOfVariables = this.solution.getDecisionVariables().length;
		Object[][][] result = new Object[3][][];
		result[0] = new Object[1][];
		result[1] = new Object[numberOfVariables-2][];
		result[2] = new Object[1][];

		Variable variable;

		//Sets the array of values (first parameter)
		variable = this.solution.getDecisionVariables()[0];
		if (variable.getVariableType().equals(ArrayParameters.class)) {
			ArrayParameters array = (ArrayParameters)variable;

			Object[] arguments = new Object[array.array_.length];
			for (int i = 0; i < array.array_.length; i++) {
				if (i < valuesTypes.length){
					//arguments[i] = valuesTypes[i].getInstance(array.array_[i]); //Variable values
					//else
					//throw new RuntimeException("Not enough value types given!!");
				}

				result[0][0] = arguments;
			}

			//Sets the array of arrays (second parameter)
			for (int j = 1; j < numberOfVariables-1 ; j++) {
				variable = this.solution.getDecisionVariables()[j];
				if (variable.getVariableType().equals(ArrayParameters.class)) {
					array = (ArrayParameters)variable;

					arguments = new Object[array.array_.length];
					for (int i = 0; i < array.array_.length; i++) {
						if (j-1 < pointerTypes.length) {
							//arguments[i] = pointerTypes[j-1].getInstance(array.array_[i]); //Array values
							//else
							//throw new RuntimeException("Not enough pointer types given!!");
						}

						result[1][j-1] = arguments;
					}
				}

				//Sets the array of pointers (third parameter)
				variable = this.solution.getDecisionVariables()[numberOfVariables-1];
				if (variable.getVariableType().equals(ArrayParameters.class)) {
					array = (ArrayParameters)variable;

					arguments = new Object[array.array_.length];
					for (int i = 0; i < array.array_.length; i++) {
						arguments[i] = new Integer(array.array_[i].intValue()); //Pointer table values
					}

					result[2][0] = arguments;
				}

				return result;
			}
		}
		return null;
	}

	// WORK ONLY WITH SIMPLE DATA
	public Object[][][] translateGraph (Graph graph) {
		List<Object> scalarValue = new ArrayList<>();
		List<List<Object>> arrayValues = new ArrayList<>();
		List<Object> pointerValue = new ArrayList<>();

		for (Edge e : graph.getEdges()) {
			if (e.contains(0)) {
				Node parameterNode = null;
				if (e.getNodeFrom().getId() == 0) {
					parameterNode = e.getNodeTo();
				} else {
					parameterNode = e.getNodeFrom();
				}

				if (parameterNode instanceof ScalarNode) {
					ScalarNode scalarParameterNode = (ScalarNode) parameterNode;

					if (scalarParameterNode.getCType() instanceof CDouble) {
						scalarValue.add(scalarParameterNode.getValue());
					} else {
						scalarValue.add((int)scalarParameterNode.getValue());
					}
				}

				//DA COMPLETARE: ALTRI TIPI
			}
		}


		//Sets up the three parameters return types
		int numberOfVariables = this.solution.getDecisionVariables().length;
		Object[][][] result = new Object[3][][];
		result[0] = new Object[1][];
		result[1] = new Object[pointerValue.size()][];
		result[2] = new Object[1][];

		Object [] scalars = new Object[scalarValue.size()];
		for (int i = 0; i < scalarValue.size(); i++)
			scalars[i] = scalarValue.get(i);
		result[0][0] = scalars;

		Object [] pointers = new Object[pointerValue.size()];
		for (int i = 0; i < pointerValue.size(); i++) {
			pointers[i] = i;
			Object [] arrays = new Object[3];
			result[1][i] = arrays;
		}
		result[2][0] = pointers;

		return result;
	}

	public Graph getGraphFromSolution (List<Graph> graphList) throws JMException {
		GraphGenerator graphGenerator = new GraphGenerator();

		ArrayList<Node> nodes = new ArrayList<>();
		for (int i = 0; i < solution.getDecisionVariables().length; i++) {
			int graphIndex = (int) solution.getDecisionVariables()[i].getValue();
			//Take i node from graphIndex's graph
			Node nodeToAdd = graphList.get(graphIndex).getNode(i);
			nodes.add(nodeToAdd);
		}

		return graphGenerator.generateGraphFromArrayNodes(nodes, graphList.get(0));
	}
}
