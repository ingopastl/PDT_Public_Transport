package services;

import beans.BusStop;
import beans.Itinerary;
import beans.ItineraryBusStop;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TCsimulator {
    private int numberOfTrips;
    private Itinerary itinerary;
    private int radius;

    private double averageWalkingTime = 0;
    private double averageTripTime = 0;

    public TCsimulator(Itinerary itinerary, int numberOfTrips, int radius) throws Exception{
        this.itinerary = itinerary;
        this.numberOfTrips = numberOfTrips;
        this.radius = radius;

        simulate();
    }

    public int getNumberOfTrips() {
        return numberOfTrips;
    }

    public void setNumberOfTrips(int numberOfTrips) {
        this.numberOfTrips = numberOfTrips;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }

    public int getR() {
        return radius;
    }

    public void setR(int r) {
        this.radius = r;
    }

    public double getStopsVariance() throws Exception {
        return this.itinerary.getStopsDistanceVariance();
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

    private double[] randomLocation() throws Exception {
        int randomInt = ThreadLocalRandom.current().nextInt(0, this.itinerary.getStops().size());
        BusStop stop = this.itinerary.getStops().get(randomInt).getBusStop();

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

    private void simulate() throws Exception {
        for (int t = 0; t < this.numberOfTrips; t++) {
            GoogleRouteAPIRequester googleRouteAPIRequester = new GoogleRouteAPIRequester();

            double[] p1 = randomLocation();
            double[] p2 = randomLocation();
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
    }
}
