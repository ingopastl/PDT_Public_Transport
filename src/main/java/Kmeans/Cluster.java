package Kmeans;


import beans.Node;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
    private List<Node> clusterMembers;
    private Node centroid;

    Cluster() {
        this.clusterMembers = new ArrayList<>();
    }

    public Node getCentroid() {
        return centroid;
    }

    public void setCentroid(Node centroid) {
        this.centroid = centroid;
    }

    public void addMember(Node newMember) {
        this.clusterMembers.add(newMember);
    }
}
