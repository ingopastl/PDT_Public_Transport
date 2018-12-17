package services.microsoft;

import beans.BusStop;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import services.APIRequester;
import services.HttpManager;

import java.util.List;

public class BingAPIRequester extends APIRequester {
    private static final String DEFAULTURL = "http://dev.virtualearth.net/REST/V1/Routes";
    private static final String APIKEY = "AoJv1xw1Uep6vS66y74KiTEzsOsWIH3AG_IALi0M4G6WTEBPVRyPhM9IsDxCI7KZ";

    public JSONArray requestRoute(List<BusStop> route) throws Exception {
        if (route == null) {
            throw new NullPointerException();
        }

        JSONArray jsonArray = new JSONArray();

        StringBuilder url = new StringBuilder();
        url.append(DEFAULTURL);
        url.append("?");

        int count;
        while (route.size() > 1) {
            count = 0;
            if (route.size() >= 25) {
                for (int i = 0; i < 24; i++) {
                    BusStop r = route.remove(0);
                    url.append("wp.");
                    url.append(count);
                    url.append("=");
                    url.append(r.getLatitude());
                    url.append(",");
                    url.append(r.getLongitude());
                    url.append("&");
                    count++;
                }
                url.append("wp.");
                url.append(count);
                url.append("=");
                url.append(route.get(0).getLatitude());
                url.append(",");
                url.append(route.get(0).getLongitude());
                url.append("&");
            } else {
                int routeSize = route.size();
                for (int j = 0; j < routeSize; j++) {
                    BusStop r = route.remove(0);
                    url.append("wp.");
                    url.append(count);
                    url.append("=");
                    url.append(r.getLatitude());
                    url.append(",");
                    url.append(r.getLongitude());
                    url.append("&");
                    count++;
                }
            }
            url.append("key=");
            url.append(APIKEY);
            System.out.print(url.toString() + '\n');

            String json = HttpManager.getInstance().requestGet(url.toString());

            jsonArray.put(new JSONObject(json));

            url.delete(0, url.length());
            url.append(DEFAULTURL);
            url.append("?");
        }

        return jsonArray;
    }

    public JSONObject requestWalkingRoute(double originLat, double originLong, double destinationLat, double destinationLong) throws Exception {
        String originCoordinate = originLat + "," + originLong;
        String destinationCoodinate = destinationLat + "," + destinationLong;

        String url = DEFAULTURL + "/Walking?wp.0=" + originCoordinate + "&wp.1=" + destinationCoodinate + "&key=" + APIKEY;
        System.out.print(url + '\n');

        String json = HttpManager.getInstance().requestGet(url);

        return new JSONObject(json);
    }
}
