package scotch.compiler.intermediate;

import static scotch.compiler.syntax.reference.DefinitionReference.rootRef;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.target.BytecodeGenerator;

@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateRoot extends IntermediateDefinition {

    private final List<DefinitionReference> references;

    public IntermediateRoot(List<DefinitionReference> references) {
        this.references = ImmutableList.copyOf(references);
    }

    @Override
    public void generateBytecode(BytecodeGenerator generator) {
        references.forEach(generator::generateBytecode);
    }

    @Override
    public DefinitionReference getReference() {
        return rootRef();
    }
}
