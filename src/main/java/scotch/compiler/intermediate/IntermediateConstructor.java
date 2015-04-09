package scotch.compiler.intermediate;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.target.BytecodeGenerator;
import scotch.symbol.MethodSignature;
import scotch.symbol.Symbol;

@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateConstructor extends IntermediateValue {

    private final Symbol                  symbol;
    private final String                  className;
    private final MethodSignature         methodSignature;
    private final List<IntermediateValue> arguments;

    public IntermediateConstructor(Symbol symbol, String className, MethodSignature methodSignature, List<IntermediateValue> arguments) {
        this.symbol = symbol;
        this.className = className;
        this.methodSignature = methodSignature;
        this.arguments = ImmutableList.copyOf(arguments);
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return new CodeBlock() {{
            newobj(className);
            dup();
            arguments.forEach(argument -> append(argument.generateBytecode(generator)));
            append(methodSignature.reference());
        }};
    }
}
