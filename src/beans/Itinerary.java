package beans;

import services.GoogleRouteAPIRequester;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import repositories.ItineraryBusStopRepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

    private Double totalTravelTime = null;
    private Double totalTravelDistance = null;
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
            requestRouteInfo();
        }
        return this.totalTravelTime;
    }

    public Double getTotalTravelDistance() throws Exception {
        if (this.totalTravelDistance == null) {
            requestRouteInfo();
        }
        return this.totalTravelDistance;
    }

    public Double getStopsDistanceVariance() throws Exception {
        if (this.stopsDistanceVariance == null) {
            requestRouteInfo();
        }
        return this.stopsDistanceVariance;
    }

    private void requestRouteInfo() throws Exception {
        ItineraryBusStopRepository itineraryBusStopRepository = ItineraryBusStopRepository.getInstance();
        if (this.stops.size() == 0) {
            itineraryBusStopRepository.readStopSequence("src\\data\\itineraries\\stopSequence\\" + this.itineraryId + ".txt");
        }

        JSONArray jsonArray;
        File f = new File("src\\data\\itineraries\\itinerariesJSON\\" + this.itineraryId + ".json");
        if (!f.exists()) {
            GoogleRouteAPIRequester apiRequester = new GoogleRouteAPIRequester();
            jsonArray = apiRequester.requestRoute(turnIntoBusStopList(this.stops));
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(jsonArray.toString());
            bw.close();
        } else {
            jsonArray = new JSONArray(FileUtils.readFileToString(f, StandardCharsets.UTF_8));
        }

        //Distance in meters; Time in seconds.
        double totalDistance = 0, totalTime = 0;
        //Distance average
        ArrayList<Double> x = new ArrayList<>();
        double distanceAverage = 0;

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            JSONArray routes = jsonObject.getJSONArray("routes");
            JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

            //System.out.print("Quantidade de pernas: " + legs.length() + "\n");

            for (int j = 0; j < legs.length(); j++) {
                int distance = (int) legs.getJSONObject(j).getJSONObject("distance").get("value");
                int duration = (int) legs.getJSONObject(j).getJSONObject("duration").get("value");
                totalTime += duration;
                totalDistance += distance;

                x.add((double) distance);
                distanceAverage += distance;
            }
        }
        distanceAverage = distanceAverage/x.size();
        System.out.print("\nAverage distance: " + distanceAverage);
        double variance = 0;
        for (int i = 0; i < x.size(); i++) {
            variance += Math.pow(x.get(i) - distanceAverage, 2);
        }

        this.totalTravelDistance = totalDistance;
        this.totalTravelTime = totalTime;
        this.stopsDistanceVariance = variance;
    }

    public double[] getBoudaries() throws Exception {
        ItineraryBusStopRepository itineraryBusStopRepository = ItineraryBusStopRepository.getInstance();
        if (this.stops.size() == 0) {
            itineraryBusStopRepository.readStopSequence("src\\data\\itineraries\\stopSequence\\" + this.itineraryId + ".txt");
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

    private List<BusStop> turnIntoBusStopList(List<ItineraryBusStop> l) {
        List<BusStop> list = new ArrayList<>();

        for (int i = 0; i < l.size(); i++) {
            list.add(l.get(i).getBusStop());
        }
        return list;
    }

    public List<ItineraryBusStop> getStops() throws Exception {
        ItineraryBusStopRepository itineraryBusStopRepository = ItineraryBusStopRepository.getInstance();
        if (this.stops.size() == 0) {
            itineraryBusStopRepository.readStopSequence("src\\data\\itineraries\\stopSequence\\" + this.itineraryId + ".txt");
        }
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
