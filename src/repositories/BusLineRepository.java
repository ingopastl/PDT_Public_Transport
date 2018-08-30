package repositories;

import beans.BusLine;
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
        return this.list.get(i);
    }

    public BusLine getByID(String id) {
        for (int i = 0; i < this.list.size(); i++) {
            if (id.equals(this.list.get(i).getId())) {
                return this.list.get(i);
            }
        }
        return null;
    }

    public BusLine getOrCreate(String id, String name) {
        for (int i = 0; i < list.size(); i++) {
            if (this.list.get(i).getId().equals(id)) {
                return list.get(i);
            }
        }
        BusLine newLine = new BusLine(id, name);
        this.list.add(newLine);
        return newLine;
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
