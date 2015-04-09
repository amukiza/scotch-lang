package scotch.compiler.intermediate;

import static lombok.AccessLevel.PACKAGE;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.target.BytecodeGenerator;
import scotch.symbol.FieldSignature;
import scotch.symbol.Symbol;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateConstantReference extends IntermediateValue {

    private final Symbol symbol;
    private final Symbol dataType;
    private final FieldSignature constantField;

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return constantField.getValue();
    }
}
