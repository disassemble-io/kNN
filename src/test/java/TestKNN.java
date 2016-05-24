import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.KNN;
import io.disassemble.knn.NeighborList;
import io.disassemble.knn.feature.DoubleFeature;
import org.junit.Test;

/**
 * @author Tyler Sedlar
 * @since 5/17/16
 */
public class TestKNN {

    private static final FeatureSet[] PEOPLE = {
            new FeatureSet("male", new DoubleFeature("height", 175D), new DoubleFeature("weight", 80D)),
            new FeatureSet("male", new DoubleFeature("height", 193.5D), new DoubleFeature("weight", 110D)),
            new FeatureSet("male", new DoubleFeature("height", 183D), new DoubleFeature("weight", 92.8D)),
            new FeatureSet("male", new DoubleFeature("height", 160D), new DoubleFeature("weight", 60D)),
            new FeatureSet("male", new DoubleFeature("height", 177D), new DoubleFeature("weight", 73.1D)),
            new FeatureSet("female", new DoubleFeature("height", 175D), new DoubleFeature("weight", 80D)),
            new FeatureSet("female", new DoubleFeature("height", 150D), new DoubleFeature("weight", 55D)),
            new FeatureSet("female", new DoubleFeature("height", 159D), new DoubleFeature("weight", 63.2D)),
            new FeatureSet("female", new DoubleFeature("height", 180D), new DoubleFeature("weight", 70D)),
            new FeatureSet("female", new DoubleFeature("height", 163D), new DoubleFeature("weight", 110D))
    };

    @Test
    public void test() {
        KNN knn = new KNN(PEOPLE);
        FeatureSet search = new FeatureSet(new DoubleFeature("height", 170D), new DoubleFeature("weight", 60D));
        long start = System.nanoTime();
        NeighborList results = knn.compute(3, search);
        long end = System.nanoTime();
        System.out.printf("Computed neighbors in %.4f seconds\n", (end - start) / 1e9);
        start = System.nanoTime();
        String classified = results.classify();
        end = System.nanoTime();
        System.out.printf("Classified as %s in %.4f seconds\n", classified, (end - start) / 1e9);
        System.out.println("Neighbors:");
        results.neighbors.forEach(neighbor -> System.out.printf("  %s\n", neighbor));
    }
}
