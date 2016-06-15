import io.disassemble.asm.ClassFactory;
import io.disassemble.asm.ClassMethod;
import io.disassemble.asm.visitor.ComplexityVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tyler Sedlar
 * @since 6/15/16
 */
public class TrackingComplexityVisitor extends ComplexityVisitor {

    private final Map<String, Integer> complexities = new HashMap<>();

    public int complexityOf(ClassFactory factory) {
        int complexity = 0;
        if (complexities.containsKey(factory.name())) {
            complexity = complexities.get(factory.name());
        } else {
            for (ClassMethod method : factory.methods) {
                method.accept(this);
                complexity += this.complexity();
            }
            complexities.put(factory.name(), complexity);
        }
        return complexity;
    }

    public int callComplexityOf(CallGraph graph, ClassFactory factory) {
        if (!graph.classes().containsKey(factory.name())) {
            return 0;
        } else {
            List<ClassMethod> methods = graph.classes().get(factory.name());
            AtomicInteger complexity = new AtomicInteger(0);
            if (complexities.containsKey(factory.name())) {
                complexity.set(complexities.get(factory.name()));
            } else {
                methods.forEach(call -> {
                    call.accept(this);
                    complexity.addAndGet(this.complexity());
                });
                complexities.put(factory.name(), complexity.get());
            }
            return complexity.get();
        }
    }
}
