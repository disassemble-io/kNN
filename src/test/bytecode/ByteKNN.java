import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.disassemble.asm.Archive;
import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.JarArchive;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.KNN;
import io.disassemble.knn.NeighborList;
import io.disassemble.knn.feature.IntegerFeature;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class ByteKNN {

    private static final String JAR = "115-deob.jar";
    private static final String JSON_MAPPING = "114CF.gson";

    private static final String TEST_CASE = null;//"VarpNode";
    @SuppressWarnings("all")
    private static final boolean TESTING = (TEST_CASE != null && !TEST_CASE.isEmpty());

    private Map<String, ClassFactory> classes;
    private JsonObject oldIdentities, assumedIdentities;

    @Before
    public void setup() throws Exception {
        Archive archive = new JarArchive(Resources.path(JAR).toFile());
        archive.build();
        classes = archive.classes();
        oldIdentities = new JsonParser()
                .parse(new String(Files.readAllBytes(Resources.path("114-MAP.gson"))))
                .getAsJsonObject();
        assumedIdentities = new JsonParser()
                .parse(new String(Files.readAllBytes(Resources.path("115-MAP.gson"))))
                .getAsJsonObject();
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
        long total = 0;
        for (FeatureSet lookup : jsonKNN.sets) {
            start = System.nanoTime();
            NeighborList neighbors = knn.compute(3, lookup);
            String category = neighbors.classify();
            end = System.nanoTime();
            total += (end - start);
            if (oldIdentities.has(lookup.category)) {
                String a = oldIdentities.get(lookup.category).getAsString();
                if (assumedIdentities.has(category)) {
                    String b = assumedIdentities.get(category).getAsString();
                    if (!a.equals(b)) {
                        System.out.printf("%s (%s) -> %s (%s)\n", lookup.category, a, category, b);
                    }
                } else {
                    System.out.printf("%s (%s) -> %s (???)\n", lookup.category, a, category);
                }
            }
        }
        System.out.printf("classified all in %.4f seconds\n", total / 1e9);
    }
}
