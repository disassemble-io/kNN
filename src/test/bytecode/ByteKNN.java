import io.disassemble.knn.*;
import io.disassemble.knn.feature.Feature;
import io.disassemble.knn.feature.IntegerFeature;
import io.disassemble.knn.util.SourceEditor;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class ByteKNN {

    private static final String OLD_MAPPING = "114CF.gson";
    private static final String NEW_MAPPING = "115CF.gson";

    private static final SourceEditor<JsonEntry, Feature> MAPPER = (entry) -> {
        int value = entry.source.get("value").getAsInt();
        double weight = entry.source.get("weight").getAsDouble();
        return new IntegerFeature(entry.key, value, weight);
    };

    private Map<String, String> mapClasses(KNN oldKNN, KNN newKNN) {
        List<String> categories = new ArrayList<>();
        Map<String, String> mapping = new HashMap<>();
        for (FeatureSet lookup : oldKNN.sets) {
            NeighborList neighbors = newKNN.compute(3, lookup);
            String category = neighbors.classify();
            if (categories.contains(category)) {
                for (Neighbor neighbor : neighbors.neighbors) {
                    if (!categories.contains(neighbor.set.category)) {
                        category = neighbor.set.category;
                        break;
                    }
                }
            }
            categories.add(category);
            mapping.put(lookup.category, category);
        }
        return mapping;
    }

    @Test
    public void test() throws Exception {
        KNN oldKNN = KNN.fromJSON(Resources.path(OLD_MAPPING), MAPPER);
        KNN newKNN = KNN.fromJSON(Resources.path(NEW_MAPPING), MAPPER);
        long start = System.nanoTime();
        Map<String, String> mapping = mapClasses(oldKNN, newKNN);
        long end = System.nanoTime();
        System.out.printf("classified all in %.4f seconds\n", (end - start) / 1e9);
        mapping.entrySet().forEach(
                entry -> System.out.println(entry.getKey() + " -> " + entry.getValue())
        );
    }
}
