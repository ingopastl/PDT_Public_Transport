package services;

import beans.BusStop;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BingAPIRequester {
    private static final String DEFAULTURL = "http://dev.virtualearth.net/REST/V1/Routes";
    private static final String APIKEY = "AoJv1xw1Uep6vS66y74KiTEzsOsWIH3AG_IALi0M4G6WTEBPVRyPhM9IsDxCI7KZ";

    public JSONArray requestRoute(List<BusStop> route) throws Exception {
        if (route == null) {
            throw new NullPointerException();
        }

        JSONArray jsonArray = new JSONArray();
        HttpClient client = HttpClientBuilder.create().build();

        StringBuilder url = new StringBuilder();
        url.append(DEFAULTURL);
        url.append("?");

        int count = 0, urlCount = 0;
        while (count < route.size()) {
            while (urlCount < 25 && count < route.size()) {
                url.append("wp.");
                url.append(count);
                url.append("=");
                url.append(route.get(count).getLatitude());
                url.append(",");
                url.append(route.get(count).getLongitude());
                url.append("&");
                count++;
                urlCount++;
            }
            url.append("key=");
            url.append(APIKEY);
            System.out.print(url.toString() + '\n');

            HttpPost postRequest = new HttpPost(url.toString());
            HttpResponse response = client.execute(postRequest);
            String json = EntityUtils.toString(response.getEntity(), "UTF-8");

            jsonArray.put(new JSONObject(json));

            urlCount = 0;
            url.delete(0, url.length());
            url.append(DEFAULTURL);
        }

        return jsonArray;
    }

    public JSONObject walkingRoute(double originLat, double originLong, double destinationLat, double destinationLong) throws Exception {
        String originCoordinate = originLat + "," + originLong;
        String destinationCoodinate = destinationLat + "," + destinationLong;

        String url = DEFAULTURL + "/Walking?wp.0=" + originCoordinate + "&wp.1=" + destinationCoodinate + "&key=" + APIKEY;
        System.out.print(url + '\n');

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(url);
        HttpResponse response = client.execute(getRequest);
        String json = EntityUtils.toString(response.getEntity(), "UTF-8");

        System.out.print(json + "\n");

        return new JSONObject(json);
    }
}
