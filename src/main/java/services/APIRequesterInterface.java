package services;

import beans.BusStop;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public interface APIRequesterInterface {
    JSONArray requestRoute(List<BusStop> route) throws Exception;
    JSONObject walkingRoute(double originLat, double originLong, double destinationLat, double destinationLong) throws Exception;
}
