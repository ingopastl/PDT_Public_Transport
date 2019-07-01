package Kmeans;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Clustering {
    public Clusters kMeans(int k, int maxIterations) throws IOException {

        //Check if there isn't clusters already made with the configuration.
        File f = new File("clusteredNodes" + k + "K" + maxIterations + "I" + ".txt");
        if (f.exists()) {
            FileReader clusterFilereader = new FileReader(f);
            BufferedReader clusterBufferedReader = new BufferedReader(clusterFilereader);
            CSVReader clusterCsvReader = new CSVReaderBuilder(clusterBufferedReader).withSkipLines(1).build();
            List<String[]> clusterData = clusterCsvReader.readAll();

            double[][] points = new double[clusterData.size()][3];
            double[][] means = new double[k][3];
            List<Integer>[] clusters = new ArrayList[k];

            for(int i = 0; i < clusters.length; i++) {
                clusters[i] = new ArrayList<>();
            }

            for (int i = 0; i < clusterData.size(); i++) {
                int clusterIndex = Integer.parseInt(clusterData.get(i)[3]);

                points[i][0] = Double.parseDouble(clusterData.get(i)[0]);
                points[i][1] = Double.parseDouble(clusterData.get(i)[1]);
                points[i][2] = Double.parseDouble(clusterData.get(i)[2]);

                clusters[clusterIndex].add(i);
                //System.out.println(i);
            }
            updateMeans(clusters, means, points);

            return new Clusters(points, means, clusters);
        }

        FileReader filereader = new FileReader("nodes.csv");
        CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
        List<String[]> csvData = csvReader.readAll();
        double[][] points = new double[csvData.size()][3];

        for (int i = 0; i < csvData.size(); i++) {
            points[i][0] = Double.parseDouble(csvData.get(i)[0]);
            points[i][1] = Double.parseDouble(csvData.get(i)[1]);
            points[i][2] = Double.parseDouble(csvData.get(i)[2]);

            //System.out.println(i);
        }

        // Calculate initial means
        double[][] means = new double[k][3];
        Random rn = new Random();
        for(int i = 0; i < means.length; i++) {
            int index = rn.nextInt(csvData.size());

            means[i][0] = points[index][0];
            means[i][1] = points[index][1];
            means[i][2] = points[index][2];
        }

        // Create skeletons for clusters
        List<Integer>[] oldClusters = new ArrayList[k];
        List<Integer>[] newClusters = new ArrayList[k];

        for(int i = 0; i < k; i++) {
            oldClusters[i] = new ArrayList<>();
            newClusters[i] = new ArrayList<>();
        }

        // Make the initial clusters
        formClusters(oldClusters, means, points);
        int iterations = 0;

        while(true) {
            updateMeans(oldClusters, means, points);
            formClusters(newClusters, means, points);

            iterations++;

            if(iterations > maxIterations || checkEquality(oldClusters, newClusters))
                break;
            else
                resetClusters(oldClusters, newClusters);
        }

        System.out.println("\nIterations taken = " + iterations);
        createCSV(oldClusters, points, k, maxIterations);
        //plotClusters(oldClusters, points);

        return new Clusters(points, means, oldClusters);
    }

    private void createCSV(List<Integer>[] oldClusters, double[][] points, int k, int maxIterations) throws IOException {
        String csv = "clusteredNodes" + k + "K" + maxIterations + "I" + ".txt";
        CSVWriter csvWriter = new CSVWriter(new FileWriter(csv));
        String [] record = "lat,lon,region,cluster".split(",");
        csvWriter.writeNext(record);

        for (int i = 0; i < oldClusters.length; i++) {
            record = new String[4];
            for (int j = 0; j < oldClusters[i].size(); j++) {
                record[0] = "" + points[oldClusters[i].get(j)][0];
                record[1] = "" + points[oldClusters[i].get(j)][1];
                record[2] = "" + points[oldClusters[i].get(j)][2];
                record[3] = "" + i;
                csvWriter.writeNext(record);
            }
        }
        csvWriter.close();
    }

    private void updateMeans(List<Integer>[] clusterList, double[][] means, double[][] points) {
        double totalX;
        double totalY;
        double totalZ;
        for(int i = 0; i < clusterList.length; i++) {
            totalX = 0;
            totalY = 0;
            totalZ = 0;
            for(int index: clusterList[i]) {
                totalX += points[index][0];
                totalY += points[index][1];
                totalZ += points[index][2];
            }
            means[i][0] = totalX/clusterList[i].size();
            means[i][1] = totalY/clusterList[i].size();
            means[i][2] = totalZ/clusterList[i].size();
        }
    }

    private void resetClusters(List<Integer>[] oldClusters, List<Integer>[] newClusters) {
        for(int i=0; i<newClusters.length; i++) {
            // Copy newClusters to oldClusters
            oldClusters[i].clear();
            for(int index: newClusters[i])
                oldClusters[i].add(index);

            // Clear newClusters
            newClusters[i].clear();
        }
    }

    private void formClusters(List<Integer>[] clusterList, double[][] means, double[][] points) {
        double distance[] = new double[means.length];
        double minDistance;
        int minIndex = 0;

        for(int i = 0; i < points.length; i++) {
            minDistance = Double.MAX_VALUE;
            for(int j = 0; j < means.length; j++) {
                distance[j] = Math.sqrt( Math.pow((points[i][0] - means[j][0]), 2) + Math.pow((points[i][1] - means[j][1]), 2) + Math.pow((points[i][2] - means[j][2]), 2));
                if(distance[j] < minDistance) {
                    minDistance = distance[j];
                    minIndex = j;
                }
            }
            System.out.println("Go to cluster: " + minIndex);
            System.out.println("Distance: " + minDistance);
            clusterList[minIndex].add(i);
        }
    }

    private boolean checkEquality(List<Integer>[] oldClusters, List<Integer>[] newClusters) {
        for(int i=0; i<oldClusters.length; i++) {
            // Check only lengths first
            if(oldClusters[i].size() != newClusters[i].size())
                return false;

            // Check individual values if lengths are equal
            for(int j=0; j<oldClusters[i].size(); j++)
                if(oldClusters[i].get(j).equals(newClusters[i].get(j)))
                    return false;
        }

        return true;
    }

    private void plotClusters(List<Integer>[] oldClusters, double[][] points) throws Exception {
        File f = new File("json.txt");
        FileWriter fw = new FileWriter(f);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n");

        for (int i = 0; i < oldClusters.length; i++) {
            for (int j = 0; j < oldClusters[i].size(); j++) {
                String color;
                if (i == 0) {
                    color = "#FFFF00";
                } else if (i == 1) {
                    color = "#808000";
                } else if (i == 2) {
                    color = "#00FFFF";
                } else if (i == 3) {
                    color = "#008000";
                } else if (i == 4) {
                    color = "#0000FF";
                } else if (i == 5) {
                    color = "#000080";
                } else if (i == 6) {
                    color = "#800080";
                } else if (i == 7) {
                    color = "#C0C0C0";
                } else if (i == 8) {
                    color = "#FA8072";
                } else {
                    color = "#800000";
                }

                bw.write("{\n" +
                        "      \"type\": \"Feature\",\n" +
                        "      \"geometry\": {\n" +
                        "        \"type\": \"Point\",\n" +
                        "        \"coordinates\": [\n" +
                        "          " + points[oldClusters[i].get(j)][1] + ",\n" +
                        "          " + points[oldClusters[i].get(j)][0] + "\n" +
                        "        ]\n" +
                        "      },\n" +
                        "      \"properties\": {\n" +
                        "        \"marker-color\": \"" + color + "\",\n" +
                        "        \"marker-size\": \"small\",\n" +
                        "        \"marker-symbol\": \"\",\n" +
                        "        \"prop0\": \"value0\"\n" +
                        "      }\n" +
                        "    },");
                j += 100;
            }
        }

        bw.write("  ]\n" +
                "}");


        bw.close();
        fw.close();
    }
}