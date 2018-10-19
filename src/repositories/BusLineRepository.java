package repositories;

import beans.BusLine;
import control.CSVReader;

import java.util.ArrayList;
import java.util.List;

public class BusLineRepository {
    private List<BusLine> list;
    private static BusLineRepository instance;

    private BusLineRepository() {
        list = new ArrayList<BusLine>();
    }

    public static BusLineRepository getInstance() {
        if (instance == null) {
            instance = new BusLineRepository();
        }
        return instance;
    }

    public void addBusLine(BusLine line) throws NullPointerException {
        if (line != null) {
            list.add(line);
        } else {
            throw new NullPointerException();
        }
    }

    public BusLine getByIndex(int i) {
        if (i < this.list.size() && i >= 0) {
            return this.list.get(i);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    public BusLine getByID(String id) throws Exception {
        BusLine bl = null;
        for (int i = 0; i < this.list.size(); i++) {
            if (id.equals(this.list.get(i).getId())) {
                bl = this.list.get(i);
                break;
            }
        }

        return bl;
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
