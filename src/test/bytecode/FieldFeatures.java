import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassField;
import io.disassemble.asm.ClassMethod;
import io.disassemble.knn.FeatureSet;
import io.disassemble.knn.KNN;
import io.disassemble.knn.feature.Feature;
import io.disassemble.knn.feature.IntegerFeature;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * @author Tyler Sedlar
 * @since 6/15/16
 */
public class FieldFeatures {


    private static Map<ClassMethod, List<AbstractInsnNode>> linearized;
    private static Map<String, List<Integer>> getsums, putsums;

    /**
     * TODO:
     * - Possibly generate CFG and sort the blocks, then use firstIndexOf(field)
     */
    public static FeatureSet spawn(Map<String, ClassFactory> classes, ClassField field, FeatureSet classSet) {
        if (SharedFeatures.callGraph == null) {
            SharedFeatures.callGraph = CallGraph.build(classes, true);
        }
        if (linearized == null) {
            linearized = new HashMap<>();
            classes.values().forEach(factory -> {
                for (ClassMethod method : factory.methods) {
                    linearized.put(method, FlowIterator.linearize(method));
                }
            });
            getsums = new HashMap<>();
            putsums = new HashMap<>();
            collectAdvancedData(classes);
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
        features.add(new IntegerFeature("getsum", getsum(field), 0.25D));
        features.add(new IntegerFeature("putsum", putsum(field), 0.25D));
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

    private static int opsum(Map<String, List<Integer>> map, ClassField field) {
        if (map.containsKey(field.key())) {
            int total = 0;
            List<Integer> sums = map.get(field.key());
            for (int sum : sums) {
                total += sum;
            }
            return total / sums.size();
        }
        return 0;
    }

    private static int getsum(ClassField field) {
        return opsum(getsums, field);
    }

    private static int putsum(ClassField field) {
        return opsum(putsums, field);
    }

    private static int extraWeight(Map<String, ClassFactory> classes, AbstractInsnNode insn) {
        int weight = 0;
        if (insn instanceof FieldInsnNode) {
            FieldInsnNode fin = (FieldInsnNode) insn;
            weight += SharedFeatures.weightOfDesc(classes, fin.desc, false);
        }
        return weight;
    }

    private static void collectAdvancedData(Map<String, ClassFactory> classes) {
        linearized.forEach((method, list) -> {
            list.stream().filter(ain -> ain instanceof FieldInsnNode).forEach(ain -> {
                int index = list.indexOf(ain);
                int dist = 3;
                if (index - dist >= 0 && index + dist < list.size()) {
                    FieldInsnNode fin = (FieldInsnNode) ain;
                    String key = (fin.owner + "." + fin.name);
                    int sum = 0;
                    for (int i = 0; i < dist; i++) {
                        AbstractInsnNode p = list.get(index - i);
                        AbstractInsnNode n = list.get(index + i);
                        int extraP = extraWeight(classes, p);
                        int extraN = extraWeight(classes, n);
                        sum += (p.getOpcode() + extraP + n.getOpcode() + extraN);
                    }
                    boolean getter = (fin.getOpcode() == GETFIELD || fin.getOpcode() == GETSTATIC);
                    Map<String, List<Integer>> map = (getter ? getsums : putsums);
                    if (!map.containsKey(key)) {
                        map.put(key, new ArrayList<>());
                    }
                    map.get(key).add(sum);
                }
            });
        });
    }
}
