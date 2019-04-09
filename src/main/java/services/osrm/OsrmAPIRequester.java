package services.osrm;

import beans.BusStop;

import org.json.JSONArray;
import org.json.JSONObject;

import services.APIRequester;
import services.HttpManager;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OsrmAPIRequester extends APIRequester {
    private static final String DEMOSERVER = "http://router.project-osrm.org/route/v1/";
    private static final String OWNSERVER = "http://192.168.0.19:5000/route/v1/";

    public JSONArray requestRoute(List<BusStop> route) throws Exception {
        if (route == null) {
            throw new NullPointerException();
        }

        JSONArray jsonArray = new JSONArray();

        String poly = getWaypointsPolyline(route);

        String url = OWNSERVER +
                "driving/" +
                "polyline(" +
                URLEncoder.encode(poly, StandardCharsets.UTF_8) +
                ")" +
                "?" +
                "geometries=geojson" +
                "&overview=full";

        //System.out.print(poly + '\n');
        System.out.print(url.toString() + '\n');

        String json = HttpManager.getInstance().requestGet(url);

        jsonArray.put(new JSONObject(json));

        return jsonArray;
    }

    public JSONObject requestWalkingRoute(double originLat, double originLong, double destinationLat, double destinationLong) throws Exception {
        List<BusStop> stops = new ArrayList<>();
        stops.add(new BusStop("start", originLat, originLong));
        stops.add(new BusStop("destination", destinationLat, destinationLong));

        String poly = getWaypointsPolyline(stops);
        String url = OWNSERVER + "foot/polyline(" + URLEncoder.encode(poly, StandardCharsets.UTF_8) + ")?overview=false";

        //String ur2 = OWNSERVER + "foot/polyline(" + poly + ")?overview=false";
        System.out.print(url + '\n');

        String json = HttpManager.getInstance().requestGet(url);

        return new JSONObject(json);
    }

}
