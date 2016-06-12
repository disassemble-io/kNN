package io.disassemble.knn;

import io.disassemble.knn.feature.Feature;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Tyler Sedlar
 * @since 5/17/16
 */
public class FeatureSet {

    public final String category;
    public final Feature[] features;

    public FeatureSet(String category, Feature... features) {
        this.category = category;
        this.features = features;
        Arrays.sort(this.features);
    }

    public FeatureSet(Feature... features) {
        this(null, features);
    }

    public FeatureSet(Collection<Feature> features) {
        this(features.toArray(new Feature[features.size()]));
    }

    public boolean categorized() {
        return category != null;
    }

    @SuppressWarnings("unchecked")
    public double distanceTo(FeatureSet set) {
        if (features.length != set.features.length) {
            throw new IllegalArgumentException("Different features");
        }
        double distance = 0;
        for (int i = 0; i < features.length; i++) {
            double t = (features[i].distanceTo(set.features[i]) * set.features[i].weight);
            distance += (t * t);
        }
        return Math.sqrt(distance);
    }

    public Map<String, Feature> map(Predicate<Feature> filter) {
        Map<String, Feature> map = new HashMap<>();
        for (Feature feature : features) {
            if (feature.value != null && filter.test(feature)) {
                map.put(feature.key, feature);
            }
        }
        return map;
    }

    public String toString(Predicate<Feature> filter) {
        return String.format("<FeatureSet features=%s>", map(filter).toString());
    }

    @Override
    public String toString() {
        return toString(feature -> true);
    }
}
