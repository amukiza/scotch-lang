package scotch.compiler.syntax.definition;

import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.reference.DefinitionReference.signatureRef;
import static scotch.compiler.util.Either.right;
import static scotch.util.StringUtil.stringify;

import java.util.Objects;
import java.util.Optional;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PatternAnalyzer;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.text.SourceLocation;
import scotch.compiler.util.Either;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

public class ValueSignature extends Definition {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation sourceLocation;
    private final Symbol         symbol;
    private final Type           type;

    ValueSignature(SourceLocation sourceLocation, Symbol symbol, Type type) {
        this.sourceLocation = sourceLocation;
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
    public Definition checkTypes(TypeChecker state) {
        return state.scoped(this, () -> {
            state.redefine(this);
            return this;
        });
    }

    @Override
    public Definition defineOperators(OperatorAccumulator state) {
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
    public Optional<DefinitionReference> generateIntermediateCode(IntermediateGenerator generator) {
        return Optional.empty();
    }

    @Override
    public DefinitionReference getReference() {
        return signatureRef(symbol);
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
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
    public Definition qualifyNames(ScopedNameQualifier state) {
        return state.scoped(this, () -> withType(type.qualifyNames(state)));
    }

    @Override
    public Definition reducePatterns(PatternAnalyzer state) {
        return state.keep(this);
    }

    @Override
    public String toString() {
        return stringify(this) + "(" + symbol + " :: " + type + ")";
    }

    public ValueSignature withType(Type type) {
        return new ValueSignature(sourceLocation, symbol, type);
    }

    public static class Builder implements SyntaxBuilder<ValueSignature> {

        private Optional<Symbol>         symbol;
        private Optional<Type>           type;
        private Optional<SourceLocation> sourceLocation;

        private Builder() {
            type = Optional.empty();
            symbol = Optional.empty();
            sourceLocation = Optional.empty();
        }

        @Override
        public ValueSignature build() {
            return Definitions.signature(
                require(sourceLocation, "Source location"),
                require(symbol, "Signature symbol"),
                require(type, "Signature type")
            );
        }

        @Override
        public Builder withSourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = Optional.of(sourceLocation);
            return this;
        }

        public Builder withSymbol(Symbol symbol) {
            this.symbol = Optional.of(symbol);
            return this;
        }

        public Builder withType(Type type) {
            this.type = Optional.of(type);
            return this;
        }
    }
}
