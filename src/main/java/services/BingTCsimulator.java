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

public class BingTCsimulator {
    private static final int NUMBER_OF_OBJECTIVES = 3;

    private Itinerary itinerary;
    private List<Double> lowerLimit;
    private List<Double> upperLimit;
    private int numberOfVariables;
    private int numberOfTrips;
    private int radius;

    public BingTCsimulator(Itinerary itinerary, int numberOfTrips, int radius) throws Exception{
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

    private double[] randomLocationAlpha(Itinerary itinerary) throws Exception {
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

    private double[] randomLocationBeta(Itinerary itinerary) throws Exception {
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

    private ItineraryBusStop findNearestStop(double[] point, Itinerary itinerary) throws Exception {
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

    public Double[] evaluate(Double[] vars) throws Exception {
        int i = 0, order = 0;
        Itinerary itinerary = new Itinerary(null, '1', "1", "Solution");
        BusStop v;
        while (i < vars.length) {
            v = new BusStop(Integer.toString(i), vars[i], vars[i+1]);
            i += 2;
            ItineraryBusStop ibs = new ItineraryBusStop(v, itinerary, order);
            order++;
            itinerary.addItineraryBusStop(ibs);
        }

        JSONArray jsonArray = new BingAPIRequester().requestRoute(itinerary.turnIntoBusStopList(itinerary.getStops()));
        JSONArray allLegs = new JSONArray();

        double averageWalkingTime = 0;
        double averageTripTime = 0;
        double stopsDistanceVariance;

        for (int t = 0; t < this.numberOfTrips; t++) {
            double[] p1 = randomLocationBeta(itinerary);
            double[] p2 = randomLocationBeta(itinerary);
            ItineraryBusStop bs1 = findNearestStop(p1, itinerary);
            ItineraryBusStop bs2 = findNearestStop(p2, itinerary);

            ItineraryBusStop start, end;
            double[] startP = new double[2];
            double[] endP = new double[2];
            JSONObject startWalkJson, endWalkJson;
            if (bs1.getSequenceValue() < bs2.getSequenceValue()) {
                start = bs1;
                end = bs2;
                startP[0] = p1[0];
                startP[1] = p1[1];
                endP[0] = p2[0];
                endP[1] = p2[1];
            } else {
                start = bs2;
                end = bs1;
                startP[0] = p2[0];
                startP[1] = p2[1];
                endP[0] = p1[0];
                endP[1] = p1[1];
            }

            BingAPIRequester apiRequester = new BingAPIRequester();
            if (start.equals(end)) {
                --t;
            } else {
                startWalkJson = apiRequester.walkingRoute(startP[0], startP[1], start.getBusStop().getLatitude(), start.getBusStop().getLongitude());
                endWalkJson = apiRequester.walkingRoute(end.getBusStop().getLatitude(), end.getBusStop().getLongitude(), endP[0], endP[1]);

                if (!startWalkJson.getString("statusDescription").equals("OK") || !endWalkJson.getString("statusDescription").equals("OK")) {
                    --t;
                } else {
                    double startWalkDistance;
                    double endWalkDistance;
                    int startWalkDuration = (int) startWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDuration");
                    try {
                        startWalkDistance = (double) startWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDistance");
                    } catch (ClassCastException e) {
                        //e.printStackTrace();
                        startWalkDistance = (int) startWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDistance");
                    }
                    int endWalkDuration = (int) endWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDuration");
                    try {
                        endWalkDistance = (double) endWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDistance");
                    } catch (ClassCastException e) {
                        //e.printStackTrace();
                        endWalkDistance = (int) endWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDistance");
                    }


                    double totalTravelDistance = 0;
                    double totalTravelTime = 0;

                    for (i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        JSONArray legs = jsonObject.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getJSONArray("routeLegs");
                        for (int j = 0; j < legs.length(); j++) {
                            allLegs.put(legs.get(j));
                        }
                    }

                    for (i = start.getSequenceValue(); i < end.getSequenceValue(); i++) {
                        int duration = (int) allLegs.getJSONObject(i).get("travelDuration");
                        double distance;
                        try {
                            distance = (double) allLegs.getJSONObject(i).get("travelDistance");
                        } catch (ClassCastException e) {
                            //e.printStackTrace();
                            distance = (int) allLegs.getJSONObject(i).get("travelDistance");
                        }
                        totalTravelTime += duration;
                        totalTravelDistance += distance;
                    }

                    //System.out.print("Tempo andando até a parada/Distancia até a parada/Tempo no onibus/Distancia percorrida no onibus/Tempo andando até o destino/Dsitancia andada até o destino\n");
                    //System.out.print(startWalkDuration/60 + " minutos / " + startWalkDistance + " KM / " + totalTravelTime/60 + " minutos / " + totalTravelDistance + " KM / " + endWalkDuration/60 + " minutos / " + endWalkDistance + " KM\n");

                    averageTripTime += totalTravelTime;
                    averageWalkingTime += startWalkDuration + endWalkDuration;
                }
            }
        }

        ArrayList<Double> x = new ArrayList<>();
        double distanceAverage = 0, d;
        System.out.print("Legs size: " + allLegs.length() + "\n");
        for (i = 0; i < allLegs.length(); i++) {
            try {
                d = (double) allLegs.getJSONObject(i).get("travelDistance");
            } catch (ClassCastException e) {
                //e.printStackTrace();
                d = (int) allLegs.getJSONObject(i).get("travelDistance");
            }
            x.add(d);
            distanceAverage += d;
        }
        distanceAverage = distanceAverage/x.size();

        System.out.print("Média: " + distanceAverage + "\n");

        double variance = 0;
        for (i = 0; i < x.size(); i++) {
            variance += Math.pow(x.get(i) - distanceAverage, 2);
        }

        averageTripTime /= this.numberOfTrips;
        averageWalkingTime /= this.numberOfTrips;
        stopsDistanceVariance = variance;

        Double[] objectives = new Double[3];
        objectives[0] = averageTripTime;
        objectives[1] = averageWalkingTime;
        objectives[2] = stopsDistanceVariance;

        System.out.print("averageTripTime: " + averageTripTime + "\naverageWalkingTime: " + averageWalkingTime + "\nstopsDistanceVariance: " + stopsDistanceVariance + "\n");

        return objectives;
    }
}
