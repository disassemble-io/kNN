package io.disassemble.knn.feature;

/**
 * @author Tyler Sedlar
 * @since 5/17/16
 */
public class IntegerFeature extends Feature<Integer> {

    public IntegerFeature(String key, Integer value, double weight) {
        super(key, value, weight);
    }

    public IntegerFeature(String key, Integer value) {
        super(key, value);
    }

    @Override
    public double distanceTo(Feature<Integer> feature) {
        return ((value - feature.value) * weight);
    }
}
