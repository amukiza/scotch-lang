package scotch.compiler.intermediate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.target.BytecodeGenerator;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateAssign extends IntermediateValue {

    private final String variable;
    private final IntermediateValue value;
    private final IntermediateValue body;

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return new CodeBlock() {{
            append(value.generateBytecode(generator));
            generator.storeOffset(variable);
            astore(generator.offsetOf(variable));
            append(body.generateBytecode(generator));
        }};
    }
}
