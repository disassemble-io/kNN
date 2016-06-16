import io.disassemble.knn.*;
import io.disassemble.knn.feature.Feature;
import io.disassemble.knn.feature.IntegerFeature;
import io.disassemble.knn.util.SourceEditor;
import org.junit.Test;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class ByteKNN {

    private static final String OLD_CLASS_MAPPING = "114CF.gson";
    private static final String NEW_CLASS_MAPPING = "115CF.gson";

    private static final String OLD_FIELD_MAPPING = "114FF.gson";
    private static final String NEW_FIELD_MAPPING = "115FF.gson";

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
                    if (neighbor != null && !categories.contains(neighbor.set.category)) {
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

    private static void mapFieldTypes(KNN knn, Map<String, List<FeatureSet>> normSets,
                                      List<FeatureSet> staticSets) {
        for (FeatureSet lookup : knn.sets) {
            int access = (int) lookup.findFeature("access").value;
            if ((access & Opcodes.ACC_STATIC) == 0) {
                String[] splits = lookup.category.split("\\.");
                String className = splits[0];
                if (!normSets.containsKey(className)) {
                    normSets.put(className, new ArrayList<>());
                }
                normSets.get(className).add(lookup);
            } else {
                staticSets.add(lookup);
            }
        }
    }

    private static Map<String, KNN> mapToKNN(Map<String, List<FeatureSet>> map) {
        Map<String, KNN> knns = new HashMap<>();
        map.forEach((key, sets) -> knns.put(key, new KNN(sets)));
        return knns;
    }

    private Map<String, String> mapFields(Map<String, String> classMapping, KNN oldKNN, KNN newKNN) {
        Map<String, String> mapping = new HashMap<>();

        Map<String, List<FeatureSet>> oldNormSets = new HashMap<>();
        List<FeatureSet> oldStaticSets = new ArrayList<>();
        mapFieldTypes(oldKNN, oldNormSets, oldStaticSets);

        Map<String, List<FeatureSet>> newNormSets = new HashMap<>();
        List<FeatureSet> newStaticSets = new ArrayList<>();
        mapFieldTypes(newKNN, newNormSets, newStaticSets);

        Map<String, KNN> oldNormNetwork = mapToKNN(oldNormSets);
        KNN oldStaticKNN = new KNN(oldStaticSets);

        Map<String, KNN> newNormNetwork = mapToKNN(newNormSets);
        KNN newStaticKNN = new KNN(newStaticSets);

        for (String oldKey : classMapping.keySet()) {
            if (oldNormNetwork.containsKey(oldKey)) {
                KNN oKNN = oldNormNetwork.get(oldKey);
                KNN nKNN = newNormNetwork.get(classMapping.get(oldKey));
                List<String> categories = new ArrayList<>();
                for (FeatureSet lookup : oKNN.sets) {
                    NeighborList neighbors = nKNN.compute(5, lookup);
                    String category = neighbors.classify();
                    if (categories.contains(category)) {
                        for (Neighbor neighbor : neighbors.neighbors) {
                            if (neighbor != null && !categories.contains(neighbor.set.category)) {
                                category = neighbor.set.category;
                                break;
                            }
                        }
                    }
                    if (oldKey.equals("hx")) {
                        System.out.println(lookup.category + " -> " + category);
                        System.out.println("  " + lookup);
                        for (Neighbor neighbor : neighbors.neighbors) {
                            System.out.println("  " + neighbor);
                        }
                    }
                    categories.add(category);
                    mapping.put(lookup.category, category);
                }
            }
        }
        return mapping;
    }

    @Test
    public void test() throws Exception {
        KNN oldClassKNN = KNN.fromJSON(Resources.path(OLD_CLASS_MAPPING), MAPPER);
        KNN newClassKNN = KNN.fromJSON(Resources.path(NEW_CLASS_MAPPING), MAPPER);
        long start = System.nanoTime();
        Map<String, String> classMapping = mapClasses(oldClassKNN, newClassKNN);
        long end = System.nanoTime();
        System.out.printf("classified classes in %.4f seconds\n", (end - start) / 1e9);
        KNN oldFieldKNN = KNN.fromJSON(Resources.path(OLD_FIELD_MAPPING), MAPPER);
        KNN newFieldKNN = KNN.fromJSON(Resources.path(NEW_FIELD_MAPPING), MAPPER);
        start = System.nanoTime();
        Map<String, String> fieldMapping = mapFields(classMapping, oldFieldKNN, newFieldKNN);
        end = System.nanoTime();
        System.out.printf("classified fields in %.4f seconds\n", (end - start) / 1e9);
    }
}
