package beans;

import control.CSVReader;
import control.GoogleRouteAPIRequester;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Itinerary {
    private BusLine busLine;
    private char serviceId;
    private String itineraryId;
    private String itineraryHeadsign;
    private List<ItineraryBusStop> stops;

    public Itinerary(BusLine busLine, char serviceId, String itineraryId, String itineraryHeadsign) {
        this.busLine = busLine;
        this.serviceId = serviceId;
        this.itineraryId = itineraryId;
        this.itineraryHeadsign = itineraryHeadsign;
        this.stops = new ArrayList<ItineraryBusStop>();
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

    public double getTotalTravelTime() throws Exception {
        File f = new File("src\\data\\SpBusLineData\\itinerary\\itinerariesJSON\\" + this.itineraryId);

        if (!f.exists()) {
            f.mkdir();
            GoogleRouteAPIRequester apiRequester = new GoogleRouteAPIRequester();
            apiRequester.requestRoute(this);
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

    public double getTotalTravelDistance() throws Exception {
        File f = new File("src\\data\\SpBusLineData\\itinerary\\itinerariesJSON\\" + this.itineraryId);

        if (!f.exists()) {
            f.mkdir();
            GoogleRouteAPIRequester apiRequester = new GoogleRouteAPIRequester();
            apiRequester.requestRoute(this);
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

    public double getStopsDistanceVariance() throws Exception {
        File f = new File("src\\data\\SpBusLineData\\itinerary\\itinerariesJSON\\" + this.itineraryId);
        if (!f.exists()) {
            f.mkdir();
            GoogleRouteAPIRequester apiRequester = new GoogleRouteAPIRequester();
            apiRequester.requestRoute(this);
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

    public double[] getBoudaries() throws Exception {
        CSVReader reader = new CSVReader();
        if (this.stops.size() == 0) {
            reader.readStopSequence("src\\data\\SpBusLineData\\itinerary\\stopSequence\\" + this.itineraryId + ".txt");
        }

        double highestLat, lowestLat, highestLong, lowestLong;
        List<ItineraryBusStop> stops = this.stops;

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

        double[] boundaryArray = new double[4];
        boundaryArray[0] = highestLat;
        boundaryArray[1] = lowestLat;
        boundaryArray[2] = highestLong;
        boundaryArray[3] = lowestLong;

        return  boundaryArray;
    }

    public void addItineraryBusStop(ItineraryBusStop ibs) throws NullPointerException {
        if (ibs != null) {
            this.stops.add(ibs);
        } else {
            throw new NullPointerException();
        }
    }

    public List<ItineraryBusStop> getStops() {
        return stops;
    }

    public void setStops(List<ItineraryBusStop> stops) {
        this.stops = stops;
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
