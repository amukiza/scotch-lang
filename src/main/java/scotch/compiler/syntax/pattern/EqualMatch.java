package scotch.compiler.syntax.pattern;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.util.Pair.pair;

import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.Identifier;
import scotch.compiler.syntax.value.Value;
import scotch.compiler.text.SourceLocation;
import scotch.compiler.util.Pair;
import scotch.symbol.Operator;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false, doNotUseGetters = true)
@ToString(exclude = "sourceLocation", doNotUseGetters = true)
public class EqualMatch extends PatternMatch {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation  sourceLocation;
    private final Optional<Value> argument;
    private final Value           value;

    @Override
    public PatternMatch accumulateDependencies(DependencyAccumulator state) {
        return map(value -> value.accumulateDependencies(state));
    }

    @Override
    public PatternMatch accumulateNames(NameAccumulator state) {
        return this;
    }

    @Override
    public Optional<Pair<EqualMatch, Operator>> asConstructorOperator(Scope scope) {
        return scope.qualify(getSymbol())
            .flatMap(scope::getOperator)
            .map(operator -> pair(this, operator));
    }

    @Override
    public PatternMatch bind(Value argument, Scope scope) {
        if (this.argument.isPresent()) {
            throw new IllegalStateException();
        } else {
            return new EqualMatch(sourceLocation, Optional.of(argument), value);
        }
    }

    @Override
    public PatternMatch bindMethods(TypeChecker state) {
        return map(value -> value.bindMethods(state));
    }

    @Override
    public PatternMatch bindTypes(TypeChecker state) {
        return map(value -> value.bindTypes(state));
    }

    @Override
    public PatternMatch checkTypes(TypeChecker state) {
        return map(value -> value.checkTypes(state));
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public Symbol getSymbol() {
        return getSymbol_().orElseThrow(IllegalStateException::new);
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    public Value getValue() {
        return value;
    }

    @Override
    public boolean isOperator(Scope scope) {
        return getSymbol_().map(scope::isOperator).orElse(false);
    }

    @Override
    public PatternMatch qualifyNames(ScopedNameQualifier state) {
        return withValue(value.qualifyNames(state));
    }

    @Override
    public void reducePatterns(PatternReducer reducer) {
        reducer.addCondition(reducer.getTaggedArgument(getArgument()), value);
    }

    public EqualMatch withSourceLocation(SourceLocation sourceLocation) {
        return new EqualMatch(sourceLocation, argument, value);
    }

    @Override
    public EqualMatch withType(Type type) {
        throw new UnsupportedOperationException(); // TODO
    }

    private Value getArgument() {
        return argument.orElseThrow(IllegalStateException::new);
    }

    private Optional<Symbol> getSymbol_() {
        if (value instanceof Identifier) {
            return Optional.of(((Identifier) value).getSymbol());
        } else {
            return Optional.empty();
        }
    }

    private PatternMatch map(Function<Value, Value> function) {
        return new EqualMatch(
            sourceLocation,
            Optional.of(function.apply(getArgument())),
            function.apply(value)
        );
    }

    private EqualMatch withValue(Value value) {
        return new EqualMatch(sourceLocation, argument, value);
    }

    public static class Builder implements SyntaxBuilder<EqualMatch> {

        private Optional<Value>          value;
        private Optional<SourceLocation> sourceLocation;

        private Builder() {
            // intentionally empty
        }

        @Override
        public EqualMatch build() {
            return Patterns.equal(
                require(sourceLocation, "Source location"),
                Optional.empty(),
                require(value, "Capture value")
            );
        }

        @Override
        public Builder withSourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = Optional.of(sourceLocation);
            return this;
        }

        public Builder withValue(Value value) {
            this.value = Optional.of(value);
            return this;
        }
    }
}
