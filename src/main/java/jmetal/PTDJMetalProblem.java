package jmetal;

import beans.Itinerary;
import beans.ItineraryBusStop;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import org.uma.jmetal.util.JMetalException;
import services.TripSimulator;
import services.osrm.OsrmTripSimulator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PTDJMetalProblem extends AbstractDoubleProblem {
	private TripSimulator tc;

	public PTDJMetalProblem(Itinerary itinerary, int numberOfTrips, int radius) throws Exception {
		if (numberOfTrips <= 0) {
			throw new JMetalException("Number of trips can't be less than or equal to zero");
		}
		if (radius < 0) {
			throw new JMetalException("Radius value can't be less than zero");
		}
		if (itinerary == null) {
			throw new JMetalException("Null itinerary object");
		}

		this.tc = new OsrmTripSimulator(itinerary, numberOfTrips, radius);
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
		try {
			List<ItineraryBusStop> l = tc.getItinerary().getStops();
			int stopsCount = 0;
			for (int i = 0; i < getNumberOfVariables(); i++) {
				double random1 = ThreadLocalRandom.current().nextDouble(-coef, coef);
				double random2 = ThreadLocalRandom.current().nextDouble(-coef, coef);
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
            List<ItineraryBusStop> l = tc.getItinerary().getStops();
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
