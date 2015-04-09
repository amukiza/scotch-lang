package scotch.compiler.intermediate;

import static scotch.compiler.syntax.reference.DefinitionReference.moduleRef;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.target.BytecodeGenerator;

@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateModule extends IntermediateDefinition {

    private final String                    symbol;
    private final List<DefinitionReference> definitions;

    public IntermediateModule(String symbol, List<DefinitionReference> definitions) {
        this.symbol = symbol;
        this.definitions = ImmutableList.copyOf(definitions);
    }

    @Override
    public void generateBytecode(BytecodeGenerator generator) {
        generator.beginModule(symbol);
        definitions.forEach(generator::generateBytecode);
        generator.endClass();
    }

    @Override
    public DefinitionReference getReference() {
        return moduleRef(symbol);
    }
}
