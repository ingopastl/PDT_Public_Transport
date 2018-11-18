import beans.Itinerary;
import services.*;
import repositories.BusLineRepository;
import repositories.BusStopRepository;
import repositories.ItineraryRepository;

public class Main {

    public static void main(String[] args) {
        BusStopRepository busStopRepository = BusStopRepository.getInstance();
        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        ItineraryRepository itineraryRepository = ItineraryRepository.getInstance();
        try{
            busStopRepository.readCSV("src\\data\\stops.txt");
            busLineRepository.readCSV("src\\data\\routes.txt");
            itineraryRepository.readCSV("src\\data\\itineraries\\itineraries.txt");

            BusLineRepository busLineRep = BusLineRepository.getInstance();
            Itinerary i = busLineRep.getByID("423032").getItineraries().get(0);
            TCsimulator simulator = new TCsimulator(i, 1, 800);
            System.out.print("Average walking time: " + simulator.getAverageWalkingTime());
            System.out.print("\nAverage trip time: " + simulator.getAverageTripTime());
            System.out.print("\nStops variance: " + simulator.getStopsVariance());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}