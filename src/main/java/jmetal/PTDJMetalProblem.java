package jmetal;

import Kmeans.Clusters;
import beans.BusStopRelation;
import beans.Itinerary;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import org.uma.jmetal.util.JMetalException;
import services.TripSimulator;
import services.osrm.OsrmTripSimulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class PTDJMetalProblem extends AbstractDoubleProblem {
	private int[] stopClusterRelation;
	private Clusters clusters;
	private int localSearchRadius;
	private TripSimulator tc;

	public PTDJMetalProblem(Itinerary itinerary, int numberOfTrips, int walkingRadius, int localSearchRadius, Clusters clusters) throws IOException {
		if (numberOfTrips <= 0) {
			throw new JMetalException("Number of trips can't be less than or equal to zero");
		}
		if (walkingRadius < 0 || localSearchRadius < 0) {
			throw new JMetalException("Radius value can't be less than zero");
		}
		if (itinerary == null) {
			throw new JMetalException("Null itinerary object");
		}

		findStopClusterRelations(clusters, itinerary.getStops());

		this.localSearchRadius = localSearchRadius;
		this.clusters = clusters;
		this.tc = new OsrmTripSimulator(itinerary, numberOfTrips, walkingRadius);
	}

	private void findStopClusterRelations(Clusters clusters, List<BusStopRelation> stops) {
		this.stopClusterRelation = new int[stops.size()];

		for (int i = 0; i < stops.size(); i++) {
			double minDistance = Double.MAX_VALUE;
			int closerClusterIndex = 0;
			for (int j = 0; j < clusters.getNumberOfClusters(); j++) {
				double[] mean = clusters.getMean(j);
				double distance = Math.sqrt( Math.pow((stops.get(i).getBusStop().getLatitude() - mean[0]), 2)
						+ Math.pow((stops.get(i).getBusStop().getLongitude() - mean[1]), 2)
						+ Math.pow((stops.get(i).getSequenceValue() - mean[2]), 2));
				if(distance < minDistance) {
					minDistance = distance;
					closerClusterIndex = j;
				}
			}
			this.stopClusterRelation[i] = closerClusterIndex;
		}
	}

	@Override
	public void evaluate(DoubleSolution solution) {
		Double[] vars = new Double[solution.getNumberOfVariables()];
		for (int i = 0; i < vars.length; i++) {
			vars[i] = solution.getVariableValue(i);
		}

		try {
			Double[] objectives = tc.evaluate(vars);

			solution.setObjective(0, objectives[0]);
			solution.setObjective(1, objectives[1]);
			solution.setObjective(2, objectives[2]);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	@Override
	public DoubleSolution createSolution() {
		double coef = this.localSearchRadius * 0.0000089;
		DefaultDoubleSolution sol = new DefaultDoubleSolution(this);

		try {
			List<BusStopRelation> l = tc.getItinerary().getStops();
			int stopsCount = 0;
			Random rand = new Random();
			double distance;
			double minDistance = Double.MAX_VALUE;
			double[] point;
			double[] closerPoint = new double[2];

			int i = 0, variableCount = 0;
			while (variableCount < getNumberOfVariables()) {
				List<double[]> localSearch = new ArrayList<>();

				for (int j = 0; j < clusters.getClusterSize(stopClusterRelation[stopsCount]); j++) {
					point = clusters.getPointFromCluster(stopClusterRelation[stopsCount], j);

					distance = Math.sqrt( Math.pow((l.get(i).getBusStop().getLatitude() - point[0]), 2)
							+ Math.pow((l.get(i).getBusStop().getLongitude() - point[1]), 2));

					if (distance < minDistance) {
						minDistance = distance;
						closerPoint = point;
					}

					if (distance <= coef) {
						localSearch.add(point);
						System.out.println("Added " + j);
					}
				}

				if (localSearch.size() <= 0) {
					sol.setVariableValue(variableCount, closerPoint[0]);
					sol.setVariableValue(variableCount + 1, closerPoint[1]);
				} else {
					int randomN = rand.nextInt(localSearch.size());
					point = localSearch.get(randomN);
					sol.setVariableValue(variableCount ,point[0]);
					sol.setVariableValue(variableCount + 1,point[1]);
				}
				i++;
				variableCount += 2;
				stopsCount++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return sol;
	}

	DoubleSolution getOriginalItinerarySolution() {
        DefaultDoubleSolution sol = new DefaultDoubleSolution(this);
        try {
            List<BusStopRelation> l = tc.getItinerary().getStops();
            int stopsCount = 0;
            for (int i = 0; i < getNumberOfVariables(); i++) {
                sol.setVariableValue(i ,l.get(stopsCount).getBusStop().getLatitude());
                ++i;
                sol.setVariableValue(i ,l.get(stopsCount).getBusStop().getLongitude());
                stopsCount++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sol;
    }

	public int[] getStopClusterRelation() {
		return stopClusterRelation;
	}
	
	@Override
	public int getNumberOfObjectives() {
		return tc.getNumberOfObjectives();
	}

	@Override
	public int getNumberOfVariables() {
		return tc.getNumberOfVariables();
	}

	@Override
	public Double getLowerBound(int index) {
		return tc.getLowerLimitVariableAt(index);
	}

	@Override
	public Double getUpperBound(int index) {
		return tc.getUpperLimitVariableAt(index);
	}
}
