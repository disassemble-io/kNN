import io.disassemble.asm.Archive;
import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.JarArchive;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.KNN;
import io.disassemble.knn.NeighborList;
import io.disassemble.knn.feature.IntegerFeature;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class ByteKNN {

    private static final String JAR = "115-deob.jar";
    private static final String JSON_MAPPING = "114CF.gson";

    private Map<String, ClassFactory> classes;

    @Before
    public void setup() throws Exception {
        Archive archive = new JarArchive(Resources.path(JAR).toFile());
        archive.build();
        classes = archive.classes();
    }

    @Test
    public void test() throws Exception {
        long start = System.nanoTime();
        FeatureSet[] sets = ClassFeatures.spawnAll(classes);
        long end = System.nanoTime();
        System.out.printf("created feature sets in %.4f seconds\n", (end - start) / 1e9);
        KNN knn = new KNN(sets);
        KNN jsonKNN = KNN.fromJSON(Resources.path(JSON_MAPPING), (entry) -> {
            int value = entry.source.get("value").getAsInt();
            double weight = entry.source.get("weight").getAsDouble();
            return new IntegerFeature(entry.key, value, weight);
        });
        for (FeatureSet lookup : jsonKNN.sets) {
            start = System.nanoTime();
            NeighborList neighbors = knn.compute(3, lookup);
            String category = neighbors.classify();
            end = System.nanoTime();
            System.out.printf("%s classified as -->%s<-- in %.4f seconds\n", lookup.category, category,
                    (end - start) / 1e9);
        }
    }
}
