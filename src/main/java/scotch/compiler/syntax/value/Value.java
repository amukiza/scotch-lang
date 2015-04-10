package scotch.compiler.syntax.value;

import static java.util.stream.Collectors.toList;
import static scotch.compiler.syntax.value.WithArguments.withoutArguments;
import static scotch.compiler.util.Either.left;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.text.SourceLocation;
import scotch.compiler.util.Either;
import scotch.compiler.util.Pair;
import scotch.symbol.Operator;
import scotch.symbol.Symbol;
import scotch.symbol.type.SumType;
import scotch.symbol.type.Type;

public abstract class Value {

    Value() {
        // intentionally empty
    }

    public abstract Value accumulateDependencies(DependencyAccumulator state);

    public abstract Value accumulateNames(NameAccumulator state);

    public Optional<Value> asInitializer(Initializer initializer, TypeChecker state) {
        Value checkedValue = checkTypes(state);
        if (checkedValue.getType() instanceof SumType) {
            return Optional.of(new CopyInitializer(
                initializer.getSourceLocation(),
                checkedValue,
                initializer.getFields().stream()
                    .map(field -> field.checkTypes(state))
                    .collect(toList())));
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    public Optional<Pair<Identifier, Operator>> asOperator(Scope scope) {
        return Optional.empty();
    }

    public abstract Value bindMethods(TypeChecker typeChecker);

    public abstract Value bindTypes(TypeChecker typeChecker);

    public abstract Value checkTypes(TypeChecker typeChecker);

    public Value collapse() {
        return this;
    }

    public abstract Value defineOperators(OperatorAccumulator state);

    public Either<Value, List<Value>> destructure() {
        return left(this);
    }

    @Override
    public abstract boolean equals(Object o);

    public boolean equalsBeta(Value o) {
        return false;
    }

    public abstract IntermediateValue generateIntermediateCode(IntermediateGenerator state);

    public abstract SourceLocation getSourceLocation();

    public Optional<Symbol> getTag() {
        return Optional.empty();
    }

    public abstract Type getType();

    @Override
    public abstract int hashCode();

    public boolean isOperator(Scope scope) {
        return false;
    }

    public Value mapTags(Function<Value, Value> mapper) {
        throw new UnsupportedOperationException();
    }

    public abstract Value parsePrecedence(PrecedenceParser state);

    public String prettyPrint() {
        return "[" + getClass().getSimpleName() + "]";
    }

    public abstract Value qualifyNames(ScopedNameQualifier state);

    public abstract Value reducePatterns(PatternReducer reducer);

    public Value reTag(Value value) {
        return value.getTag().map(this::withTag).orElse(this);
    }

    @Override
    public abstract String toString();

    public Value unwrap() {
        return this;
    }

    public WithArguments withArguments() {
        return withoutArguments(this);
    }

    public Value withTag(Symbol tag) {
        throw new UnsupportedOperationException(getClass().getName()); // TODO
    }

    public abstract Value withType(Type type);
}
