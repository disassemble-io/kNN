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

    private final Map<String, List<ClassMethod>> calls = new HashMap<>();

    public void addCall(String factory, ClassMethod method) {
        if (!calls.containsKey(factory)) {
            calls.put(factory, new ArrayList<>());
        }
        List<ClassMethod> methods = calls.get(factory);
        if (!methods.contains(method)) {
            methods.add(method);
        }
    }

    public Map<String, List<ClassMethod>> calls() {
        return calls;
    }

    public static CallGraph build(Map<String, ClassFactory> classes, boolean includeFields) {
        CallGraph graph = new CallGraph();
        ClassMethodVisitor callVisitor = new ClassMethodVisitor() {
            public void visitMethodInsn(MethodInsnNode min) {
                if (classes.containsKey(min.owner)) {
                    graph.addCall(min.owner, method);
                }
            }
            public void visitFieldInsn(FieldInsnNode fin) {
                if (includeFields && classes.containsKey(fin.owner)) {
                    graph.addCall(fin.owner, method);
                }
            }
        };
        classes.values().forEach(factory -> factory.dispatch(callVisitor));
        return graph;
    }
}
