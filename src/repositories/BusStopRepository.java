package repositories;

import beans.BusStop;
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

    public BusStop getById(String id) {
        for (int i = 0; i < this.list.size(); i++) {
            if (this.list.get(i).getId().equals(id)) {
                return this.list.get(i);
            }
        }
        return null;
    }

    public int getListSize() {
        return list.size();
    }
}
