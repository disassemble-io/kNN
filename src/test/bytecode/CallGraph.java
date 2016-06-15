import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.ClassMethodVisitor;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Tyler Sedlar
 * @since 6/12/16
 */
public class CallGraph {

    private final Map<String, List<ClassMethod>> classCalls = new HashMap<>();
    private final Map<String, Integer> classCallCounts = new HashMap<>();
    private final Map<String, List<ClassMethod>> fieldCalls = new HashMap<>();
    private final Map<String, Integer> fieldCallCounts = new HashMap<>();
    private final Map<String, List<ClassMethod>> methodCalls = new HashMap<>();
    private final Map<String, Integer> methodCallCounts = new HashMap<>();

    private void addCall(String key, Map<String, List<ClassMethod>> map, Map<String, Integer> counts,
                        ClassMethod method) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
            counts.put(key, 0);
        }
        counts.put(key, counts.get(key) + 1);
        List<ClassMethod> methods = map.get(key);
        if (!methods.contains(method)) {
            methods.add(method);
        }
    }

    public void addClassCall(String factoryKey, ClassMethod method) {
        addCall(factoryKey, classCalls, classCallCounts, method);
    }

    public void addFieldCall(String fieldKey, ClassMethod method) {
        addCall(fieldKey, fieldCalls, fieldCallCounts, method);
    }

    public void addMethodCall(String methodKey, ClassMethod method) {
        addCall(methodKey, methodCalls, methodCallCounts, method);
    }

    public Map<String, List<ClassMethod>> classes() {
        return classCalls;
    }

    public int countOfClass(String classKey) {
        return classCallCounts.containsKey(classKey) ? classCallCounts.get(classKey) : 0;
    }

    public Map<String, List<ClassMethod>> fields() {
        return fieldCalls;
    }

    public int countOfField(String fieldKey) {
        return fieldCallCounts.containsKey(fieldKey) ? fieldCallCounts.get(fieldKey) : 0;
    }

    public Map<String, List<ClassMethod>> methods() {
        return methodCalls;
    }

    public int countOfMethod(String methodKey) {
        return methodCallCounts.containsKey(methodKey) ? methodCallCounts.get(methodKey) : 0;
    }

    public static CallGraph build(Map<String, ClassFactory> classes, boolean includeFields) {
        CallGraph graph = new CallGraph();
        ClassMethodVisitor callVisitor = new ClassMethodVisitor() {
            public void visitMethodInsn(MethodInsnNode min) {
                if (classes.containsKey(min.owner)) {
                    graph.addClassCall(min.owner, method);
                    graph.addMethodCall(min.owner + "." + min.name + min.desc, method);
                }
            }
            public void visitFieldInsn(FieldInsnNode fin) {
                if (includeFields && classes.containsKey(fin.owner)) {
                    graph.addClassCall(fin.owner, method);
                }
                if (classes.containsKey(fin.owner)) {
                    graph.addFieldCall(fin.owner + "." + fin.name, method);
                }
            }
        };
        classes.values().forEach(factory -> factory.dispatch(callVisitor));
        return graph;
    }
}
