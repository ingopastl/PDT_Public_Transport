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
            busStopRepository.readCSV("src\\data\\SpBusLineData\\stops.txt");
            busLineRepository.readCSV("src\\data\\SpBusLineData\\routes.txt");
            itineraryRepository.readCSV("src\\data\\SpBusLineData\\itineraries\\itineraries.txt");

            BusLineRepository busLineRep = BusLineRepository.getInstance();
            Itinerary i = busLineRep.getByID("423032").getItineraries().get(0);
            TCsimulator simulator = new TCsimulator(i, 20, 800);
            System.out.print("Average walking time: " + simulator.getAverageWalkingTime());
            System.out.print("\nAverage trip time: " + simulator.getAverageTripTime());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}