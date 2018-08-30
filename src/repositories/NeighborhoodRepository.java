package repositories;

import beans.City;
import beans.Neighborhood;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NeighborhoodRepository {
    private List<Neighborhood> list;
    private static NeighborhoodRepository instance; //Instância do repositório

    // Construtor privado por causa da utilização de singleton
    private NeighborhoodRepository() {
        list = new ArrayList<Neighborhood>();
    }

    // Retorna a instância do repositório
    public static NeighborhoodRepository getInstance() {
        if (instance == null) {
            instance = new NeighborhoodRepository();
        }
        return instance;
    }

    public void addNeighborhood(Neighborhood neighborhood) throws NullPointerException {
        if (neighborhood != null) {
            list.add(neighborhood);
        } else {
            throw new NullPointerException();
        }
    }

    public Neighborhood getByIndex(int i) {
        return this.list.get(i);
    }

    public Neighborhood getOrCreate(String neighborhoodName, City c) {
        Neighborhood n = new Neighborhood(neighborhoodName, c);
        for (int i = 0; i < list.size(); i++) {
            if (n.equals(list.get(i))) {
                return list.get(i);
            }
        }
        this.list.add(n);
        return n;
    }

    public void printList() {
        this.list.sort(Comparator.comparing(Neighborhood::getName));
        for (int i = 0; i < this.list.size(); i++) {
            System.out.print(this.list.get(i) + "\n");
        }
    }

    public int getListSize() {
        return list.size();
    }
}
