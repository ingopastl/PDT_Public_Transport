package beans;

import java.util.Objects;

public class BusStopInLine {
    private BusStop busStop;
    private BusLine line;
    private String itineraryName;
    private int ttrOrder;
    private int rttOrder;

    public BusStopInLine(BusStop busStop, BusLine line, String itineraryName, int ttrOrder, int rttOrder) {
        this.busStop = busStop;
        this.line = line;
        this.itineraryName = itineraryName;
        this.ttrOrder = ttrOrder;
        this.rttOrder = rttOrder;
    }

    public BusStop getBusStop() {
        return busStop;
    }

    public void setBusStop(BusStop busStop) {
        this.busStop = busStop;
    }

    public BusLine getLine() {
        return line;
    }

    public void setLine(BusLine line) {
        this.line = line;
    }

    public String getItineraryName() {
        return itineraryName;
    }

    public void setItineraryName(String itineraryName) {
        this.itineraryName = itineraryName;
    }

    public int getTtrOrder() {
        return ttrOrder;
    }

    public void setTtrOrder(int ttrOrder) {
        this.ttrOrder = ttrOrder;
    }

    public int getRttOrder() {
        return rttOrder;
    }

    public void setRttOrder(int rttOrder) {
        this.rttOrder = rttOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusStopInLine that = (BusStopInLine) o;
        return Objects.equals(getBusStop(), that.getBusStop()) &&
                Objects.equals(getLine(), that.getLine()) &&
                Objects.equals(getItineraryName(), that.getItineraryName()) &&
                Objects.equals(getTtrOrder(), that.getTtrOrder()) &&
                Objects.equals(getRttOrder(), that.getRttOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBusStop(), getLine(), getItineraryName(), getTtrOrder(), getRttOrder());
    }

    @Override
    public String toString() {
        return busStop + "; " + line +
                "; itineraryName='" + itineraryName + '\'' +
                "; ttrOrder=" + ttrOrder +
                "; rttOrder=" + rttOrder +
                '}';
    }
}
