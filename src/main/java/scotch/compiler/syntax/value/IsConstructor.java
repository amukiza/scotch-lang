package scotch.compiler.syntax.value;

import static lombok.AccessLevel.PACKAGE;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.steps.BytecodeGenerator;
import scotch.compiler.steps.DependencyAccumulator;
import scotch.compiler.steps.NameAccumulator;
import scotch.compiler.steps.OperatorAccumulator;
import scotch.compiler.steps.PrecedenceParser;
import scotch.compiler.steps.ScopedNameQualifier;
import scotch.compiler.steps.TypeChecker;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.data.bool.Bool;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class IsConstructor extends Value {

    private final SourceLocation sourceLocation;
    private final Value          value;
    private final Symbol         constructor;

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return new IsConstructor(sourceLocation, value.accumulateDependencies(state), constructor);
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value bindMethods(TypeChecker state) {
        return new IsConstructor(sourceLocation, value.bindMethods(state), constructor);
    }

    @Override
    public Value bindTypes(TypeChecker state) {
        return new IsConstructor(sourceLocation, value.bindTypes(state), constructor);
    }

    @Override
    public Value checkTypes(TypeChecker state) {
        return new IsConstructor(sourceLocation, value.checkTypes(state), constructor);
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator state) {
        return new CodeBlock() {{
            append(value.generateBytecode(state));
            instance_of(state.getDataConstructorClass(constructor));
        }};
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public Type getType() {
        return Bool.TYPE;
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value withType(Type type) {
        return this; // no-op
    }
}
