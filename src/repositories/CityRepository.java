package repositories;

import beans.City;

import java.util.ArrayList;
import java.util.List;

public class CityRepository {
    private List<City> list;
    private static CityRepository instance;

    private CityRepository() {
        this.list = new ArrayList<City>();
    }

    public static CityRepository getInstance() {
        if (instance == null) {
            instance = new CityRepository();
        }
        return instance;
    }

    public void addCity(City city) throws NullPointerException {
        if (city != null) {
            list.add(city);
        } else {
            throw new NullPointerException();
        }
    }

    //Retorna nulo se não existir uma cidade com o nome pesquisado
    public City getOrCreate(String cityName) {
        City c = new City(cityName);
        for (int i = 0; i < this.list.size(); i++) {
            if (c.equals(this.list.get(i))) {
                return this.list.get(i);
            }
        }
        this.list.add(c);
        return c;
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
