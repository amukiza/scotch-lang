package scotch.compiler.intermediate;

public abstract class IntermediateValue {

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();
}
