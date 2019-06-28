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
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PTDJMetalProblem extends AbstractDoubleProblem {
	private int stopClusterRelation[];
	private Clusters clusters;
	private TripSimulator tc;

	public PTDJMetalProblem(Itinerary itinerary, int numberOfTrips, int radius, Clusters clusters) throws IOException {
		if (numberOfTrips <= 0) {
			throw new JMetalException("Number of trips can't be less than or equal to zero");
		}
		if (radius < 0) {
			throw new JMetalException("Radius value can't be less than zero");
		}
		if (itinerary == null) {
			throw new JMetalException("Null itinerary object");
		}

		findStopClusterRelations(clusters, itinerary.getStops());

		this.clusters = clusters;
		this.tc = new OsrmTripSimulator(itinerary, numberOfTrips, radius);
	}

	private void findStopClusterRelations(Clusters clusters, List<BusStopRelation> stops) {
		this.stopClusterRelation = new int[clusters.getNumberOfClusters()];

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
		double coef = tc.getRadius() * 0.0000089;
		DefaultDoubleSolution sol = new DefaultDoubleSolution(this);

		int numberOfClusters = this.clusters.getNumberOfClusters();

		try {
			List<BusStopRelation> l = tc.getItinerary().getStops();
			int stopsCount = 0;
			for (int i = 0; i < getNumberOfVariables(); i++) {
				

				sol.setVariableValue(i ,l.get(stopsCount).getBusStop().getLatitude() + random1);
				++i;
				sol.setVariableValue(i ,l.get(stopsCount).getBusStop().getLongitude() + random2);
				stopsCount++;
			}
		} catch (Exception e) {
			e.printStackTrace();
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
