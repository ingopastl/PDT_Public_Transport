package services;

import beans.BusStop;
import beans.Itinerary;
import beans.ItineraryBusStop;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public abstract class TripSimulator {
    private static final int NUMBER_OF_OBJECTIVES = 3;

    private Itinerary itinerary;
    private List<Double> lowerLimit;
    private List<Double> upperLimit;
    private int numberOfVariables;
    private int numberOfTrips;
    private int radius;

    public TripSimulator(Itinerary itinerary, int numberOfTrips, int radius) throws Exception{
        this.itinerary = itinerary;
        this.numberOfVariables = itinerary.getStops().size() * 2;
        this.numberOfTrips = numberOfTrips;
        this.radius = radius;

        double[] boudaries = itinerary.getBoudaries();
        this.lowerLimit = new ArrayList<>(this.numberOfVariables);
        this.upperLimit = new ArrayList<>(this.numberOfVariables);
        for (int i = 0; i < this.numberOfVariables; i++) {
            if (i % 2 == 0) {
                upperLimit.add(boudaries[0]);
                lowerLimit.add(boudaries[1]);
            } else {
                upperLimit.add(boudaries[2]);
                lowerLimit.add(boudaries[3]);
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

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    protected double[] randomLocationAlpha(Itinerary itinerary) throws Exception {
        int randomInt = ThreadLocalRandom.current().nextInt(0, itinerary.getStops().size());
        BusStop stop = itinerary.getStops().get(randomInt).getBusStop();

        double meters = this.radius;
        // number of km per degree = ~111km (111.32 in google maps, but range varies
        // between 110.567km at the equator and 111.699km at the poles)
        // 1km in degree = 1 / 111.32km = 0.0089
        // 1m in degree = 0.0089 / 1000 = 0.0000089
        double coef = meters * 0.0000089;

        double highestLat = stop.getLatitude() + coef;
        double lowestLat = stop.getLatitude() - coef;
        double highestLong = stop.getLongitude() + coef;
        double lowestLong = stop.getLongitude() - coef;

        double[] randomLocation = new double[2];

        Random r = new Random();
        randomLocation[0] = lowestLat + (highestLat - lowestLat) * r.nextDouble();
        randomLocation[1] = lowestLong + (highestLong - lowestLong) * r.nextDouble();

        return randomLocation;
    }

    protected double[] randomLocationBeta(Itinerary itinerary) throws Exception {
        double[] boundaries = itinerary.getBoudaries();

        double meters = this.radius;
        // number of km per degree = ~111km (111.32 in google maps, but range varies
        // between 110.567km at the equator and 111.699km at the poles)
        // 1km in degree = 1 / 111.32km = 0.0089
        // 1m in degree = 0.0089 / 1000 = 0.0000089
        double coef = meters * 0.0000089;

        double highestLat = boundaries[0] + coef;
        double lowestLat = boundaries[1] - coef;
        double highestLong = boundaries[2] + coef;
        double lowestLong = boundaries[3] - coef;

        double[] randomLocation = new double[2];

        Random r = new Random();
        randomLocation[0] = lowestLat + (highestLat - lowestLat) * r.nextDouble();
        randomLocation[1] = lowestLong + (highestLong - lowestLong) * r.nextDouble();

        return randomLocation;
    }

    public double[] getBoundariesBeta() throws Exception {
        return itinerary.getBoudaries();
    }

    protected ItineraryBusStop findNearestStop(double[] point, Itinerary itinerary) throws Exception {
        List<ItineraryBusStop> stops = itinerary.getStops();
        double stopLat;
        double stopLong;
        double closestStopEuclidianDistance = 100000000;
        ItineraryBusStop closestStop = null;
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

    public abstract Double[] evaluate(Double[] vars) throws Exception;
}
