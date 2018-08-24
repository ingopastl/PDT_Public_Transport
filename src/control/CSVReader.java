package control;

import beans.*;
import repositories.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CSVReader {

    public void readFile(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.ISO_8859_1));

        //Instâncias dos repositórios
        CityRepository cityRep = CityRepository.getInstance();
        NeighborhoodRepository neighRep = NeighborhoodRepository.getInstance();
        StreetRepository streetRep = StreetRepository.getInstance();
        BusStopRepository stopRep = BusStopRepository.getInstance();

        String currentLine = br.readLine(); //Pula a primeira linha
        while ((currentLine = br.readLine()) != null) {
            String stopId = "", streetName = "", neighborhoodName = "", cityName = "", stopLatitude = "", stopLongitude = "";
            int campo = 1;
            for (int i = 0; i < currentLine.length(); i++) {
                if (currentLine.charAt(i) != ';') {
                    if (campo == 1) {
                        stopId = stopId + currentLine.charAt(i);
                    } else if(campo == 2) {
                        streetName = streetName + currentLine.charAt(i);
                    } else if(campo == 3) {
                        neighborhoodName = neighborhoodName + currentLine.charAt(i);
                    } else if(campo == 4) {
                        cityName = cityName + currentLine.charAt(i);
                    } else if(campo == 5) {
                        stopLatitude = stopLatitude + currentLine.charAt(i);
                    } else if(campo == 6) {
                        stopLongitude = stopLongitude + currentLine.charAt(i);
                    }
                } else {
                    campo++;
                }
            }

            City c = cityRep.getOrCreate(cityName);
            Neighborhood n = neighRep.getOrCreate(neighborhoodName, c);
            c.addNeighborhood(n);
            Street s = streetRep.getOrCreate(streetName, c, n);
            s.addNeighborhood(n);
            n.addStreet(s);
            BusStop stop = new BusStop(stopId, s, n, c, stopLatitude, stopLongitude);
            stopRep.addBusStop(stop);
            s.addBusStop(stop);

        }

        stopRep.printList();
        //streetRep.printList();
        //neighRep.printList();
        //cityRep.printList();
    }
}
