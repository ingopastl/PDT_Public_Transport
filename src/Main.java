import control.CSVReader;
import repositories.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        CSVReader reader = new CSVReader();

        try{

            reader.readBusStops("CTMGrandeRecife_RuasParadasOnibus_dado.csv");
            reader.readBusLines("CTMGrandeRecife_LinhaParadaTrajeto_dado.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }


        //Instâncias dos repositórios
        CityRepository cityRep = CityRepository.getInstance();
        NeighborhoodRepository neighRep = NeighborhoodRepository.getInstance();
        StreetRepository streetRep = StreetRepository.getInstance();
        BusStopRepository stopRep = BusStopRepository.getInstance();

        stopRep.printList();
    }
}
