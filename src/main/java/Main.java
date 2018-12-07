import beans.Itinerary;

import jmetal.*;

import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import repositories.BusLineRepository;
import repositories.BusStopRepository;
import repositories.ItineraryRepository;

import java.io.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BusStopRepository busStopRepository = BusStopRepository.getInstance();
        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        ItineraryRepository itineraryRepository = ItineraryRepository.getInstance();
        try{
            busStopRepository.readCSV("src" + File.separatorChar + "main" + File.separatorChar
                    + "resources" + File.separatorChar + "busData" + File.separatorChar + "stops.txt");
            busLineRepository.readCSV("src" + File.separatorChar + "main" + File.separatorChar
                    + "resources" + File.separatorChar + "busData" + File.separatorChar + "routes.txt");
            itineraryRepository.readCSV("src" + File.separatorChar + "main" + File.separatorChar
                    + "resources" + File.separatorChar + "busData" + File.separatorChar + "itineraries"
                    + File.separatorChar + "itineraries.txt");

            Problem<DoubleSolution> problem;
            Algorithm<List<DoubleSolution>> algorithm;
            CrossoverOperator<DoubleSolution> crossover;
            MutationOperator<DoubleSolution> mutation;
            SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
            String referenceParetoFront = "";

            Itinerary i = busLineRepository.getByID("423032").getItineraries().get(0);
            problem = new PTDJMetalProblem(i, 1, 800);

            double crossoverProbability = 1.0;
            crossover = new PublicTransportNetworkCrossover(crossoverProbability);

            double mutationProbability = 1.0 / problem.getNumberOfVariables();
            mutation = new PublicTransportNetworkMutation(mutationProbability);

            selection = new BinaryTournamentSelection<DoubleSolution>();

            algorithm = new NSGAIIIBuilder<DoubleSolution>(problem).setPopulationSize(92).setMaxIterations(1).setCrossoverOperator(crossover).setMutationOperator(mutation)
                    .setSelectionOperator(selection).build();
            AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

            List<DoubleSolution> result = algorithm.getResult();
            long computingTime = algorithmRunner.getComputingTime();

            JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

            printFinalSolutionSet(result);
            //if (!referenceParetoFront.equals("")) {
                //printQualityIndicators(population, referenceParetoFront);
            //}
            saveLastPopulation(((NSGAIII<DoubleSolution>) algorithm).getPopulation());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the population into two files and prints some data on screen
     *
     * @param result
     */
    public static void printFinalSolutionSet(List<? extends Solution<?>> result) {

        new SolutionListOutput(result).setSeparator("\t")
                .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
                .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv")).print();

        JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
        JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
        JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
    }

    public static void saveLastPopulation(List<? extends Solution<?>> p) throws Exception {
        FileOutputStream fos = new FileOutputStream("src" + File.separatorChar + "main" + File.separatorChar
                + "resources" + File.separatorChar + "lastPopulation.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(p);
        oos.close();
    }

    public static List<? extends Solution<?>> loadLastPopulation() throws Exception {
        FileInputStream fis = new FileInputStream("src" + File.separatorChar + "main" + File.separatorChar
                + "resources" + File.separatorChar + "lastPopulation.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<? extends Solution<?>> p = (List<? extends Solution<?>>) ois.readObject();
        ois.close();
        return p;
    }
}