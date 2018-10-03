package repositories;

import beans.Itinerary;

import java.util.ArrayList;
import java.util.List;

public class ItineraryRepository {
    private List<Itinerary> list;
    private static ItineraryRepository instance;

    private ItineraryRepository() {
        list = new ArrayList<Itinerary>();
    }

    public static ItineraryRepository getInstance() {
        if (instance == null) {
            instance = new ItineraryRepository();
        }
        return instance;
    }

    public void addItinerary(Itinerary it) throws NullPointerException {
        if (it != null) {
            list.add(it);
        } else {
            throw new NullPointerException();
        }
    }

    public Itinerary getById(String id) {
        for (int i = 0; i < this.list.size(); i++) {
            if (this.list.get(i).getItineraryId().equals(id)) {
                return this.list.get(i);
            }
        }
        return null;
    }

    public void printList() {
        for (int i = 0; i < this.list.size(); i++) {
            System.out.print(this.list.get(i) + "\n");
        }
    }
}
