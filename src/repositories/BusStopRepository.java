package repositories;

import beans.BusStop;
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

    public boolean has(double lat, double longe) {
        for (int i = 0; i < this.list.size(); i++) {
            if (this.list.get(i).getLatitude() == lat && this.list.get(i).getLongitude() == longe){
                return true;
            }
        }
        return false;
    }

    public int getListSize() {
        return list.size();
    }
}
