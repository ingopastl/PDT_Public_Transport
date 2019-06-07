package beans;

import java.util.Objects;

public class BusStopRelation {
    private BusStop busStop;
    private Itinerary iti;
    private int order;

    public BusStopRelation(BusStop busStop, Itinerary iti, int order) {
        this.busStop = busStop;
        this.iti = iti;
        this.order = order;
    }

    public BusStop getBusStop() {
        return busStop;
    }

    public void setBusStop(BusStop busStop) {
        this.busStop = busStop;
    }

    public Itinerary getIti() {
        return iti;
    }

    public void setIti(Itinerary iti) {
        this.iti = iti;
    }

    public int getSequenceValue() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusStopRelation that = (BusStopRelation) o;
        return getSequenceValue() == that.getSequenceValue() &&
                Objects.equals(getBusStop(), that.getBusStop()) &&
                Objects.equals(getIti(), that.getIti());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBusStop(), getIti(), getSequenceValue());
    }

    @Override
    public String toString() {
        return "BusStopRelation{" +
                "busStop=" + busStop +
                ", iti=" + iti +
                ", order=" + order +
                '}';
    }
}
