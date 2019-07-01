package jmetal;

import Kmeans.Clusters;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PublicTransportNetworkMutation implements MutationOperator<DoubleSolution> {
	private static final double DEFAULT_PROBABILITY = 0.01;

	private double mutationProbability;
	private double localSearchRadius;
	private int[] stopClusterRelation;
	private Clusters clusters;

	private JMetalRandom randomGenerator;

	/** Constructor */
	public PublicTransportNetworkMutation(DoubleProblem problem, double localSearchRadius, Clusters clusters, int[] stopClusterRelation) {
		this(1.0 / problem.getNumberOfVariables(), localSearchRadius, clusters, stopClusterRelation);
	}

	/** Constructor */
	public PublicTransportNetworkMutation(double mutationProbability, double localSearchRadius, Clusters clusters, int[] stopClusterRelation) {
		if (mutationProbability < 0) {
			throw new JMetalException("Mutation probability is negative: " + mutationProbability);
		}
		this.mutationProbability = mutationProbability;
		this.localSearchRadius = localSearchRadius;
		this.clusters = clusters;
		this.stopClusterRelation = stopClusterRelation;

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
		Random rand = new Random();
		double coef = localSearchRadius * 0.0000089;

		int i = 0;
		while (i < solution.getNumberOfVariables()) {
			if (randomGenerator.nextDouble() <= probability) {
				int stopindex = (i+1)/2;
				double distance;
				double minDistance = Double.MAX_VALUE;
				double[] point;
				double[] closerPoint = new double[2];

				double lat = solution.getVariableValue(i);
				double longi = solution.getVariableValue(i + 1);

				List<double[]> localSearch = new ArrayList<>();
				for (int j = 0; j < clusters.getClusterSize(stopClusterRelation[stopindex]); j++) {
					point = clusters.getPointFromCluster(stopClusterRelation[stopindex], j);

					distance = Math.sqrt( Math.pow((lat - point[0]), 2) + Math.pow((longi - point[1]), 2));

					if (distance < minDistance) {
						minDistance = distance;
						closerPoint = point;
					}

					if (distance <= coef) {
						localSearch.add(point);
						System.out.println("Added " + j);
					}
				}

				System.out.println("Local size: " + localSearch.size());

				if (localSearch.size() <= 0) {
					solution.setVariableValue(i, closerPoint[0]);
					solution.setVariableValue(i + 1, closerPoint[1]);
				} else {
					int randomN = rand.nextInt(localSearch.size());
					point = localSearch.get(randomN);

					solution.setVariableValue(i, point[0]);
					solution.setVariableValue(i + 1, point[1]);
				}
			}
			i += 2;
		}
	}
}
