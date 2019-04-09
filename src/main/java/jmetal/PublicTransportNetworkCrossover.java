package jmetal;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public class PublicTransportNetworkCrossover implements CrossoverOperator<DoubleSolution>{

	private double crossoverProbability;

	private JMetalRandom randomGenerator;

	/** Constructor */
	public PublicTransportNetworkCrossover(double crossoverProbability) {
		if (crossoverProbability < 0) {
			throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
		} 

		this.crossoverProbability = crossoverProbability;
		randomGenerator = JMetalRandom.getInstance();
	}

	/* Getters */
	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	/** Execute() method */
	@Override
	public List<DoubleSolution> execute(List<DoubleSolution> solutions) {
		if (null == solutions) {
			throw new JMetalException("Null parameter");
		} else if (solutions.size() != 2) {
			throw new JMetalException("There must be two parents instead of " + solutions.size());
		}

		return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
	}

	/** doCrossover method */
	public List<DoubleSolution> doCrossover(double probability, DoubleSolution parent1, DoubleSolution parent2) {
		List<DoubleSolution> offspring = new ArrayList<>(2);

		offspring.add((DoubleSolution) parent1.copy());
		offspring.add((DoubleSolution) parent2.copy());

		int i;
		double valueX1, valueX2, valueY1, valueY2;

		if (randomGenerator.nextDouble() <= probability) {
			for (i = 0; i < parent1.getNumberOfVariables(); i += 2) {
				valueX1 = parent1.getVariableValue(i);
				valueX2 = parent2.getVariableValue(i);
				valueY1 = parent1.getVariableValue(i+1);
				valueY2 = parent2.getVariableValue(i+1);
				if (randomGenerator.nextDouble() <= 0.25) {
					offspring.get(0).setVariableValue(i, valueX2);
					offspring.get(0).setVariableValue(i+1, valueY2);
					offspring.get(1).setVariableValue(i, valueX1);
					offspring.get(1).setVariableValue(i+1, valueY1);
				} else {
					offspring.get(0).setVariableValue(i, valueX1);
					offspring.get(0).setVariableValue(i+1, valueY1);
					offspring.get(1).setVariableValue(i, valueX2);
					offspring.get(1).setVariableValue(i+1, valueY2);
				}
			}
		}

		return offspring;
	}

	@Override
	public int getNumberOfRequiredParents() {
		return 2;
	}

	@Override
	public int getNumberOfGeneratedChildren() {
		return 2;
	}

}
