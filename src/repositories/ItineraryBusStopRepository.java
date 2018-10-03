package repositories;

import beans.ItineraryBusStop;

import java.util.ArrayList;
import java.util.List;

public class ItineraryBusStopRepository {
    private List<ItineraryBusStop> list;
    private static ItineraryBusStopRepository instance;

    private ItineraryBusStopRepository() {
        list = new ArrayList<ItineraryBusStop>();
    }

    public static ItineraryBusStopRepository getInstance() {
        if (instance == null) {
            instance = new ItineraryBusStopRepository();
        }
        return instance;
    }

    public void addItineraryBusStop(ItineraryBusStop relation) throws NullPointerException {
        if (relation != null) {
            list.add(relation);
        } else {
            throw new NullPointerException();
        }
    }

    public void printList() {
        for (int i = 0; i < this.list.size(); i++) {
            System.out.print(this.list.get(i) + "\n");
        }
    }

    public int getListSize() {
        return list.size();
    }
}
