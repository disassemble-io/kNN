import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassField;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.visitor.ComplexityVisitor;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.feature.Feature;
import io.disassemble.knn.feature.IntegerFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class ClassFeatures {

    private static ComplexityVisitor COMPLEXITY_VISITOR = new ComplexityVisitor();
    private static CallGraph calls;

    public static FeatureSet spawn(Map<String, ClassFactory> classes, ClassFactory factory,
                                   boolean localWeight) {
        if (calls == null) {
            calls = CallGraph.build(classes, true);
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
            features.add(new IntegerFeature("field-weights", fieldWeights(classes, factory)));
            features.add(new IntegerFeature("method-complexity", complexity(factory), 0.05D));
            features.add(new IntegerFeature("call-complexity", avgCallComplexity(factory), 0.05D));
        }
        return new FeatureSet(factory.name(), features.toArray(new Feature[features.size()]));
    }

    public static FeatureSet spawn(Map<String, ClassFactory> classes, ClassFactory factory) {
        return spawn(classes, factory, false);
    }

    private static int weightOf(Map<String, ClassFactory> classes, String factory) {
        if (factory == null || !classes.containsKey(factory)) {
            return 0;
        } else {
            int weight = 0;
            ClassFactory cf = classes.get(factory);
            FeatureSet set = spawn(classes, cf, true);
            for (Feature feature : set.features) {
                if (feature instanceof IntegerFeature) {
                    weight += ((IntegerFeature) feature).value;
                }
            }
            return weight;
        }
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

    private static int fieldWeights(Map<String, ClassFactory> classes, ClassFactory cf) {
        int weight = 0;
        for (ClassField field : cf.fields) {
            String desc = field.desc();
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
                    weight += weightOf(classes, factory);
                }
            }
            int count = desc.length() - desc.replaceAll("\\[", "").length();
            weight += (8 * count);
        }
        return weight;
    }

    private static int complexity(ClassFactory factory) {
        int complexity = 0;
        for (ClassMethod method : factory.methods) {
            method.accept(COMPLEXITY_VISITOR);
            complexity += COMPLEXITY_VISITOR.complexity();
        }
        return complexity;
    }

    private static int avgCallComplexity(ClassFactory factory) {
        if (!calls.calls().containsKey(factory.name())) {
            return 0;
        } else {
            List<ClassMethod> methods = calls.calls().get(factory.name());
            AtomicInteger complexity = new AtomicInteger(0);
            methods.forEach(call -> {
                call.accept(COMPLEXITY_VISITOR);
                complexity.addAndGet(COMPLEXITY_VISITOR.complexity());
            });
            return (complexity.get() / methods.size());
        }
    }
}
