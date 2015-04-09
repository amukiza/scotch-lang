package scotch.compiler.intermediate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.target.BytecodeGenerator;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateVariable extends IntermediateValue {

    private final String name;

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return new CodeBlock().aload(generator.offsetOf(name));
    }
}
