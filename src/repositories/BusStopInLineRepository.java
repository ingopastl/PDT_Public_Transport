package repositories;

import beans.BusStopInLine;

import java.util.ArrayList;
import java.util.List;

public class BusStopInLineRepository {
    private List<BusStopInLine> list;
    private static BusStopInLineRepository instance;

    private BusStopInLineRepository() {
        list = new ArrayList<BusStopInLine>();
    }

    public static BusStopInLineRepository getInstance() {
        if (instance == null) {
            instance = new BusStopInLineRepository();
        }
        return instance;
    }

    public void addBusStopInLine(BusStopInLine relation) throws NullPointerException {
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
