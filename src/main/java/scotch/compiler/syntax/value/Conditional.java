package scotch.compiler.syntax.value;

import static scotch.compiler.intermediate.Intermediates.conditional;
import static scotch.compiler.syntax.TypeError.typeError;
import static scotch.compiler.syntax.builder.BuilderUtil.require;

import java.util.Optional;
import java.util.function.BiFunction;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
import scotch.data.bool.Bool;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class Conditional extends Value {

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    private final SourceLocation sourceLocation;
    @Getter
    private final Type           type;
    private final Value          condition;
    private final Value          whenTrue;
    private final Value          whenFalse;

    Conditional(SourceLocation sourceLocation, Value condition, Value whenTrue, Value whenFalse, Type type) {
        this.sourceLocation = sourceLocation;
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
        this.type = type;
    }

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return parse(state, Value::accumulateDependencies);
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        return parse(state, Value::accumulateNames);
    }

    @Override
    public Value bindMethods(TypeChecker state) {
        return parse(state, Value::bindMethods);
    }

    @Override
    public Value bindTypes(TypeChecker state) {
        return parse(state, Value::bindTypes);
    }

    @Override
    public Value checkTypes(TypeChecker state) {
        Value checkedCondition = condition.checkTypes(state);
        Value checkedWhenTrue = whenTrue.checkTypes(state);
        Value checkedWhenFalse = whenFalse.checkTypes(state);
        Type resultType = Bool.TYPE.unify(checkedCondition.getType(), state)
            .map(ct -> checkedWhenTrue.getType().unify(checkedWhenFalse.getType(), state))
            .orElseGet(unification -> {
                state.error(typeError(unification, checkedWhenFalse.getSourceLocation()));
                return type;
            });
        return new Conditional(sourceLocation, checkedCondition, checkedWhenTrue, checkedWhenFalse, resultType);
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        return parse(state, Value::defineOperators);
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        return conditional(
            condition.generateIntermediateCode(state),
            whenTrue.generateIntermediateCode(state),
            whenFalse.generateIntermediateCode(state)
        );
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        return parse(state, Value::parsePrecedence);
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        return parse(state, Value::qualifyNames)
            .withType(type.qualifyNames(state));
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        return new Conditional(
            sourceLocation,
            condition.reducePatterns(reducer),
            whenTrue.reducePatterns(reducer),
            whenFalse.reducePatterns(reducer),
            type
        );
    }

    @Override
    public Conditional withType(Type type) {
        return new Conditional(sourceLocation, condition, whenTrue, whenFalse, type);
    }

    private <T> Value parse(T state, BiFunction<Value, T, Value> function) {
        return builder()
            .withSourceLocation(sourceLocation)
            .withCondition(function.apply(condition, state))
            .withWhenTrue(function.apply(whenTrue, state))
            .withWhenFalse(function.apply(whenFalse, state))
            .withType(type)
            .build();
    }

    public static class Builder implements SyntaxBuilder<Conditional> {

        private Optional<Value>          condition;
        private Optional<Value>          whenTrue;
        private Optional<Value>          whenFalse;
        private Optional<Type>           type;
        private Optional<SourceLocation> sourceLocation;

        private Builder() {
            condition = Optional.empty();
            whenTrue = Optional.empty();
            whenFalse = Optional.empty();
            type = Optional.empty();
            sourceLocation = Optional.empty();
        }

        @Override
        public Conditional build() {
            return new Conditional(
                require(sourceLocation, "Source location"),
                require(condition, "Condition"),
                require(whenTrue, "True case"),
                require(whenFalse, "False case"),
                require(type, "Type")
            );
        }

        public Builder withCondition(Value condition) {
            this.condition = Optional.of(condition);
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

        public Builder withWhenFalse(Value whenFalse) {
            this.whenFalse = Optional.of(whenFalse);
            return this;
        }

        public Builder withWhenTrue(Value whenTrue) {
            this.whenTrue = Optional.of(whenTrue);
            return this;
        }
    }
}
