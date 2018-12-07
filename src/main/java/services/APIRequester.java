package services;

import beans.BusStop;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public abstract class APIRequester {

    public abstract JSONArray requestRoute(List<BusStop> route) throws Exception;

    public abstract JSONObject walkingRoute(double originLat, double originLong, double destinationLat, double destinationLong) throws Exception;
}
