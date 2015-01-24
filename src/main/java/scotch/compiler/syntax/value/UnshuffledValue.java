package scotch.compiler.syntax.value;

import static java.util.stream.Collectors.toList;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.data.either.Either.right;
import static scotch.util.StringUtil.stringify;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.symbol.Type;
import scotch.compiler.syntax.BytecodeGenerator;
import scotch.compiler.syntax.DependencyAccumulator;
import scotch.compiler.syntax.NameAccumulator;
import scotch.compiler.syntax.NameQualifier;
import scotch.compiler.syntax.OperatorDefinitionParser;
import scotch.compiler.syntax.PrecedenceParser;
import scotch.compiler.syntax.TypeChecker;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.text.SourceRange;
import scotch.data.either.Either;

public class UnshuffledValue extends Value {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceRange sourceRange;
    private final List<Value> values;

    UnshuffledValue(SourceRange sourceRange, List<Value> values) {
        this.sourceRange = sourceRange;
        this.values = ImmutableList.copyOf(values);
    }

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return withValues(values.stream()
            .map(value -> value.accumulateDependencies(state))
            .collect(toList()));
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        return withValues(values.stream()
            .map(value -> value.accumulateNames(state))
            .collect(toList()));
    }

    @Override
    public Value bindMethods(TypeChecker state) {
        return withValues(values.stream()
            .map(value -> value.bindMethods(state))
            .collect(toList()));
    }

    @Override
    public Value bindTypes(TypeChecker state) {
        return withValues(values.stream()
            .map(value -> value.bindTypes(state))
            .collect(toList()));
    }

    @Override
    public Value checkTypes(TypeChecker state) {
        return withValues(values.stream()
            .map(value -> value.checkTypes(state))
            .collect(toList()));
    }

    @Override
    public Value collapse() {
        if (values.size() == 1) {
            return values.get(0);
        } else {
            return this;
        }
    }

    @Override
    public Value defineOperators(OperatorDefinitionParser state) {
        return withValues(state.defineValueOperators(values));
    }

    @Override
    public Either<Value, List<Value>> destructure() {
        return right(getValues());
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof UnshuffledValue && Objects.equals(values, ((UnshuffledValue) o).values);
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator state) {
        throw new IllegalStateException("Can't generate bytecode for unshuffled value");
    }

    @Override
    public SourceRange getSourceRange() {
        return sourceRange;
    }

    @Override
    public Type getType() {
        throw new IllegalStateException();
    }

    public List<Value> getValues() {
        return values;
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        if (values.size() == 1) {
            return values.get(0).parsePrecedence(state);
        } else {
            return state.shuffle(this);
        }
    }

    @Override
    public Value qualifyNames(NameQualifier state) {
        return withValues(state.qualifyValueNames(values));
    }

    @Override
    public String toString() {
        return stringify(this) + "(" + values + ")";
    }

    @Override
    public Value unwrap() {
        return collapse().unwrap();
    }

    public UnshuffledValue withSourceRange(SourceRange sourceRange) {
        return new UnshuffledValue(sourceRange, values);
    }

    @Override
    public Value withType(Type type) {
        throw new UnsupportedOperationException();
    }

    public UnshuffledValue withValues(List<Value> members) {
        return new UnshuffledValue(sourceRange, members);
    }

    public static class Builder implements SyntaxBuilder<UnshuffledValue> {

        private final List<Value>           members;
        private       Optional<SourceRange> sourceRange;

        private Builder() {
            members = new ArrayList<>();
            sourceRange = Optional.empty();
        }

        @Override
        public UnshuffledValue build() {
            return unshuffled(
                require(sourceRange, "Source range"),
                members
            );
        }

        public Builder withMember(Value member) {
            members.add(member);
            return this;
        }

        @Override
        public Builder withSourceRange(SourceRange sourceRange) {
            this.sourceRange = Optional.of(sourceRange);
            return this;
        }
    }
}
