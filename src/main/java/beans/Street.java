package beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Street {
    private String name;
    private List<BusStop> busStops;

    public Street(String name) {
        this.name = name;
        this.busStops = new ArrayList<BusStop>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BusStop> getBusStops() {
        return busStops;
    }

    public void setBusStops(List<BusStop> busStops) {
        this.busStops = busStops;
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
        return Objects.equals(getName(), street.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return "Street{" +
                "name='" + name + '\'' +
                '}';
    }
}
