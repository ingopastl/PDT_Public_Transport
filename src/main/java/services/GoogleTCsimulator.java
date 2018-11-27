package services;

import beans.BusStop;
import beans.Itinerary;
import beans.ItineraryBusStop;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GoogleTCsimulator {
    private static final int NUMBER_OF_OBJECTIVES = 3;

    private Itinerary itinerary;
    private List<Double> lowerLimit;
    private List<Double> upperLimit;
    private int numberOfVariables;
    private int numberOfTrips;
    private int radius;
    private double averageWalkingTime = 0;
    private double averageTripTime = 0;
    private double stopsDistanceVariance = 0;

    public GoogleTCsimulator(Itinerary itinerary, int numberOfTrips, int radius) throws Exception{
        this.itinerary = itinerary;
        this.numberOfVariables = itinerary.getStops().size() * 2;
        this.numberOfTrips = numberOfTrips;
        this.radius = radius;

        double[] boudaries = itinerary.getBoudaries();
        this.lowerLimit = new ArrayList<>(this.numberOfVariables);
        this.upperLimit = new ArrayList<>(this.numberOfVariables);
        for (int i = 0; i < this.numberOfVariables; i++) {
            if (i % 2 == 0) {
                lowerLimit.add(boudaries[0]);
                upperLimit.add(boudaries[1]);
            } else {
                lowerLimit.add(boudaries[2]);
                upperLimit.add(boudaries[3]);
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

    public double getAverageWalkingTime() {
        return averageWalkingTime;
    }

    public void setAverageWalkingTime(double averageWalkingTime) {
        this.averageWalkingTime = averageWalkingTime;
    }

    public double getAverageTripTime() {
        return averageTripTime;
    }

    public void setAverageTripTime(double averageTripTime) {
        this.averageTripTime = averageTripTime;
    }

    public double getStopsDistanceVariance() {
        return stopsDistanceVariance;
    }

    public void setStopsDistanceVariance(double stopsDistanceVariance) {
        this.stopsDistanceVariance = stopsDistanceVariance;
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

        JSONArray jsonArray = itinerary.requestRouteInfo();

        double averageWalkingTime = 0;
        double averageTripTime = 0;
        double stopsDistanceVariance = 0;

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

            GoogleRouteAPIRequester googleRouteAPIRequester = new GoogleRouteAPIRequester();

            if (start.equals(end)) {
                --t;
            } else {
                startWalkJson = googleRouteAPIRequester.walkingRoute(startP[0], startP[1], start.getBusStop().getLatitude(), start.getBusStop().getLongitude());
                endWalkJson = googleRouteAPIRequester.walkingRoute(end.getBusStop().getLatitude(), end.getBusStop().getLongitude(), endP[0], endP[1]);

                if (!startWalkJson.getString("status").equals("OK") || !endWalkJson.getString("status").equals("OK")) {
                    --t;
                } else {
                    double startWalkDuration = (int) startWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").get("value");
                    double startWalkDistance = (int) startWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value");
                    double endWalkDuration = (int) endWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").get("value");
                    double endWalkDistance = (int) endWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value");

                    double totalTravelDistance = 0;
                    double totalTravelTime = 0;

                    JSONArray allLegs = new JSONArray();
                    for (i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
                        for (int j = 0; j < legs.length(); j++) {
                            allLegs.put(legs.get(j));
                        }
                    }

                    for (i = start.getSequenceValue(); i < end.getSequenceValue(); i++) {
                        int duration = (int) allLegs.getJSONObject(i).getJSONObject("duration").get("value");
                        int distance = (int) allLegs.getJSONObject(i).getJSONObject("distance").get("value");
                        totalTravelTime += duration;
                        totalTravelDistance += distance;
                    }

                    System.out.print("Tempo andando até a parada/Distancia até a parada/Tempo no onibus/Distancia percorrida no onibus/Tempo andando até o destino/Dsitancia andada até o destino\n");
                    System.out.print(startWalkDuration/60 + " minutos / " + startWalkDistance + " metros / " + totalTravelTime/60 + " minutos / " + totalTravelDistance + " metros / " + endWalkDuration/60 + " minutos / " + endWalkDistance + " metros\n");

                    averageTripTime += totalTravelTime;
                    averageWalkingTime += startWalkDuration + endWalkDuration;
                }
            }
        }
        averageTripTime /= this.numberOfTrips;
        averageWalkingTime /= this.numberOfTrips;
        stopsDistanceVariance = itinerary.getStopsDistanceVariance();

        Double[] objectives = new Double[3];
        objectives[0] = averageTripTime;
        objectives[1] = averageWalkingTime;
        objectives[2] = stopsDistanceVariance;

        return objectives;
    }

    public void simulateWalk(int fileNumber) throws Exception {
        double[] loc1 = randomLocationBeta(itinerary);
        double[] loc2 = randomLocationBeta(itinerary);

        JSONObject jsonObject = new GoogleRouteAPIRequester().walkingRoute(loc1[0], loc1[1], loc2[0], loc2[1]);

        if (!jsonObject.getString("status").equals("OK")) {
            return;
        }

        File f = new File("src\\main\\resources\\pointsData\\" + fileNumber + ".txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(jsonObject.toString());
        bw.close();

    }

    public void simulate() throws Exception {
        for (int t = 0; t < this.numberOfTrips; t++) {
            GoogleRouteAPIRequester googleRouteAPIRequester = new GoogleRouteAPIRequester();

            double[] p1 = randomLocationBeta(this.itinerary);
            double[] p2 = randomLocationBeta(this.itinerary);
            ItineraryBusStop bs1 = findNearestStop(p1, this.itinerary);
            ItineraryBusStop bs2 = findNearestStop(p2, this.itinerary);

            BusStop start, end;
            double[] startP = new double[2];
            double[] endP = new double[2];
            JSONObject startWalkJson, endWalkJson;
            if (bs1.getSequenceValue() < bs2.getSequenceValue()) {
                start = bs1.getBusStop();
                end = bs2.getBusStop();
                startP[0] = p1[0];
                startP[1] = p1[1];
                endP[0] = p2[0];
                endP[1] = p2[1];
            } else {
                start = bs2.getBusStop();
                end = bs1.getBusStop();
                startP[0] = p2[0];
                startP[1] = p2[1];
                endP[0] = p1[0];
                endP[1] = p1[1];
            }

            if (start.equals(end)) {
                --t;
            } else {
                startWalkJson = googleRouteAPIRequester.walkingRoute(startP[0], startP[1], start.getLatitude(), start.getLongitude());
                endWalkJson = googleRouteAPIRequester.walkingRoute(end.getLatitude(), end.getLongitude(), endP[0], endP[1]);

                if (!startWalkJson.getString("status").equals("OK") || !endWalkJson.getString("status").equals("OK")) {
                    --t;
                } else {
                    double startWalkDuration = (int) startWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").get("value");
                    double startWalkDistance = (int) startWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value");
                    double endWalkDuration = (int) endWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").get("value");
                    double endWalkDistance = (int) endWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value");

                    int i = 0;
                    while (!this.itinerary.getStops().get(i).getBusStop().equals(start)) {
                        i++;
                    }
                    ArrayList<BusStop> way = new ArrayList<>();
                    while (!this.itinerary.getStops().get(i).getBusStop().equals(end)) {
                        way.add(this.itinerary.getStops().get(i).getBusStop());
                        i++;
                    }
                    way.add(this.itinerary.getStops().get(i).getBusStop());

                    JSONArray jsonArray = new GoogleRouteAPIRequester().requestRoute(way);

                    double totalTravelDistance = 0;
                    double totalTravelTime = 0;
                    for (i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

                        for (int j = 0; j < legs.length(); j++) {
                            int duration = (int) legs.getJSONObject(j).getJSONObject("duration").get("value");
                            int distance = (int) legs.getJSONObject(j).getJSONObject("distance").get("value");
                            totalTravelTime += duration;
                            totalTravelDistance += distance;
                        }
                    }

                    //System.out.print("Tempo andando até a parada/Distancia até a parada/Tempo no onibus/Distancia percorrida no onibus/Tempo andando até o destino/Dsitancia andada até o destino\n");
                    //System.out.print(startWalkDuration/60 + " minutos / " + startWalkDistance + " metros / " + totalTravelTime/60 + " minutos / " + totalTravelDistance + " metros / " + endWalkDuration/60 + " minutos / " + endWalkDistance + " metros\n");

                    this.averageTripTime += totalTravelTime;
                    this.averageWalkingTime += startWalkDuration + endWalkDuration;
                }
            }
        }
        this.averageTripTime /= this.numberOfTrips;
        this.averageWalkingTime /= this.numberOfTrips;
        this.stopsDistanceVariance = this.itinerary.getStopsDistanceVariance();
    }
}
