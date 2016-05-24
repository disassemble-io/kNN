package io.disassemble.knn;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Tyler Sedlar
 * @since 5/17/16
 */
public class KNN {

    public final FeatureSet[] sets;

    public KNN(FeatureSet[] set) {
        this.sets = set;
    }

    public KNN(Collection<FeatureSet> collection) {
        this.sets = collection.toArray(new FeatureSet[collection.size()]);
    }

    public NeighborList compute(int k, FeatureSet likely) {
        List<Neighbor> results = Arrays.asList(new Neighbor[k]);
        for (FeatureSet set : sets) {
            double setDistance = set.distanceTo(likely);
            if (results.isEmpty()) {
                results.add(new Neighbor(set, setDistance));
            } else {
                for (int i = 0; i < results.size(); i++) {
                    Neighbor neighbor = results.get(i);
                    if (neighbor == null || setDistance < neighbor.set.distanceTo(likely)) {
                        results.set(i, new Neighbor(set, setDistance));
                        break;
                    }
                }
            }
        }
        return new NeighborList(results, likely);
    }
}
