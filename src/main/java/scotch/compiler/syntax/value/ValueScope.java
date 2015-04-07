package scotch.compiler.syntax.value;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.syntax.definition.Definitions.scopeDef;
import static scotch.compiler.syntax.reference.DefinitionReference.scopeRef;

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
import scotch.compiler.syntax.Scoped;
import scotch.compiler.syntax.definition.Definition;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ValueScope extends Value implements Scoped {

    private final SourceLocation sourceLocation;
    private final Symbol         symbol;
    private final Value          value;

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return state.keep(withValue(value.accumulateDependencies(state)));
    }

    @Override
    public Definition getDefinition() {
        return scopeDef(sourceLocation, symbol);
    }

    @Override
    public DefinitionReference getReference() {
        return scopeRef(symbol);
    }

    private ValueScope withValue(Value value) {
        return new ValueScope(sourceLocation, symbol, value);
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value bindMethods(TypeChecker state) {
        return state.scoped(this, () -> new ValueScope(sourceLocation, symbol, value.bindMethods(state)));
    }

    @Override
    public Value bindTypes(TypeChecker state) {
        return new ValueScope(sourceLocation, symbol, value.bindTypes(state));
    }

    @Override
    public Value checkTypes(TypeChecker state) {
        return state.scoped(this, () -> new ValueScope(sourceLocation, symbol, value.checkTypes(state)));
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator state) {
        state.beginMatches();
        try {
            return value.generateBytecode(state);
        } finally {
            state.endMatches();
        }
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        return value.generateIntermediateCode(state);
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public Type getType() {
        return value.getType();
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
        throw new UnsupportedOperationException(); // TODO
    }
}
