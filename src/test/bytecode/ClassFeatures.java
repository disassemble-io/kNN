import io.disassemble.asm.ClassFactory;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.feature.IntegerFeature;

import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class ClassFeatures {

    public static FeatureSet spawn(Map<String, ClassFactory> classes, ClassFactory factory) {
        return new FeatureSet(factory.name(),
                new IntegerFeature("access", factory.access()),
                new IntegerFeature("methods", factory.methodCount()),
                new IntegerFeature("fields", factory.fieldCount()),
                new IntegerFeature("unique-fields", factory.fieldTypeCount()),
                new IntegerFeature("parents", parentCount(classes, factory)),
                new IntegerFeature("ifaces", factory.interfaces().size()),
                new IntegerFeature("extended", extendCount(classes, factory))
        );
    }

    public static FeatureSet[] spawnAll(Map<String, ClassFactory> classes) {
        FeatureSet[] sets = new FeatureSet[classes.size()];
        ClassFactory[] factArray = classes.values().toArray(new ClassFactory[classes.size()]);
        for (int i = 0; i < factArray.length; i++) {
            sets[i] = ClassFeatures.spawn(classes, factArray[i]);
        }
        return sets;
    }

    private static int parentCount(Map<String, ClassFactory> classes, ClassFactory cf) {
        int count = 0;
        while (cf != null && !cf.ownerless()) {
            count++;
            cf = classes.get(cf.superName());
        }
        return count;
    }

    private static int extendCount(Map<String, ClassFactory> classes, ClassFactory cf) {
        int count = 0;
        for (ClassFactory factory : classes.values()) {
            if (factory.superName().equals(cf.name())) {
                count++;
            }
        }
        return count;
    }
}
