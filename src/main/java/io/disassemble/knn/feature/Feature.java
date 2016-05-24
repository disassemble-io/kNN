package io.disassemble.knn.feature;

/**
 * @author Tyler Sedlar
 * @since 5/17/16
 */
public abstract class Feature<T> {

    public final String key;
    public final T value;
    public double weight;

    public Feature(String key, T value, double weight) {
        this.key = key;
        this.value = value;
        this.weight = weight;
    }

    public Feature(String key, T value) {
        this(key, value, 1D);
    }

    public abstract double distanceTo(Feature<T> feature);

    @Override
    public String toString() {
        return (value + ":" + weight);
    }
}
