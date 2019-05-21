package Kmeans;

import beans.Node;

import java.util.List;

public class Kmeans {
    private final int K;
    private List<Node> population;
    private Cluster[] clusters;

    public Kmeans(int nClusters, int k, List<Node> population) {
        this.K = k;
        this.clusters = new Cluster[this.K];
        this.population = population;
    }

    public int getK() {
        return this.K;
    }

    public double calculateDistance() {
        return 0;
    }

}
