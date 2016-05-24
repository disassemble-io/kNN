package io.disassemble.knn.feature;

/**
 * @author Tyler Sedlar
 * @since 5/17/16
 */
public class BooleanFeature extends IntegerFeature {

    public BooleanFeature(String key, boolean value) {
        super(key, value ? 1 : 0, 1F);
    }

    @Override
    public double distanceTo(Feature<Integer> feature) {
        return (value - feature.value);
    }
}
