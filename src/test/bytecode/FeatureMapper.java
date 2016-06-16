import io.disassemble.asm.Archive;
import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.JarArchive;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.KNN;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class FeatureMapper {

    private static final String JAR = "115-deob.jar";

    private static final String CLASS_OUT_FILE = "115CF.gson";
    private static final String FIELD_OUT_FILE = "115FF.gson";

    private Map<String, ClassFactory> classes;
    private KNN classKNN;

    @Before
    public void setup() throws Exception {
        Archive archive = new JarArchive(Resources.path(JAR).toFile());
        archive.build();
        classes = archive.classes();
    }

    @Test
    public void mapClassesToJSON() throws URISyntaxException, IOException {
        long start = System.nanoTime();
        FeatureSet[] sets = ClassFeatures.spawnAll(classes);
        long end = System.nanoTime();
        System.out.printf("created class feature sets in %.4f seconds\n", (end - start) / 1e9);
        classKNN = new KNN(sets);
        classKNN.writeJSON(Resources.path(CLASS_OUT_FILE));
    }

    @Test
    public void mapFieldsToJSON() throws URISyntaxException, IOException {
        long start = System.nanoTime();
        FeatureSet[] sets = FieldFeatures.spawnAll(classes, classKNN);
        long end = System.nanoTime();
        System.out.printf("created field feature sets in %.4f seconds\n", (end - start) / 1e9);
        new KNN(sets).writeJSON(Resources.path(FIELD_OUT_FILE));
    }

    @Test
    public void map() throws Exception {
        mapClassesToJSON();
        mapFieldsToJSON();
    }
}
