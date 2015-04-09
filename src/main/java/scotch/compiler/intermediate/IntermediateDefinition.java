package scotch.compiler.intermediate;

import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.target.BytecodeGenerator;

public abstract class IntermediateDefinition {

    @Override
    public abstract boolean equals(Object o);

    public abstract void generateBytecode(BytecodeGenerator generator);

    public abstract DefinitionReference getReference();

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
