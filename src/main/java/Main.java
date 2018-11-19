import beans.BusStop;
import beans.Itinerary;
import services.*;
import repositories.BusLineRepository;
import repositories.BusStopRepository;
import repositories.ItineraryRepository;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        BusStopRepository busStopRepository = BusStopRepository.getInstance();
        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        ItineraryRepository itineraryRepository = ItineraryRepository.getInstance();
        try{
            busStopRepository.readCSV("src\\main\\resources\\data\\stops.txt");
            busLineRepository.readCSV("src\\main\\resources\\data\\routes.txt");
            itineraryRepository.readCSV("src\\main\\resources\\data\\itineraries\\itineraries.txt");

            BusLineRepository busLineRep = BusLineRepository.getInstance();
            Itinerary i = busLineRep.getByID("423032").getItineraries().get(0);

            PTDJMetalProblem problem = new PTDJMetalProblem(i, 10, 800);
            problem.evaluate(problem.createSolution());

            //TCsimulator simulator = new TCsimulator(i, 1, 800);
            //System.out.print("Average walking time: " + simulator.getAverageWalkingTime());
            //System.out.print("\nAverage trip time: " + simulator.getAverageTripTime());
            //System.out.print("\nStops variance: " + simulator.getStopsDistanceVariance());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}