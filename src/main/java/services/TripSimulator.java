package services;

import beans.BusStopRelation;
import beans.BusStop;
import beans.Itinerary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class TripSimulator {
    private static final int NUMBER_OF_OBJECTIVES = 3;

    private Itinerary itinerary;
    private List<Double> lowerLimit;
    private List<Double> upperLimit;
    private int numberOfVariables;
    private int numberOfTrips;
    private int walkingRadius;

    public TripSimulator(Itinerary itinerary, int numberOfTrips, int walkingRadius) throws IOException {
        this.itinerary = itinerary;
        this.numberOfVariables = itinerary.getStops().size() * 2;
        this.numberOfTrips = numberOfTrips;
        this.walkingRadius = walkingRadius;

        double[] boundaries = itinerary.getBounds();
        System.out.println(boundaries[0]);
        System.out.println(boundaries[1]);
        System.out.println(boundaries[2]);
        System.out.println(boundaries[3]);
        this.lowerLimit = new ArrayList<>(this.numberOfVariables);
        this.upperLimit = new ArrayList<>(this.numberOfVariables);
        for (int i = 0; i < this.numberOfVariables; i++) {
            if (i % 2 == 0) {
                upperLimit.add(boundaries[0]);
                lowerLimit.add(boundaries[1]);
            } else {
                upperLimit.add(boundaries[2]);
                lowerLimit.add(boundaries[3]);
            }
        }
    }

    public double getLowerLimitVariableAt(int index) {
        return this.lowerLimit.get(index);
    }

    public double getUpperLimitVariableAt(int index) {
        return this.upperLimit.get(index);
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }

    public int getNumberOfObjectives() {
        return NUMBER_OF_OBJECTIVES;
    }

    public int getNumberOfVariables() {
        return numberOfVariables;
    }

    public int getNumberOfTrips() {
        return numberOfTrips;
    }

    public void setNumberOfTrips(int numberOfTrips) {
        this.numberOfTrips = numberOfTrips;
    }

    public int getWalkingRadius() {
        return walkingRadius;
    }

    public void setWalkingRadius(int walkingRadius) {
        this.walkingRadius = walkingRadius;
    }

    protected double[] randomLocationInsideBounds(Itinerary itinerary) throws Exception {
        double[] bounds = itinerary.getBounds();

        // number of km per degree = ~111km (111.32 in google maps, but range varies
        // between 110.567km at the equator and 111.699km at the poles)
        // 1km in degree = 1 / 111.32km = 0.0089
        // 1m in degree = 0.0089 / 1000 = 0.0000089
        double coef = this.walkingRadius * 0.0000089;

        double highestLat = bounds[0] + coef;
        double lowestLat = bounds[1] - coef;
        double highestLong = bounds[2] + coef;
        double lowestLong = bounds[3] - coef;

        double[] randomLocation = new double[2];
        Random r = new Random();
        randomLocation[0] = lowestLat + (highestLat - lowestLat) * r.nextDouble();
        randomLocation[1] = lowestLong + (highestLong - lowestLong) * r.nextDouble();
        return randomLocation;
    }

    public double[] getBounds() throws Exception {
        return itinerary.getBounds();
    }

    protected BusStopRelation findNearestStop(double[] point, Itinerary itinerary) throws Exception {
        List<BusStopRelation> stops = itinerary.getStops();
        double stopLat;
        double stopLong;
        double closestStopEuclidianDistance = 100000000;
        BusStopRelation closestStop = null;
        for (int i = 0; i < stops.size(); i++) {
            stopLat = stops.get(i).getBusStop().getLatitude();
            stopLong = stops.get(i).getBusStop().getLongitude();

            double euclidian = Math.sqrt(Math.pow(point[0] - stopLat, 2) + Math.pow(point[1] - stopLong, 2));
            if (euclidian < closestStopEuclidianDistance) {
                closestStopEuclidianDistance = euclidian;
                closestStop = stops.get(i);
            }
        }
        //System.out.print(point[0] + "\n" + point[1] + "\n\n");
        //System.out.print(closestStop.getLatitude() + "\n" + closestStop.getLongitude() + "\n\n");
        return closestStop;
    }

    protected static Itinerary turnIntoItinerary(Double[] vars) {
        Itinerary itinerary = new Itinerary(null, '1', "1", "Solution");
        BusStop v;
        int i = 0, order = 0;
        while (i < vars.length) {
            v = new BusStop(Integer.toString(i), vars[i], vars[i+1]);
            BusStopRelation ibs = new BusStopRelation(v, itinerary, order);
            itinerary.addItineraryBusStop(ibs);
            order++;
            i += 2;
        }
        return itinerary;
    }

    public abstract Double[] evaluate(Double[] vars) throws Exception;
}
