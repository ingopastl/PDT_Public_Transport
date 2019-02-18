import beans.Itinerary;

import jmetal.*;

import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.qualityindicator.impl.hypervolume.WFGHypervolume;
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
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import repositories.BusLineRepository;
import repositories.BusStopRepository;
import repositories.ItineraryRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
            String referenceParetoFront = "referenceParetoFront.pf";

            Itinerary iti = busLineRepository.getByID("423032").getItineraries().get(0);
            problem = new PTDJMetalProblem(iti, 30, 800);

            double crossoverProbability = 1.0;
            crossover = new PublicTransportNetworkCrossover(crossoverProbability);
            double mutationProbability = 1.0 / problem.getNumberOfVariables();
            mutation = new PublicTransportNetworkMutation(mutationProbability);
            selection = new BinaryTournamentSelection<>();

            List<DoubleSolution> initialPopulation;

            long computingTime = 0;
            for (int i = 0; i < 40; i++) {
                initialPopulation = loadInitialPopulation((PTDJMetalProblem) problem);

                algorithm = new NSGAIIIBuilder<>(problem).setPopulationSize(92).setMaxIterations(10).setCrossoverOperator(crossover).setMutationOperator(mutation)
                        .setSelectionOperator(selection).setInitialPopulation(initialPopulation).build();
                AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
                List<DoubleSolution> population = algorithm.getResult();

                computingTime += algorithmRunner.getComputingTime();

                printFinalSolutionSet(population);
                ReferenceParetoFrontGenerator.run();
                if (!referenceParetoFront.equals("")) {
                    printQualityIndicators(population, referenceParetoFront, (i + 1) * 10);
                }
            }
            JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the population into two files and prints some data on screen
     *
     * @param population
     */
    private static void printFinalSolutionSet(List<? extends Solution<?>> population) {

        new SolutionListOutput(population).setSeparator("\t")
                .setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
                .setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv")).print();

        JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
        JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
        JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
    }

    /**
     * Print all the available quality indicators
     *
     * @param population
     * @param paretoFrontFile
     * @throws Exception
     */
    private static void printQualityIndicators(List<DoubleSolution> population, String paretoFrontFile, int totalProgress) throws Exception {
        Front referenceFront = new ArrayFront(paretoFrontFile);
        FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);

        Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
        Front normalizedFront = frontNormalizer.normalize(new ArrayFront(population));
        List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);

        String outputString = "\n";
        outputString += "Progress        : " + totalProgress + "\n";
        outputString += "Hypervolume (N) : "
                + new WFGHypervolume<PointSolution>(normalizedReferenceFront).evaluate(normalizedPopulation) + "\n";
        outputString += "Hypervolume     : "
                + new WFGHypervolume<DoubleSolution>(referenceFront).evaluate(population) + "\n";
        outputString += "Error ratio     : "
                + new ErrorRatio<>(referenceFront).evaluate(population) + "\n";

        File f = new File("QualityIndicatorsProgress.txt");
        if (!f.exists()) {
            f.createNewFile();
        }
        Files.write(Paths.get("QualityIndicatorsProgress.txt"), outputString.getBytes(), StandardOpenOption.APPEND);

        JMetalLogger.logger.info(outputString);
    }

    private static List<DoubleSolution> loadInitialPopulation(PTDJMetalProblem problem) throws Exception {
        List<DoubleSolution> initialPopulation = new ArrayList<>();
        File f = new File("src" + File.separatorChar + "main" + File.separatorChar + "resources" + File.separatorChar + "progress.txt");

        if (f.exists()) {
            FileReader fr = new FileReader(f);
            BufferedReader br = new BufferedReader(fr);

            String firstLine = br.readLine();

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
                initialPopulation.add(solution);
            }
            br.close();
            fr.close();
            return initialPopulation;
        } else {
            return null;
        }
    }
}