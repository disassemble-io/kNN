import io.disassemble.asm.ClassMethod;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.objectweb.asm.Opcodes.GOTO;

public class FlowIterator implements Iterable<AbstractInsnNode> {

    private final InsnList insns;

    public FlowIterator(InsnList insns) {
        this.insns = insns;
    }

    public static List<AbstractInsnNode> linearize(ClassMethod method) {
        List<AbstractInsnNode> linear = new ArrayList<>();
        InsnList insns = new InsnList();
        method.instructions().iterator().forEachRemaining(insns::add);
        new FlowIterator(insns).forEach(linear::add);
        return linear;
    }

    @Override
    public Iterator<AbstractInsnNode> iterator() {
        return new Iterator<AbstractInsnNode>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < insns.size();
            }

            @Override
            public AbstractInsnNode next() {
                AbstractInsnNode insn = insns.get(index++);
                return insn.getOpcode() == GOTO ? insns.get(
                        insns.indexOf(((JumpInsnNode) insn).label)
                ).getNext() : insn;
            }

            @Override
            public void remove() {
                insns.remove(insns.get(index));
            }
        };
    }
}