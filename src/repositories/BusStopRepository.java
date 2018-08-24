package repositories;

import beans.BusStop;
import java.util.ArrayList;

public class BusStopRepository {
    private ArrayList<BusStop> list;
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

    public int getListSize() {
        return list.size();
    }
}
