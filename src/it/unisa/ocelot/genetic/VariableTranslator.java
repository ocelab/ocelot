package it.unisa.ocelot.genetic;

import it.unisa.ocelot.c.types.CDouble;
import it.unisa.ocelot.c.types.CInteger;
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
import jmetal.encodings.variable.ArrayReal;
import jmetal.util.JMException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableTranslator {
	private List<Graph> graphList;
	private Map<Integer, Integer> scalarNodeIndexMap;

	public VariableTranslator(List<Graph> graphList, Map<Integer, Integer> scalarNodeIndexMap) {
		this.graphList = graphList;
		this.scalarNodeIndexMap = scalarNodeIndexMap;
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

					if (scalarParameterNode.getCType() instanceof CInteger) {
						scalarValue.add((int)scalarParameterNode.getValue());
					} else {
						scalarValue.add(scalarParameterNode.getValue());
					}
				}

				//DA COMPLETARE: ALTRI TIPI
			}
		}


		//Sets up the three parameters return types
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

	/*public Graph getGraphFromSolution (List<Graph> graphList) throws JMException {
		GraphGenerator graphGenerator = new GraphGenerator();

		ArrayList<Node> nodes = new ArrayList<>();
		for (int i = 0; i < solution.getDecisionVariables().length; i++) {
			int graphIndex = (int) solution.getDecisionVariables()[i].getValue();
			//Take i node from graphIndex's graph
			Node nodeToAdd = graphList.get(graphIndex).getNode(i);
			nodes.add(nodeToAdd);
		}

		return graphGenerator.generateGraphFromArrayNodes(nodes, graphList.get(0));
	}*/

	public Graph getGraphFromSolution (Solution solution) {
		GraphGenerator graphGenerator = new GraphGenerator();

		Graph graphToReturn = null;
		try {
			graphToReturn = (Graph) graphList.get(0).clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

		Double [] variables = ((ArrayReal)solution.getDecisionVariables()[0]).array_;

		//Take nodes from chromosome
		for (int i = 0; i < variables.length; i++) {
			//Get index of scalaNode
			int scalarNodeIndex = this.scalarNodeIndexMap.get(i);

			((ScalarNode)graphToReturn.getNode(scalarNodeIndex)).setValue(variables[i]);
		}

		return graphToReturn;
	}
}
