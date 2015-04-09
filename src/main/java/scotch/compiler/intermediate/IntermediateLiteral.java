package scotch.compiler.intermediate;

import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.target.BytecodeGenerator;
import scotch.runtime.Callable;
import scotch.runtime.RuntimeSupport;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateLiteral extends IntermediateValue {

    private final Object value;

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return new CodeBlock() {{
            ldc(value);
            if (value instanceof Integer) {
                invokestatic(p(RuntimeSupport.class), "box", sig(Callable.class, int.class));
            } else if (value instanceof Character) {
                invokestatic(p(RuntimeSupport.class), "box", sig(Callable.class, char.class));
            } else if (value instanceof String) {
                invokestatic(p(RuntimeSupport.class), "box", sig(Callable.class, Object.class));
            } else if (value instanceof Boolean) {
                invokestatic(p(RuntimeSupport.class), "box", sig(Callable.class, boolean.class));
            } else if (value instanceof Double) {
                invokestatic(p(RuntimeSupport.class), "box", sig(Callable.class, double.class));
            } else {
                throw new UnsupportedOperationException(); // TODO
            }
        }};
    }
}
