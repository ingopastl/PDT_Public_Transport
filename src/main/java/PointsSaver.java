import beans.Itinerary;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import repositories.BusLineRepository;
import repositories.BusStopRepository;
import repositories.ItineraryRepository;

import java.io.File;

public class PointsSaver {
    public static void main(String[] args) throws Exception {
        BusStopRepository busStopRepository = BusStopRepository.getInstance();
        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        ItineraryRepository itineraryRepository = ItineraryRepository.getInstance();

        busStopRepository.readCSV("src" + File.separatorChar + File.separatorChar + "main" + File.separatorChar
                        + "resources" + File.separatorChar + "busData" + File.separatorChar + "stops.txt");
        busLineRepository.readCSV("src" + File.separatorChar + File.separatorChar + "main" + File.separatorChar
                + "resources" + File.separatorChar + "busData" + File.separatorChar + "routes.txt");
        itineraryRepository.readCSV("src" + File.separatorChar + File.separatorChar + "main" + File.separatorChar
                + "resources" + File.separatorChar + "busData" + File.separatorChar + "itineraries" + File.separatorChar
                + "itineraries.txt");

        Itinerary it = busLineRepository.getByID("423032").getItineraries().get(0);
        JMetalRandom random = JMetalRandom.getInstance();

        System.out.print(random.nextInt(-1, 1) + "\n");
        System.out.print(random.nextInt(-1, 1) + "\n");
        System.out.print(random.nextInt(-1, 1) + "\n");
        System.out.print(random.nextInt(-1, 1) + "\n");
        System.out.print(random.nextInt(-1, 1) + "\n");
        System.out.print(random.nextInt(-1, 1) + "\n");

    }
}