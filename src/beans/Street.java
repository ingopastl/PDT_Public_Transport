package beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Street {
    private String name;
    private City city;
    private List<Neighborhood> neighborhoods;
    private List<BusStop> busStops;

    public Street(String name, City c, Neighborhood n) {
        this.name = name;
        this.city = c;
        this.neighborhoods = new ArrayList<Neighborhood>();
        this.busStops = new ArrayList<BusStop>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public List<Neighborhood> getNeighborhoods() {
        return neighborhoods;
    }

    public void setNeighborhoods(List<Neighborhood> neighborhoods) {
        this.neighborhoods = neighborhoods;
    }

    public List<BusStop> getBusStops() {
        return busStops;
    }

    public void setBusStops(List<BusStop> busStops) {
        this.busStops = busStops;
    }

    public void addNeighborhood(Neighborhood n) {
        if (n != null) {
            this.neighborhoods.add(n);
        } else {
            throw new NullPointerException();
        }
    }

    public void addBusStop(BusStop stop) {
        if (stop != null) {
            this.busStops.add(stop);
        } else {
            throw new NullPointerException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Street street = (Street) o;
        return Objects.equals(getName(), street.getName()) &&
                Objects.equals(getCity(), street.getCity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCity());
    }

    @Override
    public String toString() {
        return "Street{" +
                "name='" + name + '\'' +
                '}';
    }
}
