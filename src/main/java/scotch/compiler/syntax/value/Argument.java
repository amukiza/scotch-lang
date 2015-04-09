package scotch.compiler.syntax.value;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.error.SymbolNotFoundError.symbolNotFound;
import static scotch.compiler.intermediate.Intermediates.variable;
import static scotch.compiler.syntax.TypeError.typeError;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.value.Values.arg;
import static scotch.symbol.Symbol.unqualified;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class Argument extends Value {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation sourceLocation;
    private final String         name;
    private final Type           type;

    @Override
    public Argument accumulateDependencies(DependencyAccumulator state) {
        return this;
    }

    @Override
    public Argument accumulateNames(NameAccumulator state) {
        state.defineValue(getSymbol(), type);
        return this;
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        state.reference(name);
        return variable(name);
    }

    @Override
    public Argument bindMethods(TypeChecker state) {
        return this;
    }

    @Override
    public Argument bindTypes(TypeChecker state) {
        return withType(state.generate(getType()));
    }

    @Override
    public Argument checkTypes(TypeChecker state) {
        state.capture(getSymbol());
        return state.scope().getValue(getSymbol())
            .map(actualType -> withType(actualType.unify(type, state.scope())
                .orElseGet(unification -> {
                    state.error(typeError(unification, sourceLocation));
                    return type;
                })))
            .orElseGet(() -> {
                state.error(symbolNotFound(getSymbol(), sourceLocation));
                return this;
            });
    }

    @Override
    public Argument defineOperators(OperatorAccumulator state) {
        return this;
    }

    public String getName() {
        return name;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public Symbol getSymbol() {
        return unqualified(name);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Argument parsePrecedence(PrecedenceParser state) {
        return this;
    }

    @Override
    public Argument qualifyNames(ScopedNameQualifier state) {
        return new Argument(sourceLocation, name, type.qualifyNames(state));
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Argument withType(Type type) {
        return arg(sourceLocation, name, type);
    }

    public static class Builder implements SyntaxBuilder<Argument> {

        private Optional<String>         name;
        private Optional<Type>           type;
        private Optional<SourceLocation> sourceLocation;

        private Builder() {
            name = Optional.empty();
            type = Optional.empty();
            sourceLocation = Optional.empty();
        }

        @Override
        public Argument build() {
            return arg(
                require(sourceLocation, "Source location"),
                require(name, "Argument name"),
                require(type, "Argument type")
            );
        }

        public Builder withName(String name) {
            this.name = Optional.of(name);
            return this;
        }

        @Override
        public Builder withSourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = Optional.of(sourceLocation);
            return this;
        }

        public Builder withType(Type type) {
            this.type = Optional.of(type);
            return this;
        }
    }
}
