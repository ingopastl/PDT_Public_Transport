package beans;

import java.util.ArrayList;
import java.util.List;

public class BusLine {
    private String id;
    private String shortName;
    private String longName;
    private List<Itinerary> itineraries;

    public BusLine(String id, String shortName, String longName) {
        this.id = id;
        this.shortName = shortName;
        this.longName = longName;
        this.itineraries = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String name) {
        this.shortName = name;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public List<Itinerary> getItineraries() {
        return itineraries;
    }

    public void setItineraries(List<Itinerary> itineraries) {
        this.itineraries = itineraries;
    }

    public void addItinerary(Itinerary it) throws NullPointerException {
        if (it != null) {
            itineraries.add(it);
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public String toString() {
        return "BusLine{" +
                "id=" + id +
                ", short name='" + shortName + '\'' +
                ", long name='" + longName + '\'' +
                '}';
    }
}
