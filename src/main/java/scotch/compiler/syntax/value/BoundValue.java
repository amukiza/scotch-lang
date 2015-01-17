package scotch.compiler.syntax.value;

import static scotch.util.StringUtil.stringify;

import java.util.Objects;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.symbol.Type;
import scotch.compiler.syntax.BytecodeGenerator;
import scotch.compiler.syntax.SyntaxTreeParser;
import scotch.compiler.syntax.TypeChecker;
import scotch.compiler.syntax.reference.ValueReference;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.text.SourceRange;

public class BoundValue extends Value {

    private final SourceRange    sourceRange;
    private final ValueReference reference;
    private final Type           type;

    BoundValue(SourceRange sourceRange, ValueReference reference, Type type) {
        this.sourceRange = sourceRange;
        this.reference = reference;
        this.type = type;
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Value accumulateDependencies(SyntaxTreeParser state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value accumulateNames(SyntaxTreeParser state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value bindMethods(TypeChecker state) {
        return this;
    }

    @Override
    public Value checkTypes(TypeChecker state) {
        return this;
    }

    @Override
    public Value bindTypes(TypeChecker state) {
        return withType(state.generate(type));
    }

    @Override
    public Value defineOperators(SyntaxTreeParser state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value parsePrecedence(SyntaxTreeParser state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof BoundValue) {
            BoundValue other = (BoundValue) o;
            return Objects.equals(sourceRange, other.sourceRange)
                && Objects.equals(reference, other.reference)
                && Objects.equals(type, other.type);
        } else {
            return false;
        }
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public SourceRange getSourceRange() {
        return sourceRange;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, type);
    }

    @Override
    public Value qualifyNames(SyntaxTreeParser state) {
        throw new UnsupportedOperationException();
    }

    public CodeBlock reference(Scope scope) {
        return scope.getValueSignature(reference.getSymbol()).get().reference();
    }

    @Override
    public String toString() {
        return stringify(this) + "(" + reference + " :: " + type + ")";
    }

    @Override
    public BoundValue withType(Type type) {
        return new BoundValue(sourceRange, reference, type);
    }
}
