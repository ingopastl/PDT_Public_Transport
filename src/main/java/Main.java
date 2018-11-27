import beans.Itinerary;

import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
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
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import repositories.BusLineRepository;
import repositories.BusStopRepository;
import repositories.ItineraryRepository;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        BusStopRepository busStopRepository = BusStopRepository.getInstance();
        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        ItineraryRepository itineraryRepository = ItineraryRepository.getInstance();
        try{
            busStopRepository.readCSV("src\\main\\resources\\busData\\stops.txt");
            busLineRepository.readCSV("src\\main\\resources\\busData\\routes.txt");
            itineraryRepository.readCSV("src\\main\\resources\\busData\\itineraries\\itineraries.txt");

            Problem<DoubleSolution> problem;
            Algorithm<List<DoubleSolution>> algorithm;
            CrossoverOperator<DoubleSolution> crossover;
            MutationOperator<DoubleSolution> mutation;
            SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
            String referenceParetoFront = "";

            Itinerary i = busLineRepository.getByID("423032").getItineraries().get(0);
            problem = new PTDJMetalProblem(i, 10, 800);

            double crossoverProbability = 1.0;
            crossover = new SBXCrossover(crossoverProbability, 1);

            double mutationProbability = 1.0 / problem.getNumberOfVariables();
            mutation = new PolynomialMutation(mutationProbability, 1);

            selection = new BinaryTournamentSelection<DoubleSolution>();

            algorithm = new NSGAIIIBuilder<>(problem).setCrossoverOperator(crossover).setMutationOperator(mutation)
                    .setSelectionOperator(selection).setPopulationSize(10).setMaxIterations(1).build();
            AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

            List<DoubleSolution> population = algorithm.getResult();
            long computingTime = algorithmRunner.getComputingTime();

            JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");


            printFinalSolutionSet(population);
            if (!referenceParetoFront.equals("")) {
                //printQualityIndicators(population, referenceParetoFront);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write the population into two files and prints some data on screen
     *
     * @param population
     */
    public static void printFinalSolutionSet(List<? extends Solution<?>> population) {

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
    public static void printQualityIndicators(List<? extends Solution<?>> population, String paretoFrontFile) throws Exception {
            Front referenceFront = new ArrayFront(paretoFrontFile);
            FrontNormalizer frontNormalizer = new FrontNormalizer(referenceFront);

            Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
            Front normalizedFront = frontNormalizer.normalize(new ArrayFront(population));
            List<DoubleSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);

            String outputString = "\n";
            outputString += "Hypervolume (N) : "
                    + new Hypervolume<List<? extends Solution<?>>>(normalizedReferenceFront).evaluate(normalizedPopulation)
                    + "\n";
            outputString += "Hypervolume     : "
                    + new Hypervolume<List<? extends Solution<?>>>(referenceFront).evaluate(population) + "\n";
            outputString += "Epsilon (N)     : "
                    + new Epsilon<List<? extends Solution<?>>>(normalizedReferenceFront).evaluate(normalizedPopulation)
                    + "\n";
            outputString += "Epsilon         : "
                    + new Epsilon<List<? extends Solution<?>>>(referenceFront).evaluate(population) + "\n";
            outputString += "GD (N)          : "
                    + new GenerationalDistance<List<? extends Solution<?>>>(normalizedReferenceFront)
                    .evaluate(normalizedPopulation)
                    + "\n";
            outputString += "GD              : "
                    + new GenerationalDistance<List<? extends Solution<?>>>(referenceFront).evaluate(population) + "\n";
            outputString += "IGD (N)         : "
                    + new InvertedGenerationalDistance<List<? extends Solution<?>>>(normalizedReferenceFront)
                    .evaluate(normalizedPopulation)
                    + "\n";
            outputString += "IGD             : "
                    + new InvertedGenerationalDistance<List<? extends Solution<?>>>(referenceFront).evaluate(population)
                    + "\n";
            outputString += "IGD+ (N)        : "
                    + new InvertedGenerationalDistancePlus<List<? extends Solution<?>>>(normalizedReferenceFront)
                    .evaluate(normalizedPopulation)
                    + "\n";
            outputString += "IGD+            : "
                    + new InvertedGenerationalDistancePlus<List<? extends Solution<?>>>(referenceFront).evaluate(population)
                    + "\n";
            outputString += "Spread (N)      : "
                    + new Spread<List<? extends Solution<?>>>(normalizedReferenceFront).evaluate(normalizedPopulation)
                    + "\n";
            outputString += "Spread          : "
                    + new Spread<List<? extends Solution<?>>>(referenceFront).evaluate(population) + "\n";
            outputString += "R2 (N)          : "
                    + new R2<List<DoubleSolution>>(normalizedReferenceFront).evaluate(normalizedPopulation) + "\n";
            outputString += "R2              : "
                    + new R2<List<? extends Solution<?>>>(referenceFront).evaluate(population) + "\n";
            outputString += "Error ratio     : "
                    + new ErrorRatio<List<? extends Solution<?>>>(referenceFront).evaluate(population) + "\n";

            JMetalLogger.logger.info(outputString);
    }
}