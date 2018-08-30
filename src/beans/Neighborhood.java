package beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Neighborhood {
    private String name;
    private City city;
    private List<Street> streets;

    public Neighborhood(String name, City city) {
        this.name = name;
        this.city = city;
        this.streets = new ArrayList<Street>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public List<Street> getStreets() {
        return streets;
    }

    public void setStreets(List<Street> streets) {
        this.streets = streets;
    }

    public void addStreet(Street s) throws NullPointerException {
        if (s != null) {
            for (int i = 0; i < this.streets.size(); i++) {
                if (s.equals(this.streets.get(i))) {
                    return;
                }
            }
            this.streets.add(s);
        } else {
            throw new NullPointerException();
        }
    }

    public void printStreets() {
        for (int i = 0; i < this.streets.size(); i++) {
            System.out.print(this.streets.get(i) + "\n");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Neighborhood that = (Neighborhood) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getCity(), that.getCity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCity());
    }

    @Override
    public String toString() {
        return "Neighborhood{" +
                "name='" + name + '\'' +
                '}';
    }
}
