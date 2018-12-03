package services.microsoft;

import beans.BusStop;
import beans.Itinerary;
import beans.ItineraryBusStop;
import org.json.JSONArray;
import org.json.JSONObject;
import services.TripSimulator;

import java.util.ArrayList;

public class BingTripSimulator extends TripSimulator {
    public BingTripSimulator(Itinerary itinerary, int numberOfTrips, int radius) throws Exception{
        super(itinerary, numberOfTrips, radius);
    }

    public Double[] evaluate(Double[] vars) throws Exception {
        Itinerary itinerary = new Itinerary(null, '1', "1", "Solution");
        BusStop v;
        int i = 0, order = 0;
        while (i < vars.length) {
            v = new BusStop(Integer.toString(i), vars[i], vars[i+1]);
            ItineraryBusStop ibs = new ItineraryBusStop(v, itinerary, order);
            itinerary.addItineraryBusStop(ibs);
            order++;
            i += 2;
        }

        BingAPIRequester apiRequester = new BingAPIRequester();
        JSONArray jsonArray = apiRequester.requestRoute(itinerary.turnIntoBusStopList(itinerary.getStops()));

        JSONArray allLegs = new JSONArray();
        for (i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray legs = jsonObject.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getJSONArray("routeLegs");
            for (int j = 0; j < legs.length(); j++) {
                allLegs.put(legs.get(j));
            }
        }

        double averageWalkingTime = 0;
        double averageTripTime = 0;
        double stopsDistanceVariance;
        for (int t = 0; t < getNumberOfTrips(); t++) {
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
                        startWalkDistance = (int) startWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDistance");
                    }
                    int endWalkDuration = (int) endWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDuration");
                    try {
                        endWalkDistance = (double) endWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDistance");
                    } catch (ClassCastException e) {
                        endWalkDistance = (int) endWalkJson.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).get("travelDistance");
                    }

                    double totalTravelDistance = 0;
                    double totalTravelTime = 0;

                    for (i = start.getSequenceValue(); i < end.getSequenceValue(); i++) {
                        int duration = (int) allLegs.getJSONObject(i).get("travelDuration");
                        double distance;
                        try {
                            distance = (double) allLegs.getJSONObject(i).get("travelDistance");
                        } catch (ClassCastException e) {
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
        for (i = 0; i < allLegs.length(); i++) {
            try {
                d = (double) allLegs.getJSONObject(i).get("travelDistance");
            } catch (ClassCastException e) {
                d = (int) allLegs.getJSONObject(i).get("travelDistance");
            }
            x.add(d);
            distanceAverage += d;
        }
        distanceAverage = distanceAverage/x.size();

        double variance = 0;
        for (i = 0; i < x.size(); i++) {
            variance += Math.pow(x.get(i) - distanceAverage, 2);
        }

        averageTripTime /= getNumberOfTrips();
        averageWalkingTime /= getNumberOfTrips();
        stopsDistanceVariance = variance;

        Double[] objectives = new Double[3];
        objectives[0] = averageTripTime;
        objectives[1] = averageWalkingTime;
        objectives[2] = stopsDistanceVariance;

        //System.out.print("averageTripTime: " + averageTripTime + "\naverageWalkingTime: " + averageWalkingTime + "\nstopsDistanceVariance: " + stopsDistanceVariance + "\n");
        //System.out.print("Legs size: " + allLegs.length() + "\n");
        //System.out.print("Distance average: " + distanceAverage + "\n");

        return objectives;
    }
}
