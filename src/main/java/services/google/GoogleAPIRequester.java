package services.google;

import beans.BusStop;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import services.APIRequester;
import services.HttpManager;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GoogleAPIRequester extends APIRequester {
    private static final String DEFAULTURL = "https://maps.googleapis.com/maps/api/directions/json?";
    private static final String APIKEY = "AIzaSyAOfkS5vP9_OGo52BcFEg5uvPXiqX0cq9o";

    public JSONArray requestRoute(List<BusStop> route) throws Exception {
        if (route == null) {
            throw new NullPointerException();
        }

        JSONArray jsonArray = new JSONArray();

        List<BusStop> routeWaypoints = new ArrayList<>();
        BusStop start, end;
        while (route.size() > 1) {
            routeWaypoints.clear();
            if (route.size() >= 25) {
                start = route.remove(0);
                for (int i = 0; i < 23; i++) {
                    routeWaypoints.add(route.remove(0));
                }
                end = route.get(0);
            } else {
                if (route.size() > 2) {
                    int routeSize = route.size();
                    start = route.remove(0);
                    for (int j = 0; j < routeSize - 2; j++) {
                        routeWaypoints.add(route.remove(0));
                    }
                    end = route.get(0);
                } else {
                    start = route.remove(0);
                    end = route.get(0);
                }
            }

            String originCoordinate = start.getLatitude() + "," + start.getLongitude();
            String destinationCoodinate = end.getLatitude() + "," + end.getLongitude();

            String routePoly;
            if (routeWaypoints.size() > 0) {
                routePoly = getWaypointsPolyline(routeWaypoints);
            } else {
                routePoly = "";
            }

            String url = DEFAULTURL + "origin=" + URLEncoder.encode(originCoordinate, StandardCharsets.UTF_8) + "&destination=" + URLEncoder.encode(destinationCoodinate, StandardCharsets.UTF_8) + "&waypoints=enc:" + URLEncoder.encode(routePoly, StandardCharsets.UTF_8) + ":&key=" + APIKEY;
            String url2 = DEFAULTURL + "origin=" + originCoordinate + "&destination=" + destinationCoodinate + "&waypoints=enc:" + routePoly + ":&key=" + APIKEY;
            System.out.print(url2 + '\n');

            String json = HttpManager.getInstance().requestPost(url);

            jsonArray.put(new JSONObject(json));
        }

        return jsonArray;
    }

    public JSONObject requestWalkingRoute(double originLat, double originLong, double destinationLat, double destinationLong) throws Exception {
        String originCoordinate = originLat + "," + originLong;
        String destinationCoodinate = destinationLat + "," + destinationLong;

        String url = DEFAULTURL + "origin=" + originCoordinate + "&destination=" + destinationCoodinate + "&mode=walking&key=" + APIKEY;
        System.out.print(url + '\n');

        String json = HttpManager.getInstance().requestPost(url);

        return new JSONObject(json);
    }
}
