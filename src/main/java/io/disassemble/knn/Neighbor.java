package io.disassemble.knn;

import io.disassemble.knn.feature.Feature;

import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Tyler Sedlar
 * @since 5/23/16
 */
public class Neighbor {

    public final FeatureSet set;
    public final double distance;

    public Neighbor(FeatureSet set, double distance) {
        this.set = set;
        this.distance = distance;
    }

    public String toString(Predicate<Feature> filter) {
        Map<String, Feature> map = set.map(filter);
        return String.format("<Neighbor category=%s, distance=%s, features=%s>", set.category,
                distance, map.toString());
    }

    @Override
    public String toString() {
        return toString(feature -> true);
    }
}
