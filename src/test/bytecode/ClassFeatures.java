import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassField;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.visitor.ComplexityVisitor;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.feature.IntegerFeature;

import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/11/16
 */
public class ClassFeatures {

    private static ComplexityVisitor COMPLEXITY_VISITOR = new ComplexityVisitor();

    public static FeatureSet spawn(Map<String, ClassFactory> classes, ClassFactory factory) {
        return new FeatureSet(factory.name(),
                new IntegerFeature("access", factory.access()),
                new IntegerFeature("methods", factory.methodCount()),
                new IntegerFeature("fields", factory.fieldCount()),
                new IntegerFeature("unique-fields", factory.fieldTypeCount()),
                new IntegerFeature("parents", parentCount(classes, factory)),
                new IntegerFeature("ifaces", factory.interfaces().size()),
                new IntegerFeature("extended", extendCount(classes, factory)),
//                new IntegerFeature("complexity", complexity(factory)),
                new IntegerFeature("field-weights", fieldWeights(factory))
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

    private static int complexity(ClassFactory cf) {
        int complexity = 0;
        for (ClassMethod method : cf.methods) {
            method.accept(COMPLEXITY_VISITOR);
            complexity += COMPLEXITY_VISITOR.complexity();
        }
        return complexity;
    }

    private static int fieldWeights(ClassFactory cf) {
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
            }
            int count = desc.length() - desc.replaceAll("\\[", "").length();
            weight += (8 * count);
        }
        return weight;
    }
}
