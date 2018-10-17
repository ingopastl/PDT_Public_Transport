package control;

import beans.BusLine;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class PDT {

    public double getTravelTime(BusLine bl, int direction) throws Exception {
        File f = new File("src\\data\\SpBusLineData\\itinerary\\itinerariesJSON\\" + bl.getItineraries().get(direction).getItineraryId());

        if (!f.exists()) {
            f.mkdir();
            GoogleRouteAPIRequester apiRequester = new GoogleRouteAPIRequester();
            apiRequester.requestRoute(bl, direction);
        }

        File[] files = f.listFiles();

        //Time in seconds.
        double time = 0;

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String json = FileUtils.readFileToString(files[i], StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(json);
                JSONArray routes = jsonObject.getJSONArray("routes");
                JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

                //System.out.print("Quantidade de pernas: " + legs.length() + "\n");

                for (int j = 0; j < legs.length(); j++) {
                    int duration = (int) legs.getJSONObject(j).getJSONObject("duration").get("value");
                    time += duration;
                }
            }
            System.out.print(time + "\n");
        }else {
            throw new NullPointerException();
        }
        return time;
    }

    public double getTotalTravelDistance(BusLine bl, int direction) throws Exception {
        File f = new File("src\\data\\SpBusLineData\\itinerary\\itinerariesJSON\\" + bl.getItineraries().get(direction).getItineraryId());

        if (!f.exists()) {
            f.mkdir();
            GoogleRouteAPIRequester apiRequester = new GoogleRouteAPIRequester();
            apiRequester.requestRoute(bl, direction);
        }

        File[] files = f.listFiles();

        //Distance in meters.
        double totalDistance = 0;

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String json = FileUtils.readFileToString(files[i], StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(json);
                JSONArray routes = jsonObject.getJSONArray("routes");
                JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

                //System.out.print("Quantidade de pernas: " + legs.length() + "\n");

                for (int j = 0; j < legs.length(); j++) {
                    int distance = (int) legs.getJSONObject(j).getJSONObject("distance").get("value");
                    totalDistance += distance;
                }
            }
            System.out.print(totalDistance + "\n");
        }else {
            throw new NullPointerException();
        }
        return totalDistance;
    }

    public double getDistanceVariance(BusLine bl, int direction) throws Exception {
        File f = new File("src\\data\\SpBusLineData\\itinerary\\itinerariesJSON\\" + bl.getItineraries().get(direction).getItineraryId());
        if (!f.exists()) {
            f.mkdir();
            GoogleRouteAPIRequester apiRequester = new GoogleRouteAPIRequester();
            apiRequester.requestRoute(bl, direction);
        }
        File[] files = f.listFiles();

        ArrayList<Double> x = new ArrayList<Double>();
        double average = 0;

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String json = FileUtils.readFileToString(files[i], StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(json);
                JSONArray routes = jsonObject.getJSONArray("routes");
                JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

                for (int j = 0; j < legs.length(); j++) {
                    double d = (int) legs.getJSONObject(j).getJSONObject("distance").get("value");
                    x.add(d);
                    average += d;
                }
            }
            average = average/x.size();

            double x2_sum = 0;
            for (int i = 0; i < x.size(); i++) {
                x2_sum += Math.pow(x.get(i) - average, 2);
            }

            return x2_sum/x.size();
        }else {
            throw new FileNotFoundException();
        }
    }
}
