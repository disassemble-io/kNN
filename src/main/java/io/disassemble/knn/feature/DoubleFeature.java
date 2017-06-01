package io.disassemble.knn.feature;

/**
 * @author Tyler Sedlar
 * @since 5/17/16
 */
public class DoubleFeature extends Feature<Double> {

    public DoubleFeature(String key, Double value, double weight) {
        super(key, value, weight);
    }

    public DoubleFeature(String key, Double value) {
        super(key, value);
    }

    @Override
    public double distanceTo(Feature<Double> feature) {
        return (Math.abs(value - feature.value) * weight);
    }
}
