package repositories;

import beans.Street;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class StreetRepository {
    private List<Street> list;
    private static StreetRepository instance; //Instância do repositório

    // Construtor privado por causa da utilização de singleton
    private StreetRepository() {
        list = new ArrayList<Street>();
    }

    // Retorna a instância do repositório
    public static StreetRepository getInstance() {
        if (instance == null) {
            instance = new StreetRepository();
        }
        return instance;
    }

    public void addStreet(Street street) throws NullPointerException {
        if (street != null) {
            list.add(street);
        } else {
            throw new NullPointerException();
        }
    }

    public Street getOrCreate(String streetName) {
        Street s = new Street(streetName);
        for (int i = 0; i < list.size(); i++) {
            if (s.equals(list.get(i))) {
                return list.get(i);
            }
        }
        this.list.add(s);
        return s;
    }

    public void printList() {
        this.list.sort(Comparator.comparing(Street::getName));
        for (int i = 0; i < this.list.size(); i++) {
            System.out.print(this.list.get(i) + "\n");
        }
    }

    public int getListSize() {
        return list.size();
    }
}
