package scotch.compiler.syntax.definition;

import static scotch.compiler.syntax.reference.DefinitionReference.signatureRef;
import static scotch.data.either.Either.right;
import static scotch.util.StringUtil.stringify;

import java.util.Objects;
import java.util.Optional;
import scotch.compiler.symbol.Symbol;
import scotch.compiler.symbol.Type;
import scotch.compiler.syntax.BytecodeGenerator;
import scotch.compiler.syntax.DependencyAccumulator;
import scotch.compiler.syntax.NameAccumulator;
import scotch.compiler.syntax.NameQualifier;
import scotch.compiler.syntax.OperatorDefinitionParser;
import scotch.compiler.syntax.PrecedenceParser;
import scotch.compiler.syntax.TypeChecker;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.text.SourceRange;
import scotch.data.either.Either;

public class ValueSignature extends Definition {

    private final SourceRange sourceRange;
    private final Symbol      symbol;
    private final Type        type;

    ValueSignature(SourceRange sourceRange, Symbol symbol, Type type) {
        this.sourceRange = sourceRange;
        this.symbol = symbol;
        this.type = type;
    }

    @Override
    public Definition accumulateDependencies(DependencyAccumulator state) {
        return state.keep(this);
    }

    @Override
    public Definition accumulateNames(NameAccumulator state) {
        return state.scoped(this, () -> {
            state.defineSignature(symbol, type);
            state.specialize(type);
            return this;
        });
    }

    @Override
    public Either<Definition, ValueSignature> asSignature() {
        return right(this);
    }

    @Override
    public Definition bindTypes(TypeChecker state) {
        return withType(state.generate(type));
    }

    @Override
    public Definition checkTypes(TypeChecker state) {
        return state.scoped(this, () -> {
            state.redefine(this);
            return this;
        });
    }

    @Override
    public Definition defineOperators(OperatorDefinitionParser state) {
        return state.keep(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ValueSignature) {
            ValueSignature other = (ValueSignature) o;
            return Objects.equals(symbol, other.symbol)
                && Objects.equals(type, other.type);
        } else {
            return false;
        }
    }

    @Override
    public void generateBytecode(BytecodeGenerator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public DefinitionReference getReference() {
        return signatureRef(symbol);
    }

    @Override
    public SourceRange getSourceRange() {
        return sourceRange;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, type);
    }

    @Override
    public Optional<Definition> parsePrecedence(PrecedenceParser state) {
        return Optional.of(state.keep(this));
    }

    @Override
    public Definition qualifyNames(NameQualifier state) {
        return state.scoped(this, () -> withType(type.qualifyNames(state)));
    }

    @Override
    public String toString() {
        return stringify(this) + "(" + symbol + " :: " + type + ")";
    }

    public ValueSignature withType(Type type) {
        return new ValueSignature(sourceRange, symbol, type);
    }
}