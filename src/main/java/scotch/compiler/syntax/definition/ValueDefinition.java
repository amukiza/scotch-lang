package scotch.compiler.syntax.definition;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.syntax.TypeError.typeError;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.reference.DefinitionReference.valueRef;
import static scotch.compiler.util.Either.right;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
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
import scotch.compiler.syntax.reference.ValueReference;
import scotch.compiler.syntax.value.Value;
import scotch.compiler.text.SourceLocation;
import scotch.compiler.util.Either;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ValueDefinition extends Definition {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation sourceLocation;
    private final Symbol         symbol;
    private final Value          body;

    @Override
    public Definition accumulateDependencies(DependencyAccumulator state) {
        return state.scoped(this, () -> {
            state.pushSymbol(symbol);
            try {
                return withBody(body.accumulateDependencies(state));
            } finally {
                state.popSymbol();
            }
        });
    }

    @Override
    public Definition accumulateNames(NameAccumulator state) {
        state.defineValue(symbol, getType());
        state.specialize(getType());
        return state.scoped(this, () -> withBody(body.accumulateNames(state)));
    }

    @Override
    public Optional<Symbol> asSymbol() {
        return Optional.of(symbol);
    }

    @Override
    public Either<Definition, ValueDefinition> asValue() {
        return right(this);
    }

    @Override
    public Definition checkTypes(TypeChecker state) {
        return state.enclose(this, () -> {
            Value checkedBody = body.checkTypes(state);
            Type type = state.getType(this)
                .unify(checkedBody.getType(), state.scope())
                .orElseGet(unification -> {
                    state.error(typeError(unification, sourceLocation));
                    return checkedBody.getType();
                });
            Type generatedType = state.scope().generate(type);
            ValueDefinition result = withBody(checkedBody.withType(generatedType));
            state.redefine(result);
            return state.bind(result);
        });
    }

    @Override
    public Definition defineOperators(OperatorAccumulator state) {
        return state.scoped(this, () -> withBody(body.defineOperators(state)));
    }

    @Override
    public Optional<DefinitionReference> generateIntermediateCode(IntermediateGenerator generator) {
        return generator.scoped(this, () -> generator.defineValue(symbol, body.getType(), body.generateIntermediateCode(generator)));
    }

    public Value getBody() {
        return body;
    }

    public String getMethodName() {
        return symbol.unqualify().getMethodName();
    }

    @Override
    public ValueReference getReference() {
        return valueRef(symbol);
    }

    public String getSignature() {
        return getType().getSignature();
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Type getType() {
        return body.getType();
    }

    @Override
    public Optional<Definition> parsePrecedence(PrecedenceParser state) {
        return Optional.of(state.named(symbol, () -> state.scoped(this, () -> withBody(body.parsePrecedence(state).unwrap()))));
    }

    @Override
    public Definition qualifyNames(ScopedNameQualifier state) {
        return state.named(symbol, () -> state.scoped(this, () -> {
            Type qualifiedType = getType().qualifyNames(state);
            state.redefineValue(symbol, qualifiedType);
            return new ValueDefinition(
                sourceLocation,
                symbol,
                body.qualifyNames(state)
            );
        }));
    }

    @Override
    public Definition reducePatterns(PatternAnalyzer state) {
        return state.scoped(this, () -> withBody(body.reducePatterns(state)));
    }

    public ValueDefinition withBody(Value body) {
        return new ValueDefinition(sourceLocation, symbol, body);
    }

    public ValueDefinition withSourceLocation(SourceLocation sourceLocation) {
        return new ValueDefinition(sourceLocation, symbol, body);
    }

    public static class Builder implements SyntaxBuilder<ValueDefinition> {

        private Optional<Symbol>         symbol;
        private Optional<Value>          body;
        private Optional<SourceLocation> sourceLocation;

        private Builder() {
            symbol = Optional.empty();
            body = Optional.empty();
            sourceLocation = Optional.empty();
        }

        @Override
        public ValueDefinition build() {
            return Definitions.value(
                require(sourceLocation, "Source location"),
                require(symbol, "Value symbol"),
                require(body, "Value body").collapse()
            );
        }

        public Builder withBody(Value body) {
            this.body = Optional.of(body);
            return this;
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
    }
}
