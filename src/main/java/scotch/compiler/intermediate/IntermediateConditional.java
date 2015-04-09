package scotch.compiler.intermediate;

import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import org.objectweb.asm.tree.LabelNode;
import scotch.compiler.target.BytecodeGenerator;
import scotch.runtime.Callable;
import scotch.runtime.RuntimeSupport;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateConditional extends IntermediateValue {

    private final IntermediateValue condition;
    private final IntermediateValue whenTrue;
    private final IntermediateValue whenFalse;

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return new CodeBlock() {{
            LabelNode falseBranch = new LabelNode();
            LabelNode end = new LabelNode();
            append(condition.generateBytecode(generator));
            invokestatic(p(RuntimeSupport.class), "unboxBool", sig(boolean.class, Callable.class));
            iffalse(falseBranch);
            append(whenTrue.generateBytecode(generator));
            go_to(end);
            label(falseBranch);
            append(whenFalse.generateBytecode(generator));
            label(end);
        }};
    }
}
