import control.CSVReader;
import repositories.NeighborhoodRepository;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        CSVReader reader = new CSVReader();

        try{

            reader.readFile("CTMGrandeRecife_RuasParadasOnibus_dado.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }


        NeighborhoodRepository n = NeighborhoodRepository.getInstance();
    }
}
