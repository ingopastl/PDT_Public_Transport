import Kmeans.Clustering;
import Kmeans.Clusters;
import beans.Itinerary;

import jmetal.*;

import org.uma.jmetal.solution.impl.DefaultDoubleSolution;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.Point;
import org.uma.jmetal.util.point.PointSolution;
import org.uma.jmetal.util.point.impl.ArrayPoint;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import repositories.BusLineRepository;
import repositories.BusStopRepository;
import repositories.ItineraryRepository;

import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        /*
        * Cria instâncias dos repositórios para armazenar os dados da aplicação.
        */

        BusStopRepository busStopRepository = BusStopRepository.getInstance();
        BusLineRepository busLineRepository = BusLineRepository.getInstance();
        ItineraryRepository itineraryRepository = ItineraryRepository.getInstance();
        try{

            /*
            * Leitura dos dados na base de dados.
            * File.separatorChar equivale a um // ou \\, ele é usado para que a aplicação rode tanto em windows quanto
            * em linux sem a necessidade de alterações no código.
             */

            busStopRepository.readCSV("src" + File.separatorChar + "main" + File.separatorChar
                    + "resources" + File.separatorChar + "SPTrans_Data" + File.separatorChar + "stops.txt");
            busLineRepository.readCSV("src" + File.separatorChar + "main" + File.separatorChar
                    + "resources" + File.separatorChar + "SPTrans_Data" + File.separatorChar + "routes.txt");
            itineraryRepository.readCSV("src" + File.separatorChar + "main" + File.separatorChar
                    + "resources" + File.separatorChar + "SPTrans_Data" + File.separatorChar + "itineraries"
                    + File.separatorChar + "itineraries.txt");

            PTDJMetalProblem problem;
            Algorithm<List<DoubleSolution>> algorithm;
            CrossoverOperator<DoubleSolution> crossover;
            MutationOperator<DoubleSolution> mutation;
            SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

            /*
            * Busca um itinerário de uma linha de ônibus utilizando o repositório.
             */

            Itinerary iti = busLineRepository.getByID("423032").getItineraries().get(0);

            Clusters clusters = new Clustering().kMeans(10, 200);

            /*
            * walkingRadius == Raio em metros para a geração de pontos de origem e pontos de destino
            * para passageiros simulados.
            * localSearchRadius == Raio em metros para a realização de uma busca local na fase de mutação.
             */
            
            int walkingRadius = 800;
            int localSearchRadius = 2000;

            problem = new PTDJMetalProblem(iti, 30, walkingRadius, localSearchRadius, clusters);
            double crossoverProbability = 1.0;
            crossover = new PublicTransportNetworkCrossover(crossoverProbability);
            double mutationProbability = 1.0 / problem.getNumberOfVariables();
            mutation = new PublicTransportNetworkMutation(mutationProbability, localSearchRadius, clusters, problem.getStopClusterRelation());
            selection = new BinaryTournamentSelection<>();

            List<DoubleSolution> initialPopulation;

            long computingTime = 0;

            int i = 0, previousI = 8;
            while (true) {
                try {
                    for (i = previousI; i < 40; i++) {
                        initialPopulation = loadInitialPopulation(problem);

                        algorithm = new NSGAIIIBuilder<>(problem).setPopulationSize(92).setMaxIterations(10).setCrossoverOperator(crossover).setMutationOperator(mutation)
                                .setSelectionOperator(selection).setInitialPopulation(initialPopulation).build();
                        AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
                        List<DoubleSolution> population = algorithm.getResult();

                        computingTime += algorithmRunner.getComputingTime();

                        printFinalSolutionSet(population, (i + 1) * 10);
                        printQualityIndicators(population, (i + 1) * 10);
                    }
                    break;
                } catch (SocketTimeoutException | ConnectException e) {
                    previousI = i;
                    System.out.println(e.toString());
                }
            }

            JMetalLogger.logger.info("Total execution time: " + computingTime + "ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printFinalSolutionSet(List<? extends Solution<?>> population, int totalProgress) {
        new File("results" + File.separatorChar + "paretoFronts" + File.separatorChar + totalProgress).mkdirs();

        new SolutionListOutput(population).setSeparator("\t")
                .setVarFileOutputContext(new DefaultFileOutputContext("results" + File.separatorChar + "paretoFronts" + File.separatorChar + totalProgress + File.separatorChar + "VAR.tsv"))
                .setFunFileOutputContext(new DefaultFileOutputContext("results" + File.separatorChar + "paretoFronts" + File.separatorChar + totalProgress + File.separatorChar + "FUN.tsv")).print();

        JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
        JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
        JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
    }

    private static void printQualityIndicators(List<DoubleSolution> population, int totalProgress) throws IOException {
        Front frontRef = new ArrayFront(1, 3);
        Point point = new ArrayPoint(3);
        point.setValue(0, 0);
        point.setValue(1, 0);
        point.setValue(2, 0);

        Front front = new ArrayFront(population);
        FrontNormalizer frontNormalizer = new FrontNormalizer(front);
        Front normalizedFront = frontNormalizer.normalize(front);
        List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);

        Hypervolume<List<? extends Solution<?>>> hypervolume = new Hypervolume<>(frontRef);

        String qualityIndicator = "\n";
        String progress = "\n";
        qualityIndicator += totalProgress + "\n";
        progress += hypervolume.evaluate(normalizedPopulation);
        //qualityIndicator += "Error ratio     : " + new ErrorRatio<>(frontRef).evaluate(population) + "\n";

        File f = new File("results" + File.separatorChar + "QualityIndicators.txt");
        if (!f.exists()) {
            f.createNewFile();
        }

        File f2 = new File("results" + File.separatorChar + "Progress.txt");
        if (!f2.exists()) {
            f2.createNewFile();
        }

        Files.write(Paths.get("results" + File.separatorChar + "QualityIndicators.txt"), qualityIndicator.getBytes(), StandardOpenOption.APPEND);
        Files.write(Paths.get("results" + File.separatorChar + "Progress.txt"), progress.getBytes(), StandardOpenOption.APPEND);

        JMetalLogger.logger.info(qualityIndicator);
    }

    private static List<DoubleSolution> loadInitialPopulation(PTDJMetalProblem problem) throws IOException {
        List<DoubleSolution> initialPopulation = new ArrayList<>();
        File f = new File("results" + File.separatorChar + "lastPopulation.txt");

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