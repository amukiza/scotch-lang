package scotch.compiler.intermediate;

import static java.util.Collections.emptyList;
import static scotch.compiler.syntax.reference.DefinitionReference.valueRef;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.target.BytecodeGenerator;
import scotch.symbol.Symbol;
import scotch.symbol.Value;
import scotch.symbol.type.Type;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateValueDefinition extends IntermediateDefinition {

    private final Symbol            symbol;
    private final Type              type;
    private final IntermediateValue value;

    @Override
    public void generateBytecode(BytecodeGenerator generator) {
        generator.createValue(symbol, new CodeBlock() {{
            annotate(Value.class).value("memberName", symbol.getSimpleName());
            generator.beginMethod(emptyList());
            //markLine(this); TODO
            append(value.generateBytecode(generator));
            areturn();
            generator.endMethod();
        }});
    }

    @Override
    public DefinitionReference getReference() {
        return valueRef(symbol);
    }
}
