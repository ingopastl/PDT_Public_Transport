package services;

import beans.BusStop;
import beans.Itinerary;
import beans.ItineraryBusStop;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TCsimulator {
    private int numberOfTrips;
    private Itinerary itinerary;
    private int r;

    private double averageWalkingTime = 0;
    private double averageTripTime = 0;

    public TCsimulator(Itinerary itinerary, int numberOfTrips, int r) throws Exception{
        this.itinerary = itinerary;
        this.numberOfTrips = numberOfTrips;
        this.r = r;

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
        return r;
    }

    public void setR(int r) {
        this.r = r;
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
        double[] boundaries = this.itinerary.getBoudaries();

        double meters = this.r;
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

    private BusStop findNearestStop(double[] point, Itinerary itinerary) throws Exception {
        List<ItineraryBusStop> stops = itinerary.getStops();
        double stopLat;
        double stopLong;
        double closestStopEuclidianDistance = 100000;
        BusStop closestStop = null;
        for (int i = 0; i < stops.size(); i++) {
            stopLat = stops.get(i).getBusStop().getLatitude();
            stopLong = stops.get(i).getBusStop().getLongitude();

            double euclidian = Math.sqrt(Math.pow(point[0] - stopLat, 2) + Math.pow(point[1] - stopLong, 2));
            if (euclidian < closestStopEuclidianDistance) {
                closestStopEuclidianDistance = euclidian;
                closestStop = stops.get(i).getBusStop();
            }
        }
        //System.out.print(point[0] + "\n" + point[1] + "\n\n");
        //System.out.print(closestStop.getLatitude() + "\n" + closestStop.getLongitude() + "\n\n");
        return closestStop;
    }

    public void simulate() throws Exception {
        for (int t = 0; t < this.numberOfTrips; t++) {
            GoogleRouteAPIRequester googleRouteAPIRequester = new GoogleRouteAPIRequester();

            BusStop departure = this.itinerary.getStops().get(0).getBusStop();

            double[] p1 = randomLocation();
            double[] p2 = randomLocation();

            BusStop bs1 = findNearestStop(p1, this.itinerary);
            BusStop bs2 = findNearestStop(p2, this.itinerary);

            double euclidian1 = Math.sqrt(Math.pow(departure.getLatitude() - bs1.getLatitude(), 2) + Math.pow(departure.getLongitude() - bs1.getLongitude(), 2));
            double euclidian2 = Math.sqrt(Math.pow(departure.getLatitude() - bs2.getLatitude(), 2) + Math.pow(departure.getLongitude() - bs2.getLongitude(), 2));

            BusStop start, end;
            JSONObject startWalkJson, endWalkJson;
            if (euclidian1 < euclidian2) {
                start = bs1;
                end = bs2;
                startWalkJson = googleRouteAPIRequester.walkingRoute(p1[0], p1[1], start.getLatitude(), start.getLongitude());
                endWalkJson = googleRouteAPIRequester.walkingRoute(end.getLatitude(), end.getLongitude(), p2[0], p2[1]);
            } else {
                start = bs2;
                end = bs1;
                startWalkJson = googleRouteAPIRequester.walkingRoute(p2[0], p2[1], start.getLatitude(), start.getLongitude());
                endWalkJson = googleRouteAPIRequester.walkingRoute(end.getLatitude(), end.getLongitude(), p1[0], p1[1]);
            }

            double startWalkDuration = (int) startWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").get("value");
            double startWalkDistance = (int) startWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value");
            double endWalkDuration = (int) endWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").get("value");
            double endWalkDistance = (int) endWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").get("value");

            int i = 0;
            while (!this.itinerary.getStops().get(i).getBusStop().equals(start)) {
                i++;
            }
            ArrayList<BusStop> way = new ArrayList<BusStop>();
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

            //System.out.print("Tempo andando até a parada/Distancia até a parada/Tempo no onibus/Distancia percorrida no onibus/Tempo andando até o destino/Dsitancia andada até o destino");
            //System.out.print(startWalkDuration/60 + " minutos / " + startWalkDistance + " metros / " + totalTravelTime/60 + " minutos / " + totalTravelDistance + " metros / " + endWalkDuration/60 + " minutos / " + endWalkDistance + " metros\n");

            this.averageTripTime += totalTravelTime;
            this.averageWalkingTime += startWalkDuration + endWalkDuration;
        }
        this.averageTripTime /= this.numberOfTrips;
        this.averageWalkingTime /= this.numberOfTrips;
    }
}
