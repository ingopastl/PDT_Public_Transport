import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.IntegerProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.concurrent.ThreadLocalRandom;

public class PublicTransportNetworkMutation implements MutationOperator<DoubleSolution> {
	private static final double DEFAULT_PROBABILITY = 0.01;

	private double mutationProbability;

	private JMetalRandom randomGenerator;

	/** Constructor */
	public PublicTransportNetworkMutation() {
		this(DEFAULT_PROBABILITY);
	}

	/** Constructor */
	public PublicTransportNetworkMutation(DoubleProblem problem) {
		this(1.0 / problem.getNumberOfVariables());
	}

	/** Constructor */
	public PublicTransportNetworkMutation(double mutationProbability) {
		if (mutationProbability < 0) {
			throw new JMetalException("Mutation probability is negative: " + mutationProbability);
		}
		this.mutationProbability = mutationProbability;

		randomGenerator = JMetalRandom.getInstance();
	}

	/* Getters */
	public double getMutationProbability() {
		return mutationProbability;
	}

	/** Execute() method */
	public DoubleSolution execute(DoubleSolution solution) throws JMetalException {
		if (null == solution) {
			throw new JMetalException("Null parameter");
		}

		doMutation(mutationProbability, solution);
		return solution;
	}

	/** Perform the mutation operation */
	private void doMutation(double probability, DoubleSolution solution) {
		int i = 0;
		while (i < solution.getNumberOfVariables()) {
			if (randomGenerator.nextDouble() <= probability) {
				double sd = smallerDistance(solution, i);
				System.out.print("Smaller Distance: " + sd + "\n");

				double lat = solution.getVariableValue(i);
				double longi = solution.getVariableValue(i + 1);

				solution.setVariableValue(i, lat + (sd * getOneOrNegativeOne() * randomGenerator.nextDouble()));
				solution.setVariableValue(i + 1, longi + (sd * getOneOrNegativeOne() * randomGenerator.nextDouble()));
			}
			i += 2;
		}
	}

	private double smallerDistance(DoubleSolution solution, int index) {
		if (index == 0) {
			double indexPointLat = solution.getVariableValue(index);
			double indexPointLongi = solution.getVariableValue(index + 1);
			double p1Lat = solution.getVariableValue(index + 2);
			double p1Longi = solution.getVariableValue(index + 3);

			return distance(indexPointLat, indexPointLongi, p1Lat, p1Longi);
		} else if (index == solution.getNumberOfVariables()) {
			//todo
		} else {
			double indexPointLat = solution.getVariableValue(index);
			double indexPointLongi = solution.getVariableValue(index + 1);
			double p1Lat = solution.getVariableValue(index + 2);
			double p1Longi = solution.getVariableValue(index + 3);
			double p2Lat = solution.getVariableValue(index - 1);
			double p2Longi = solution.getVariableValue(index - 2);

			double distance1 = distance(indexPointLat,indexPointLongi, p1Lat, p1Longi);
			double distance2 = distance(indexPointLat, indexPointLongi, p2Lat, p2Longi);

			if (distance1 < distance2) {
				return distance1;
			} else {
				return distance2;
			}
		}
	}

	private double distance(double p1Lat, double p1Longi, double p2Lat, double p2Longi) {
		return Math.sqrt(Math.pow(p1Lat - p2Lat, 2) + Math.pow(p1Longi - p2Longi, 2));
	}

	private int getOneOrNegativeOne() {
		double n = randomGenerator.nextDouble();
		if (n < 0.5) {
			return -1;
		} else {
			return 1;
		}
	}
}
