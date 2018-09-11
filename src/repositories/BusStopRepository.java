package repositories;

import beans.BusStop;
import beans.City;
import beans.Neighborhood;
import beans.Street;

import java.util.ArrayList;
import java.util.List;

public class BusStopRepository {
    private List<BusStop> list;
    private static BusStopRepository instance;

    private BusStopRepository() {
        list = new ArrayList<BusStop>();
    }

    public static BusStopRepository getInstance() {
        if (instance == null) {
            instance = new BusStopRepository();
        }
        return instance;
    }

    public void addBusStop(BusStop stop) throws NullPointerException {
        if (stop != null) {
            list.add(stop);
        } else {
            throw new NullPointerException();
        }
    }

    public void printList() {
        for (int i = 0; i < this.list.size(); i++) {
            System.out.print(this.list.get(i) + "\n");
        }
    }

    public BusStop getOrCreate(String id, Street s, Neighborhood n, City c, double latitude, double longitude) {
        for (int i = 0; i < this.list.size(); i++) {
            if (this.list.get(i).getId().equals(id)) {
                return this.list.get(i);
            }
        }
        BusStop stop = new BusStop(id, s, n, c, latitude, longitude);
        this.list.add(stop);
        return stop;
    }

    public int getListSize() {
        return list.size();
    }
}
