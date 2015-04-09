package scotch.compiler.intermediate;

import static lombok.AccessLevel.PACKAGE;
import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.target.BytecodeGenerator;
import scotch.runtime.Callable;
import scotch.runtime.RuntimeSupport;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateInstanceOf extends IntermediateValue {

    private final IntermediateValue intermediateValue;
    private final String className;

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return new CodeBlock() {{
            append(intermediateValue.generateBytecode(generator));
            invokeinterface(p(Callable.class), "call", sig(Object.class));
            instance_of(className);
            invokestatic(p(RuntimeSupport.class), "box", sig(Callable.class, boolean.class));
        }};
    }
}
