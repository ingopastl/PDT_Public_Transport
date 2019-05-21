package services.microsoft;

import beans.BusStopRelation;
import beans.Itinerary;
import org.json.JSONArray;
import org.json.JSONObject;
import services.TripSimulator;

public class BingTripSimulator extends TripSimulator {

    public BingTripSimulator(Itinerary itinerary, int numberOfTrips, int radius) throws Exception{
        super(itinerary, numberOfTrips, radius);
    }

    public Double[] evaluate(Double[] vars) throws Exception {
        Itinerary itinerary = turnIntoItinerary(vars);

        BingAPIRequester apiRequester = new BingAPIRequester();
        JSONArray jsonArray = apiRequester.requestRoute(Itinerary.turnIntoBusStopList(itinerary.getStops()));

        JSONArray allLegs = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray legs = jsonObject.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getJSONArray("routeLegs");
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

                if (!startWalkJson.getString("statusDescription").equals("OK") || !endWalkJson.getString("statusDescription").equals("OK")) {
                    --t;
                } else {
                    int startWalkDuration = startWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getInt("travelDuration");
                    int endWalkDuration = endWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getInt("travelDuration");

                    double totalTravelTime = 0;
                    for (int i = start.getSequenceValue(); i < end.getSequenceValue(); i++) {
                        int duration = allLegs.getJSONObject(i).getInt("travelDuration");
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
            d = allLegs.getJSONObject(i).getDouble("travelDistance") * 1000;
            distanceAverage += d;
        }
        distanceAverage = distanceAverage/allLegs.length();

        double stopsDistanceVariance = 0;
        for (int i = 0; i < allLegs.length(); i++) {
            d = allLegs.getJSONObject(i).getDouble("travelDistance") * 1000;
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
}
