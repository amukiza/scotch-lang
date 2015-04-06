package scotch.compiler.syntax.pattern;

import static lombok.AccessLevel.PACKAGE;
import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.syntax.value.Values.id;
import static scotch.symbol.Symbol.symbol;

import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.steps.BytecodeGenerator;
import scotch.compiler.steps.DependencyAccumulator;
import scotch.compiler.steps.NameAccumulator;
import scotch.compiler.steps.ScopedNameQualifier;
import scotch.compiler.steps.TypeChecker;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.Value;
import scotch.compiler.text.SourceLocation;
import scotch.runtime.Callable;
import scotch.runtime.RuntimeSupport;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class EqualMatch extends PatternMatch {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation  sourceLocation;
    private final Optional<Value> argument;
    private final Value           value;
    private final Optional<Value> match;

    @Override
    public PatternMatch accumulateDependencies(DependencyAccumulator state) {
        return map(value -> value.accumulateDependencies(state));
    }

    @Override
    public PatternMatch accumulateNames(NameAccumulator state) {
        return this;
    }

    @Override
    public PatternMatch bind(Value argument, Scope scope) {
        if (this.argument.isPresent()) {
            throw new IllegalStateException();
        } else {
            return new EqualMatch(sourceLocation, Optional.of(argument), value, Optional.of(apply(
                apply(
                    id(sourceLocation, symbol("scotch.data.eq.(==)"), scope.reserveType()),
                    argument,
                    scope.reserveType()
                ),
                value,
                scope.reserveType()
            )));
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
    public CodeBlock generateBytecode(BytecodeGenerator state) {
        return new CodeBlock() {{
            append(match
                .orElseThrow(() -> new IllegalStateException("No match found"))
                .generateBytecode(state));
            invokestatic(p(RuntimeSupport.class), "unboxBool", sig(boolean.class, Callable.class));
            iffalse(state.nextCase());
        }};
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    public Value getValue() {
        return value;
    }

    @Override
    public PatternMatch qualifyNames(ScopedNameQualifier state) {
        return withValue(value.qualifyNames(state));
    }

    @Override
    public void reducePatterns(PatternReducer reducer) {
        throw new UnsupportedOperationException(); // TODO
    }

    public EqualMatch withSourceLocation(SourceLocation sourceLocation) {
        return new EqualMatch(sourceLocation, argument, value, match);
    }

    @Override
    public EqualMatch withType(Type type) {
        throw new UnsupportedOperationException(); // TODO
    }

    private PatternMatch map(Function<Value, Value> function) {
        return new EqualMatch(
            sourceLocation,
            Optional.of(function.apply(argument.orElseThrow(IllegalStateException::new))),
            function.apply(value),
            match.map(function)
        );
    }

    private EqualMatch withValue(Value value) {
        return new EqualMatch(sourceLocation, argument, value, match);
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
                require(value, "Capture value"),
                Optional.empty()
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
