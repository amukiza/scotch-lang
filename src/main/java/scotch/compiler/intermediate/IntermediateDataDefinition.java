package scotch.compiler.intermediate;

import static scotch.compiler.syntax.reference.DefinitionReference.dataRef;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.target.BytecodeGenerator;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateDataDefinition extends IntermediateDefinition {

    private final Symbol                                  symbol;
    private final List<Type>                              parameters;
    private final List<IntermediateConstructorDefinition> constructors;

    public IntermediateDataDefinition(Symbol symbol, List<Type> parameters, List<IntermediateConstructorDefinition> constructors) {
        this.symbol = symbol;
        this.parameters = ImmutableList.copyOf(parameters);
        this.constructors = ImmutableList.copyOf(constructors);
    }

    @Override
    public void generateBytecode(BytecodeGenerator generator) {
        generator.beginData(symbol);
        constructors.forEach(constructor -> constructor.generateBytecode(generator));
        generator.endClass();
    }

    @Override
    public DefinitionReference getReference() {
        return dataRef(symbol);
    }
}
