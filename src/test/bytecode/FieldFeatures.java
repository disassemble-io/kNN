import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassField;
import io.disassemble.asm.ClassMethod;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.KNN;
import io.disassemble.knn.feature.Feature;
import io.disassemble.knn.feature.IntegerFeature;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/15/16
 */
public class FieldFeatures {

    /**
     * TODO:
     *  - Force use on classes matched from ClassFeatures on non-static fields
     *  - Possibly generate CFG and sort the blocks, then use firstIndexOf(field)
     */
    public static FeatureSet spawn(Map<String, ClassFactory> classes, ClassField field, FeatureSet classSet) {
        if (SharedFeatures.callGraph == null) {
            SharedFeatures.callGraph = CallGraph.build(classes, true);
        }
        List<Feature> features = new ArrayList<>();
        if (isStatic(field)) {
            features.add(new IntegerFeature("owner-weight", 0, 0D));
        } else {
            features.add(new IntegerFeature("owner-weight", SharedFeatures.weightOfSet(classSet), 0.1D));
        }
        features.add(new IntegerFeature("access", field.access()));
        features.add(new IntegerFeature("desc", SharedFeatures.weightOfDesc(classes, field.desc())));
        features.add(new IntegerFeature("unique-calls", ucalls(field), 0.25D));
        features.add(new IntegerFeature("exact-calls", ecalls(field), 0.1D));
        features.add(new IntegerFeature("class-call-weights", weightedCalls(classes, field), 0.1D));
        return new FeatureSet(field.key(), features.toArray(new Feature[features.size()]));
    }

    public static FeatureSet[] spawnAll(Map<String, ClassFactory> classes, KNN classKNN) {
        List<FeatureSet> sets = new ArrayList<>();
        classes.values().forEach(factory -> {
            FeatureSet factorySet = classKNN.findSet(factory.name());
            for (ClassField field : factory.fields) {
                sets.add(spawn(classes, field, factorySet));
            }
        });
        return sets.toArray(new FeatureSet[sets.size()]);
    }

    private static boolean isStatic(ClassField field) {
        return (field.access() & Opcodes.ACC_STATIC) > 0;
    }

    private static int ucalls(ClassField field) {
        if (SharedFeatures.callGraph.fields().containsKey(field.key())) {
            return SharedFeatures.callGraph.fields().get(field.key()).size();
        }
        return 0;
    }

    private static int ecalls(ClassField field) {
        return SharedFeatures.callGraph.countOfField(field.key());
    }

    private static int weightedCalls(Map<String, ClassFactory> classes, ClassField field) {
        int weight = 0;
        if (SharedFeatures.callGraph.fields().containsKey(field.key())) {
            List<String> visited = new ArrayList<>();
            List<ClassMethod> methods = SharedFeatures.callGraph.fields().get(field.key());
            for (ClassMethod method : methods) {
                if (!visited.contains(method.owner.name())) {
                    weight += SharedFeatures.weightOfFactory(classes, method.owner.name());
                    visited.add(method.owner.name());
                }
            }
        }
        return weight;
    }
}
