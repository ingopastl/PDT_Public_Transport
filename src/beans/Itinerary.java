package beans;

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

    public void addItineraryBusStop(ItineraryBusStop ibs) throws NullPointerException {
        if (ibs != null) {
            this.stops.add(ibs);
        } else {
            throw new NullPointerException();
        }
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
