package scotch.compiler.intermediate;

import static lombok.AccessLevel.PACKAGE;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.target.BytecodeGenerator;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateField {

    private final String name;
    private final Type   type;

    public void generateBytecode(BytecodeGenerator generator) {
        generator.defineField(name);
    }

    public String getJavaName() {
        return Symbol.toJavaName(name);
    }

    public String getName() {
        return name;
    }
}
