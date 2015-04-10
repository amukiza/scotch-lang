package scotch.compiler.syntax.value;

import static scotch.compiler.intermediate.Intermediates.literal;
import static scotch.util.StringUtil.quote;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
public abstract class LiteralValue<A> extends Value {

    @Getter protected final SourceLocation sourceLocation;
    @Getter protected final A              value;
    @Getter protected final Type           type;

    LiteralValue(SourceLocation sourceLocation, A value, Type type) {
        this.sourceLocation = sourceLocation;
        this.value = value;
        this.type = type;
    }

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return this;
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        return this;
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        return literal(value);
    }

    @Override
    public Value bindMethods(TypeChecker typeChecker) {
        return this;
    }

    @Override
    public Value checkTypes(TypeChecker typeChecker) {
        return this;
    }

    @Override
    public Value bindTypes(TypeChecker typeChecker) {
        return this;
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        return this;
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        return this;
    }

    protected abstract CodeBlock loadValue();

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        return this;
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        return this;
    }

    @Override
    public String toString() {
        return "(" + quote(value) + " :: " + type + ")";
    }

    @Override
    public Value withType(Type type) {
        return this;
    }
}
