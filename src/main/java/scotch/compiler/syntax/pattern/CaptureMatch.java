package scotch.compiler.syntax.pattern;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.syntax.TypeError.typeError;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.value.Values.let;
import static scotch.compiler.util.Either.right;
import static scotch.compiler.util.Pair.pair;

import java.util.Optional;
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
import scotch.compiler.util.Either;
import scotch.compiler.util.Pair;
import scotch.symbol.Operator;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;
import scotch.symbol.util.SymbolGenerator;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false, doNotUseGetters = true)
@ToString(exclude = "sourceLocation", doNotUseGetters = true)
public class CaptureMatch extends PatternMatch {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation   sourceLocation;
    private final Optional<Value> argument;
    private final Symbol           symbol;
    private final Type             type;

    @Override
    public PatternMatch accumulateDependencies(DependencyAccumulator state) {
        return this;
    }

    @Override
    public PatternMatch accumulateNames(NameAccumulator state) {
        state.defineValue(symbol, type);
        state.specialize(type);
        return this;
    }

    @Override
    public Either<PatternMatch, CaptureMatch> asCapture() {
        return right(this);
    }

    @Override
    public Optional<Pair<CaptureMatch, Operator>> asCaptureOperator(Scope scope) {
        return scope.qualify(getSymbol())
            .flatMap(scope::getOperator)
            .map(operator -> pair(this, operator));
    }

    @Override
    public PatternMatch bind(Value argument, Scope scope) {
        return new CaptureMatch(sourceLocation, Optional.of(argument), symbol, type);
    }

    @Override
    public PatternMatch bindMethods(TypeChecker state) {
        return new CaptureMatch(sourceLocation, Optional.of(getArgument().bindMethods(state)), symbol, type);
    }

    @Override
    public PatternMatch bindTypes(TypeChecker state) {
        return new CaptureMatch(sourceLocation, Optional.of(getArgument().bindTypes(state)), symbol, state.generate(type));
    }

    @Override
    public PatternMatch checkTypes(TypeChecker state) {
        Scope scope = state.scope();
        state.addLocal(symbol);
        Value checkedArgument = getArgument().checkTypes(state);
        return new CaptureMatch(
            sourceLocation,
            Optional.of(checkedArgument),
            symbol,
            type.unify(checkedArgument.getType(), scope)
                .orElseGet(unification -> {
                    state.error(typeError(unification, sourceLocation));
                    return type;
                })
        );
    }

    public Value getArgument() {
        return argument.orElseThrow(IllegalStateException::new);
    }

    public String getName() {
        return symbol.getCanonicalName();
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isOperator(Scope scope) {
        return scope.isOperator(symbol);
    }

    @Override
    public PatternMatch qualifyNames(ScopedNameQualifier state) {
        return this;
    }

    public Value reducePattern(PatternReducer reducer, SymbolGenerator generator, Value result) {
        Value argument = reducer.getTaggedArgument(getArgument());
        return let(sourceLocation, generator.reserveType(), symbol.getCanonicalName(), argument, result);
    }

    @Override
    public void reducePatterns(PatternReducer reducer) {
        reducer.addAssignment(this);
    }

    public CaptureMatch withSourceLocation(SourceLocation sourceLocation) {
        return new CaptureMatch(sourceLocation, argument, symbol, type);
    }

    public CaptureMatch withSymbol(Symbol symbol) {
        return new CaptureMatch(sourceLocation, argument, symbol, type);
    }

    @Override
    public PatternMatch withType(Type type) {
        return new CaptureMatch(sourceLocation, argument, symbol, type);
    }

    public static class Builder implements SyntaxBuilder<CaptureMatch> {

        private Optional<SourceLocation> sourceLocation = Optional.empty();
        private Optional<Symbol>         symbol      = Optional.empty();
        private Optional<Type>           type        = Optional.empty();

        private Builder() {
            // intentionally empty
        }

        @Override
        public CaptureMatch build() {
            return Patterns.capture(
                require(sourceLocation, "Source location"),
                Optional.empty(),
                require(symbol, "Capture symbol"),
                require(type, "Capture type")
            );
        }

        public Builder withIdentifier(Identifier identifier) {
            return withSymbol(identifier.getSymbol())
                .withType(identifier.getType());
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
