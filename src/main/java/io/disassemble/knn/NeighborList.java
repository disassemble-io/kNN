package io.disassemble.knn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 5/17/16
 */
public class NeighborList {

    public final List<Neighbor> neighbors;
    public final FeatureSet classifier;

    public NeighborList(List<Neighbor> neighbors, FeatureSet classifier) {
        this.neighbors = neighbors;
        this.classifier = classifier;
    }

    public String classify() {
        HashMap<String, Double> distances = new HashMap<>();
        for (Neighbor neighbor : neighbors) {
            double distance = (1D / neighbor.set.distanceTo(classifier));
            if (!distances.containsKey(neighbor.set.category)) {
                distances.put(neighbor.set.category, distance);
            } else {
                distances.put(neighbor.set.category, distances.get(neighbor.set.category) + distance);
            }
        }
        String category = null;
        double max = 0;
        for (Map.Entry<String, Double> entry : distances.entrySet()) {
            double dist = entry.getValue();
            if (dist > max) {
                max = dist;
                category = entry.getKey();
            }
        }
        return category;
    }
}
