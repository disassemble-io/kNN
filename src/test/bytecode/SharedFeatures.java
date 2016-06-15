import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassField;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.feature.Feature;
import io.disassemble.knn.feature.IntegerFeature;

import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/15/16
 */
public class SharedFeatures {

    public static TrackingComplexityVisitor COMPLEXITY_VISITOR = new TrackingComplexityVisitor();

    public static CallGraph callGraph;

    public static int weightOfSet(FeatureSet set) {
        int weight = 0;
        for (Feature feature : set.features) {
            if (feature instanceof IntegerFeature) {
                weight += ((IntegerFeature) feature).value;
            }
        }
        return weight;
    }

    public static int weightOfFactory(Map<String, ClassFactory> classes, String factory, boolean localWeight) {
        if (factory == null || !classes.containsKey(factory)) {
            return 0;
        } else {
            ClassFactory cf = classes.get(factory);
            FeatureSet set = ClassFeatures.spawn(classes, cf, localWeight);
            return weightOfSet(set);
        }
    }

    public static int weightOfFactory(Map<String, ClassFactory> classes, String factory) {
        return weightOfFactory(classes, factory, true);
    }

    public static int weightOfDesc(Map<String, ClassFactory> classes, String desc) {
        int weight = 0;
        if (desc.endsWith("B")) {
            weight += 100;
        } else if (desc.endsWith("Z")) {
            weight += 200;
        } else if (desc.endsWith("I")) {
            weight += 300;
        } else if (desc.endsWith("S")) {
            weight += 400;
        } else if (desc.endsWith("J")) {
            weight += 500;
        } else if (desc.endsWith("C")) {
            weight += 600;
        } else if (desc.equals("D")) {
            weight += 700;
        } else if (desc.equals("F")) {
            weight += 800;
        } else if (desc.endsWith("Ljava/lang/String;")) {
            weight += 900;
        } else if (desc.endsWith(";")) {
            weight += 1000;
            String factory = desc.split("L")[1].split(";")[0];
            if (classes.containsKey(factory)) {
                weight += weightOfFactory(classes, factory);
            }
        }
        int lastIdx = desc.lastIndexOf('[');
        if (lastIdx != -1) {
            lastIdx++;
        }
        int count = Math.max(0, lastIdx);
        weight += (8 * count);
        return weight;
    }

    public static int fieldWeights(Map<String, ClassFactory> classes, ClassFactory cf) {
        int weight = 0;
        for (ClassField field : cf.fields) {
            weight += weightOfDesc(classes, field.desc());
        }
        return weight;
    }
}
