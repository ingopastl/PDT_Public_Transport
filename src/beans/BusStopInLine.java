package beans;

import java.util.Objects;

public class BusStopInLine {
    private BusStop busStop;
    private BusLine line;
    private String itineraryName;
    private String direction;
    private int directionID;
    private int ttrOrder;
    private int rttOrder;

    public BusStopInLine(BusStop busStop, BusLine line, String itineraryName, String direction, int directionID, int ttrOrder, int rttOrder) {
        this.busStop = busStop;
        this.line = line;
        this.itineraryName = itineraryName;
        this.direction = direction;
        this.directionID = directionID;
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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getDirectionID() {
        return directionID;
    }

    public void setDirectionID(int directionID) {
        this.directionID = directionID;
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
                Objects.equals(getDirection(), that.getDirection()) &&
                Objects.equals(getDirectionID(), that.getDirectionID()) &&
                Objects.equals(getTtrOrder(), that.getTtrOrder()) &&
                Objects.equals(getRttOrder(), that.getRttOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBusStop(), getLine(), getItineraryName(), getDirection(), getDirectionID(), getTtrOrder(), getRttOrder());
    }

    @Override
    public String toString() {
        return busStop + "; " + line +
                "; itineraryName='" + itineraryName + '\'' +
                "; direction='" + direction + '\'' +
                "; directionID=" + directionID +
                "; ttrOrder=" + ttrOrder +
                "; rttOrder=" + rttOrder +
                '}';
    }
}
