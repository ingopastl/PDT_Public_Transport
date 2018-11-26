import beans.Itinerary;
import beans.ItineraryBusStop;
import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;

import services.TCsimulator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class PTDJMetalProblem extends AbstractDoubleProblem {
	private TCsimulator tc;

	public PTDJMetalProblem(Itinerary itinerary, int numberOfTrips, int radius) throws Exception {
		this.tc = new TCsimulator(itinerary, numberOfTrips, radius);
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
		}
	}

	@Override
	public DoubleSolution createSolution() {
		DefaultDoubleSolution sol = new DefaultDoubleSolution(this);
		try {
			List<ItineraryBusStop> l = tc.getItinerary().getStops();
			int stopsCount = 0;
			for (int i = 0; i < getNumberOfVariables(); i++) {
				double random1 = ThreadLocalRandom.current().nextDouble(-0.05, 0.05);
				double random2 = ThreadLocalRandom.current().nextDouble(-0.05, 0.05);
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
		//todo
		return 5.0;
	}

	@Override
	public Double getUpperBound(int index) {
		//todo
		return 5.0;
	}
}
