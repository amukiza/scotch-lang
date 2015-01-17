package scotch.compiler.syntax.value;

import static java.util.stream.Collectors.toList;
import static scotch.util.StringUtil.stringify;

import java.util.List;
import java.util.Objects;
import com.google.common.collect.ImmutableList;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.symbol.Type;
import scotch.compiler.syntax.BytecodeGenerator;
import scotch.compiler.syntax.SyntaxTreeParser;
import scotch.compiler.syntax.TypeChecker;
import scotch.compiler.text.SourceRange;

public class UnshuffledValue extends Value {

    private final SourceRange sourceRange;
    private final List<Value> values;

    UnshuffledValue(SourceRange sourceRange, List<Value> values) {
        this.sourceRange = sourceRange;
        this.values = ImmutableList.copyOf(values);
    }

    @Override
    public <T> T accept(ValueVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Value accumulateDependencies(SyntaxTreeParser state) {
        return withValues(values.stream()
            .map(value -> value.accumulateDependencies(state))
            .collect(toList()));
    }

    @Override
    public Value accumulateNames(SyntaxTreeParser state) {
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

    public Value collapse() {
        if (values.size() == 1) {
            return values.get(0);
        } else {
            return this;
        }
    }

    @Override
    public Value defineOperators(SyntaxTreeParser state) {
        return withValues(values.stream()
            .map(value -> value.defineOperators(state))
            .collect(toList()));
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof UnshuffledValue && Objects.equals(values, ((UnshuffledValue) o).values);
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator state) {
        throw new UnsupportedOperationException();
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
    public Value parsePrecedence(SyntaxTreeParser state) {
        if (values.size() == 1) {
            return values.get(0).parsePrecedence(state);
        } else {
            return state.shuffle(this);
        }
    }

    @Override
    public Value qualifyNames(SyntaxTreeParser state) {
        return withValues(values.stream().map(value -> value.qualifyNames(state)).collect(toList()));
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
}
