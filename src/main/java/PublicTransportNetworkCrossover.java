import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

public class PublicTransportNetworkCrossover implements CrossoverOperator<IntegerSolution>{

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
	public List<IntegerSolution> execute(List<IntegerSolution> solutions) {
		if (null == solutions) {
			throw new JMetalException("Null parameter");
		} else if (solutions.size() != 2) {
			throw new JMetalException("There must be two parents instead of " + solutions.size());
		}

		return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1));
	}

	/** doCrossover method */
	public List<IntegerSolution> doCrossover(double probability, IntegerSolution parent1, IntegerSolution parent2) {
		List<IntegerSolution> offspring = new ArrayList<IntegerSolution>(2);

		offspring.add((IntegerSolution) parent1.copy());
		offspring.add((IntegerSolution) parent2.copy());

		int i;
		int valueX1, valueX2;

		if (randomGenerator.nextDouble() <= probability) {
			for (i = 0; i < parent1.getNumberOfVariables(); i++) {
				valueX1 = parent1.getVariableValue(i);
				valueX2 = parent2.getVariableValue(i);
				if (randomGenerator.nextDouble() <= 0.25) {
					offspring.get(0).setVariableValue(i, valueX2);
					offspring.get(1).setVariableValue(i, valueX1);
				} else {
					offspring.get(0).setVariableValue(i, valueX1);
					offspring.get(1).setVariableValue(i, valueX2);
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
