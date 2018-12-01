package services.google;

import beans.BusStop;
import beans.Itinerary;
import beans.ItineraryBusStop;

import org.json.JSONArray;
import org.json.JSONObject;
import services.TripSimulator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class GoogleTripSimulator extends TripSimulator {
    public GoogleTripSimulator(Itinerary itinerary, int numberOfTrips, int radius) throws Exception{
        super(itinerary, numberOfTrips, radius);
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

        JSONArray jsonArray = itinerary.getRouteInfo();

        double averageWalkingTime = 0;
        double averageTripTime = 0;
        double stopsDistanceVariance = 0;

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
        averageTripTime /= getNumberOfTrips();
        averageWalkingTime /= getNumberOfTrips();
        //stopsDistanceVariance = itinerary.getStopsDistanceVariance();

        Double[] objectives = new Double[3];
        objectives[0] = averageTripTime;
        objectives[1] = averageWalkingTime;
        objectives[2] = stopsDistanceVariance;

        return objectives;
    }

    public void simulateWalk(int fileNumber) throws Exception {
        double[] loc1 = randomLocationBeta(getItinerary());
        double[] loc2 = randomLocationBeta(getItinerary());

        JSONObject jsonObject = new GoogleRouteAPIRequester().walkingRoute(loc1[0], loc1[1], loc2[0], loc2[1]);

        if (!jsonObject.getString("status").equals("OK")) {
            return;
        }

        File f = new File("src\\main\\resources\\pointsData\\" + fileNumber + ".txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(f));
        bw.write(jsonObject.toString());
        bw.close();

    }
}
