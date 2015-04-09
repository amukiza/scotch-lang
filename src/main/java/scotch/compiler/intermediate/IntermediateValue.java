package scotch.compiler.intermediate;

import me.qmx.jitescript.CodeBlock;
import scotch.compiler.target.BytecodeGenerator;

public abstract class IntermediateValue {

    @Override
    public abstract boolean equals(Object o);

    public abstract CodeBlock generateBytecode(BytecodeGenerator generator);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
