package services.google;

import beans.BusStopRelation;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import services.TripSimulator;

import beans.Itinerary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class GoogleTripSimulator extends TripSimulator {

    private HttpClient client;

    public GoogleTripSimulator(Itinerary itinerary, int numberOfTrips, int radius) throws Exception{
        super(itinerary, numberOfTrips, radius);
        this.client = HttpClientBuilder.create().build();
    }

    public Double[] evaluate(Double[] vars) throws Exception {
        Itinerary itinerary = turnIntoItinerary(vars);

        GoogleAPIRequester apiRequester = new GoogleAPIRequester();
        JSONArray jsonArray = apiRequester.requestRoute(itinerary.turnIntoBusStopList(itinerary.getStops()));

        JSONArray allLegs = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray legs = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs");
            for (int j = 0; j < legs.length(); j++) {
                allLegs.put(legs.get(j));
            }
        }

        double averageWalkingTime = 0;
        double averageTripTime = 0;
        for (int t = 0; t < getNumberOfTrips(); t++) {
            double[] p1 = randomLocationInsideBounds(itinerary);
            double[] p2 = randomLocationInsideBounds(itinerary);
            BusStopRelation bs1 = findNearestStop(p1, itinerary);
            BusStopRelation bs2 = findNearestStop(p2, itinerary);

            BusStopRelation start, end;
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

            if (start.equals(end)) {
                --t;
            } else {
                startWalkJson = apiRequester.requestWalkingRoute(startP[0], startP[1], start.getBusStop().getLatitude(), start.getBusStop().getLongitude());
                endWalkJson = apiRequester.requestWalkingRoute(end.getBusStop().getLatitude(), end.getBusStop().getLongitude(), endP[0], endP[1]);

                if (!startWalkJson.getString("status").equals("OK") || !endWalkJson.getString("status").equals("OK")) {
                    --t;
                } else {
                    int startWalkDuration = startWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");
                    int endWalkDuration = endWalkJson.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");

                    double totalTravelTime = 0;
                    for (int i = start.getSequenceValue(); i < end.getSequenceValue(); i++) {
                        int duration = allLegs.getJSONObject(i).getJSONObject("duration").getInt("value");
                        totalTravelTime += duration;
                    }

                    averageTripTime += totalTravelTime;
                    averageWalkingTime += startWalkDuration + endWalkDuration;
                }
            }
        }
        averageTripTime /= getNumberOfTrips();
        averageWalkingTime /= getNumberOfTrips();

        double distanceAverage = 0, d;
        for (int i = 0; i < allLegs.length(); i++) {
            d = allLegs.getJSONObject(i).getJSONObject("distance").getInt("value");
            distanceAverage += d;
        }
        distanceAverage = distanceAverage/allLegs.length();

        double stopsDistanceVariance = 0;
        for (int i = 0; i < allLegs.length(); i++) {
            d = allLegs.getJSONObject(i).getJSONObject("distance").getInt("value");
            stopsDistanceVariance += Math.pow(d - distanceAverage, 2);
        }
        stopsDistanceVariance = stopsDistanceVariance/allLegs.length();

        //System.out.print("averageTripTime: " + averageTripTime + "\naverageWalkingTime: " + averageWalkingTime + "\nstopsDistanceVariance: " + stopsDistanceVariance + "\n");
        //System.out.print("Distance average: " + distanceAverage + "\n");

        Double[] objectives = new Double[3];
        objectives[0] = averageTripTime;
        objectives[1] = averageWalkingTime;
        objectives[2] = stopsDistanceVariance;
        return objectives;
    }

    public void simulateWalk(int fileNumber) throws Exception {
        double[] loc1 = randomLocationInsideBounds(getItinerary());
        double[] loc2 = randomLocationInsideBounds(getItinerary());

        JSONObject jsonObject = new GoogleAPIRequester().requestWalkingRoute(loc1[0], loc1[1], loc2[0], loc2[1]);

        if (!jsonObject.getString("status").equals("OK")) {
            return;
        }

        File f = new File("src\\main\\resources\\pointsData\\" + fileNumber + ".txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(jsonObject.toString());
        bw.close();

    }
}
