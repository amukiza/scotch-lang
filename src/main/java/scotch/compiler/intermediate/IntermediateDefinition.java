package scotch.compiler.intermediate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.symbol.type.Type;

@Getter
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateDefinition {

    private final DefinitionReference reference;
    private final Type type;
    private final IntermediateValue   value;
}
