package beans;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import repositories.BusStopRelationRepository;
import services.osrm.OsrmAPIRequester;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Itinerary {
    private BusLine busLine;
    private char serviceId;
    private String itineraryId;
    private String itineraryHeadsign;
    private List<BusStopRelation> stops;

    private Double totalTravelTime = null;
    private Double totalTravelDistance = null;
    private Double stopsDistanceAverage = null;
    private Double stopsDistanceVariance = null;

    public Itinerary(BusLine busLine, char serviceId, String itineraryId, String itineraryHeadsign) {
        this.busLine = busLine;
        this.serviceId = serviceId;
        this.itineraryId = itineraryId;
        this.itineraryHeadsign = itineraryHeadsign;
        this.stops = new ArrayList<>();
    }

    public BusLine getBusLine() {
        return busLine;
    }

    public void setBusLine(BusLine busLine) {
        this.busLine = busLine;
    }

    public char getServiceId() {
        return serviceId;
    }

    public void setServiceId(char serviceId) {
        this.serviceId = serviceId;
    }

    public String getItineraryId() {
        return itineraryId;
    }

    public void setItineraryId(String itineraryId) {
        this.itineraryId = itineraryId;
    }

    public String getItineraryHeadsign() {
        return itineraryHeadsign;
    }

    public void setItineraryHeadsign(String itineraryHeadsign) {
        this.itineraryHeadsign = itineraryHeadsign;
    }

    public Double getTotalTravelTime() throws Exception {
        if (this.totalTravelTime == null) {
            getRouteInfo();
        }
        return this.totalTravelTime;
    }

    public Double getTotalTravelDistance() throws Exception {
        if (this.totalTravelDistance == null) {
            getRouteInfo();
        }
        return this.totalTravelDistance;
    }

    public Double getStopsDistanceAverage() throws Exception {
        if (this.stopsDistanceAverage == null) {
            getRouteInfo();
        }
        return this.stopsDistanceAverage;
    }

    public Double getStopsDistanceVariance() throws Exception {
        if (this.stopsDistanceVariance == null) {
            getRouteInfo();
        }
        return this.stopsDistanceVariance;
    }

    private void processBingJson(JSONArray jsonArray) {
        if (jsonArray == null) {
            throw new NullPointerException();
        }

        //Distance in KM; Time in seconds.
        double totalDistance = 0, totalTime = 0;

        JSONArray allLegs = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            JSONArray legs = jsonObject.getJSONArray("resourceSets").getJSONObject(0).getJSONArray("resources").getJSONObject(0).getJSONArray("routeLegs");
            for (int j = 0; j < legs.length(); j++) {
                allLegs.put(legs.get(j));
            }
        }

        for (int i = 0; i < allLegs.length(); i++) {
            int duration = (int) allLegs.getJSONObject(i).get("travelDuration");
            double distance;
            try {
                distance = ((double) allLegs.getJSONObject(i).get("travelDistance")) * 1000; //Multiply by 1000 to convert into meters
            } catch (ClassCastException e) {
                distance = ((int) allLegs.getJSONObject(i).get("travelDistance")) * 1000;
            }
            totalTime += duration;
            totalDistance += ((int) distance);
        }

        this.stopsDistanceAverage = totalDistance/allLegs.length();

        double variance = 0;
        for (int i = 0; i < allLegs.length(); i++) {
            double distance;
            try {
                distance = ((double) allLegs.getJSONObject(i).get("travelDistance")) * 1000; //Multiply by 1000 to convert into meters
            } catch (ClassCastException e) {
                distance = ((int) allLegs.getJSONObject(i).get("travelDistance")) * 1000;
            }
            variance += Math.pow( ((int)distance) - this.stopsDistanceAverage, 2);
        }
        variance = variance/allLegs.length();

        this.stopsDistanceVariance = variance;
        this.totalTravelDistance = totalDistance;
        this.totalTravelTime = totalTime;
    }

    private void processGoogleJson(JSONArray jsonArray) {
        if (jsonArray == null) {
            throw new NullPointerException();
        }

        //Distance in meters; Time in seconds.
        double totalDistance = 0, totalTime = 0;

        JSONArray allLegs = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray routes = jsonObject.getJSONArray("routes");
            JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
            for (int j = 0; j < legs.length(); j++) {
                allLegs.put(legs.get(j));
            }
        }
        System.out.print("Legs: " + allLegs.length() + "\n");
        for (int i = 0; i < allLegs.length(); i++) {
            double duration = allLegs.getJSONObject(i).getJSONObject("duration").getDouble("value");
            double distance = allLegs.getJSONObject(i).getJSONObject("distance").getDouble("value");
            totalTime += duration;
            totalDistance += distance;
        }

        this.stopsDistanceAverage = totalDistance/allLegs.length();

        double variance = 0;
        int d;
        for (int i = 0; i < allLegs.length(); i++) {
            d = (int) allLegs.getJSONObject(i).getJSONObject("distance").get("value");
            double xMinusAverage = d - this.stopsDistanceAverage;
            variance = variance + Math.pow(xMinusAverage, 2);
        }
        variance = variance/allLegs.length();

        this.stopsDistanceVariance = variance;
        this.totalTravelDistance = totalDistance;
        this.totalTravelTime = totalTime;
    }

    private void processOsrmJason(JSONArray jsonArray) {
        if (jsonArray == null) {
            throw new NullPointerException();
        }

        //Distance in meters; Time in seconds.
        double totalDistance = 0, totalTime = 0;

        JSONObject jsonObject = (JSONObject) jsonArray.get(0);
        JSONArray routes = jsonObject.getJSONArray("routes");
        JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

        for (int i = 0; i < legs.length(); i++) {
            double duration = legs.getJSONObject(i).getDouble("duration");
            double distance = legs.getJSONObject(i).getDouble("distance");
            totalTime += duration;
            totalDistance += distance;
        }

        this.stopsDistanceAverage = totalDistance/legs.length();

        double variance = 0;
        double d;
        for (int i = 0; i < legs.length(); i++) {
            d = legs.getJSONObject(i).getDouble("distance");
            variance += Math.pow(d - this.stopsDistanceAverage, 2);
        }
        variance = variance/legs.length();

        this.stopsDistanceVariance = variance;
        this.totalTravelDistance = totalDistance;
        this.totalTravelTime = totalTime;
    }

    public JSONArray getRouteInfo() throws Exception {
        if (this.stops.size() == 0) {
            readStops();
        }

        JSONArray jsonArray;
        File f = new File("src" + File.separatorChar + "main" + File.separatorChar + "resources"
                + File.separatorChar + "SPTrans_Data" + File.separatorChar + "itineraries" + File.separatorChar
                + "itinerariesJSON" + File.separatorChar + this.itineraryId + ".json");
        if (!f.exists()) {
            OsrmAPIRequester apiRequester = new OsrmAPIRequester();
            jsonArray = apiRequester.requestRoute(turnIntoBusStopList(this.stops));
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(jsonArray.toString());
            bw.close();
        } else {
            jsonArray = new JSONArray(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
        }

        processOsrmJason(jsonArray);

        return jsonArray;
    }

    public double[] getBounds() throws IOException {
        if (this.stops.size() == 0) {
            readStops();
        }

        double highestLat, lowestLat, highestLong, lowestLong;
        List<BusStopRelation> stops = this.stops;

        highestLat = stops.get(0).getBusStop().getLatitude();
        lowestLat = stops.get(0).getBusStop().getLatitude();
        highestLong = stops.get(0).getBusStop().getLongitude();
        lowestLong = stops.get(0).getBusStop().getLongitude();
        for (int i = 1; i < stops.size(); i++) {
            double currentLat = stops.get(i).getBusStop().getLatitude();
            double currentLong = stops.get(i).getBusStop().getLongitude();

            if (currentLat > highestLat) {
                highestLat = currentLat;
            }
            if (currentLat < lowestLat) {
                lowestLat = currentLat;
            }
            if (currentLong > highestLong) {
                highestLong = currentLong;
            }
            if (currentLong < lowestLong) {
                lowestLong = currentLong;
            }
        }

        double[] boundsArray = new double[4];
        boundsArray[0] = highestLat;
        boundsArray[1] = lowestLat;
        boundsArray[2] = highestLong;
        boundsArray[3] = lowestLong;

        return  boundsArray;
    }

    public void addItineraryBusStop(BusStopRelation ibs) {
        if (ibs == null) {
            throw new NullPointerException();
        }
        this.stops.add(ibs);
    }

    public static List<BusStop> turnIntoBusStopList(List<BusStopRelation> l) {
        if (l == null) {
            throw new NullPointerException();
        }

        List<BusStop> list = new ArrayList<>();
        for (int i = 0; i < l.size(); i++) {
            list.add(l.get(i).getBusStop());
        }
        return list;
    }

    public List<BusStopRelation> getStops() throws IOException {
        if (this.stops.size() == 0) {
            readStops();
        }
        return stops;
    }

    public void setStops(List<BusStopRelation> stops) {
        this.stops = stops;
    }

    public void printInfo() throws Exception {
        System.out.print("Total travel time: " + getTotalTravelTime() + "\n"
        + "Total travel distance: " + getTotalTravelDistance() + "\n"
        + "Stops distance average: " + getStopsDistanceAverage() + "\n"
        + "Stops distance variance: " + getStopsDistanceVariance() + "\n");
    }

    private void readStops() throws IOException {
        BusStopRelationRepository busStopRelationRepository = BusStopRelationRepository.getInstance();
        busStopRelationRepository.readStopSequence("src" + File.separatorChar + "main" + File.separatorChar
                + "resources" + File.separatorChar + "SPTrans_Data" + File.separatorChar + "itineraries"
                + File.separatorChar + "stopSequence" + File.separatorChar + this.itineraryId + ".txt");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Itinerary itinerary = (Itinerary) o;
        return getServiceId() == itinerary.getServiceId() &&
                Objects.equals(getBusLine(), itinerary.getBusLine()) &&
                Objects.equals(getItineraryId(), itinerary.getItineraryId()) &&
                Objects.equals(getItineraryHeadsign(), itinerary.getItineraryHeadsign());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBusLine(), getServiceId(), getItineraryId(), getItineraryHeadsign());
    }

    @Override
    public String toString() {
        return "Itinerary{" +
                ", serviceId=" + serviceId +
                ", itineraryId='" + itineraryId + '\'' +
                ", itineraryHeadsign='" + itineraryHeadsign + '\'' +
                '}';
    }
}
