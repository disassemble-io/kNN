import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.feature.Feature;
import io.disassemble.knn.feature.IntegerFeature;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class ClassFeatures {

    public static FeatureSet spawn(Map<String, ClassFactory> classes, ClassFactory factory,
                                   boolean localWeight) {
        if (SharedFeatures.callGraph == null) {
            SharedFeatures.callGraph = CallGraph.build(classes, true);
        }
        List<Feature> features = new ArrayList<>();
        features.add(new IntegerFeature("access", factory.access()));
        features.add(new IntegerFeature("methods", factory.methodCount(), 0.25D));
        features.add(new IntegerFeature("fields", factory.fieldCount()));
        features.add(new IntegerFeature("unique-fields", factory.fieldTypeCount()));
        features.add(new IntegerFeature("parents", parentCount(classes, factory)));
        features.add(new IntegerFeature("ifaces", factory.interfaces().size()));
        features.add(new IntegerFeature("extended", extendCount(classes, factory)));
        if (!localWeight) {
            features.add(new IntegerFeature("weighted-calls", weightedCalls(classes, factory), 0.0005D));
            features.add(new IntegerFeature("field-weights", SharedFeatures.fieldWeights(classes, factory)));
            features.add(new IntegerFeature("method-complexity",
                    SharedFeatures.COMPLEXITY_VISITOR.complexityOf(factory),
                    0.05D));
            features.add(new IntegerFeature("call-complexity",
                    SharedFeatures.COMPLEXITY_VISITOR.callComplexityOf(SharedFeatures.callGraph, factory),
                    0.05D));
        }
        return new FeatureSet(factory.name(), features.toArray(new Feature[features.size()]));
    }

    public static FeatureSet spawn(Map<String, ClassFactory> classes, ClassFactory factory) {
        return spawn(classes, factory, false);
    }

    public static FeatureSet[] spawnAll(Map<String, ClassFactory> classes) {
        FeatureSet[] sets = new FeatureSet[classes.size()];
        ClassFactory[] factArray = classes.values().toArray(new ClassFactory[classes.size()]);
        for (int i = 0; i < factArray.length; i++) {
            sets[i] = spawn(classes, factArray[i]);
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

    private static int weightedCalls(Map<String, ClassFactory> classes, ClassFactory cf) {
        int weight = 0;
        for (ClassMethod method : cf.methods) {
            for (AbstractInsnNode ain : method.instructions().toArray()) {
                if (ain instanceof FieldInsnNode) {
                    weight += SharedFeatures.weightOfDesc(classes, ((FieldInsnNode) ain).desc);
                }
            }
        }
        return weight;
    }
}
