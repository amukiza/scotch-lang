package scotch.compiler.intermediate;

import static lombok.AccessLevel.PACKAGE;
import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.target.BytecodeGenerator;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateRaise extends IntermediateValue {

    private final String message;

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return new CodeBlock() {{
            newobj(p(RuntimeException.class)); // TODO should be specific exception type
            dup();
            ldc(message);
            invokespecial(p(RuntimeException.class), "<init>", sig(void.class, String.class));
            athrow();
        }};
    }
}
