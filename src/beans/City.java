package beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class City {
    private String name;
    private List<Neighborhood> neighborhoods;

    public City(String name) {
        this.name = name;
        this.neighborhoods = new ArrayList<Neighborhood>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Neighborhood> getNeighborhoods() {
        return neighborhoods;
    }

    public void setNeighborhoods(List<Neighborhood> neighborhoods) {
        this.neighborhoods = neighborhoods;
    }

    public void addNeighborhood(Neighborhood n) throws NullPointerException {
        if (n != null) {
            for (int i = 0; i < this.neighborhoods.size(); i++) {
                if (n.equals(this.neighborhoods.get(i))) {
                    return;
                }
            }
            this.neighborhoods.add(n);
        } else {
            throw new NullPointerException();
        }
    }

    public void printNeighborhoods() {
        for (int i = 0; i < this.neighborhoods.size(); i++) {
            System.out.print(this.neighborhoods.get(i) + "\n");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        City oCity = (City) o;
        return Objects.equals(getName(), oCity.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                '}';
    }
}
