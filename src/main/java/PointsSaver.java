import beans.Itinerary;
import repositories.BusLineRepository;
import repositories.BusStopRepository;
import repositories.ItineraryRepository;
import services.TCsimulator;

public class PointsSaver {
    public static void main(String[] args) throws Exception {
        BusStopRepository busStopRepository = BusStopRepository.getInstance();
        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        ItineraryRepository itineraryRepository = ItineraryRepository.getInstance();

        busStopRepository.readCSV("src\\main\\resources\\busData\\stops.txt");
        busLineRepository.readCSV("src\\main\\resources\\busData\\routes.txt");
        itineraryRepository.readCSV("src\\main\\resources\\busData\\itineraries\\itineraries.txt");

        Itinerary it = busLineRepository.getByID("423032").getItineraries().get(0);
        TCsimulator tCsimulator = new TCsimulator(it, 10, 800);

        for (int i = 0; i < 1400; i++) {
            tCsimulator.simulateWalk(i);
        }
    }
}