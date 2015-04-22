package scotch.compiler.ast;

import scotch.compiler.text.SourceLocation;

public abstract class AstNode {

    public abstract <T> T accept(AstNodeVisitor<T> visitor);

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    public abstract SourceLocation getSourceLocation();

    @Override
    public abstract String toString();
}
