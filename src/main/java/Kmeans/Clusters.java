package Kmeans;

import java.util.List;

public class Clusters {
    private double[][] points;
    private double[][] means;
    private List<Integer>[] clustersOfIndexes;

    public Clusters(double[][] points, double[][] means, List<Integer>[] clustersOfIndexes) {
        this.points = points;
        this.means = means;
        this.clustersOfIndexes = clustersOfIndexes;
    }

    public double[] getMean(int clusterIndex) {
        return means[clusterIndex];
    }

    public int getNumberOfClusters() {
        return clustersOfIndexes.length;
    }

    public int getClusterSize(int clusterIndex) {
        return clustersOfIndexes[clusterIndex].size();
    }

    //ClusterIndex = Order of the cluster in the array of clusters.
    public double[] getPointFromCluster(int clusterIndex, int positionInsideTheClusterArray) {
        return points[clustersOfIndexes[clusterIndex].get(positionInsideTheClusterArray)];
    }
}
