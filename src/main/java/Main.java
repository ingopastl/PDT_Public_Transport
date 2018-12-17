import beans.Itinerary;

import jmetal.*;

import org.uma.jmetal.solution.impl.DefaultDoubleSolution;
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
import java.util.ArrayList;
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
            problem = new PTDJMetalProblem(i, 30, 800);

            double crossoverProbability = 1.0;
            crossover = new PublicTransportNetworkCrossover(crossoverProbability);

            double mutationProbability = 1.0 / problem.getNumberOfVariables();
            mutation = new PublicTransportNetworkMutation(mutationProbability);

            selection = new BinaryTournamentSelection<>();

            algorithm = new NSGAIIIBuilder<>(problem).setPopulationSize(92).setMaxIterations(100).setCrossoverOperator(crossover).setMutationOperator(mutation)
                    .setSelectionOperator(selection).build();
            AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

            List<DoubleSolution> result = algorithm.getResult();
            long computingTime = algorithmRunner.getComputingTime();

            JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");

            printFinalSolutionSet(result);
            //if (!referenceParetoFront.equals("")) {
                //printQualityIndicators(population, referenceParetoFront);
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the population into two files and prints some data on screen
     *
     * @param result
     */
    private static void printFinalSolutionSet(List<? extends Solution<?>> result) {

        new SolutionListOutput(result).setSeparator("\t")
                .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
                .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv")).print();

        JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
        JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
        JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
    }

    private static List<DoubleSolution> loadPopulation(PTDJMetalProblem problem) throws Exception {
        File f = new File("src" + File.separatorChar + "main" + File.separatorChar + "resources" + File.separatorChar + "progress.txt");
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        br.readLine();

        List<DoubleSolution> list = new ArrayList<>();

        String line;
        while ((line = br.readLine()) != null) {
            DoubleSolution solution = new DefaultDoubleSolution(problem);
            StringBuilder lat = new StringBuilder(), longi = new StringBuilder();

            int evenOdd = 0, solutionIndex = 0;
            for (int i = 0; i < line.length(); i++) {
                char currentChar = line.charAt(i);
                if (currentChar == ',') {
                    evenOdd++;
                } else if (currentChar == ';') {
                    evenOdd++;
                    solution.setVariableValue(solutionIndex, Double.parseDouble(lat.toString()));
                    ++solutionIndex;
                    solution.setVariableValue(solutionIndex, Double.parseDouble(longi.toString()));
                    ++solutionIndex;

                    lat.delete(0, lat.length());
                    longi.delete(0, longi.length());
                } else {
                    if (evenOdd%2 == 0) {
                        lat.append(currentChar);
                    } else {
                        longi.append(currentChar);
                    }
                }
            }
            list.add(solution);
        }

        return list;
    }
}