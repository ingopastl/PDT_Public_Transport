package repositories;

import beans.Neighborhood;
import beans.Street;
import beans.City;
import java.util.ArrayList;

public class StreetRepository {
    private ArrayList<Street> list;
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

    public Street getOrCreate(String streetName, City c, Neighborhood n) {
        Street s = new Street(streetName, c, n);
        for (int i = 0; i < list.size(); i++) {
            if (s.equals(list.get(i))) {
                return list.get(i);
            }
        }
        this.list.add(s);
        return s;
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
