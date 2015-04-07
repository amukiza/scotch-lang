package scotch.compiler.syntax.value;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.intermediate.Intermediates.assign;
import static scotch.compiler.syntax.TypeError.typeError;
import static scotch.symbol.Symbol.symbol;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class Let extends Value {

    @Getter
    private final SourceLocation sourceLocation;
    private final String         name;
    private final Value          value;
    private final Value          scope;
    @Getter
    private final Type           type;

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return new Let(sourceLocation, name, value.accumulateDependencies(state), scope.accumulateDependencies(state), type);
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value bindMethods(TypeChecker state) {
        return new Let(sourceLocation, name, value.bindMethods(state), scope.bindMethods(state), type);
    }

    @Override
    public Value bindTypes(TypeChecker state) {
        return new Let(sourceLocation, name, value.bindTypes(state), scope.bindTypes(state), state.generate(type));
    }

    @Override
    public Value checkTypes(TypeChecker state) {
        state.addLocal(symbol(name));
        Value checkedValue = value.checkTypes(state);
        state.scope().redefineValue(symbol(name), checkedValue.getType());
        Value checkedScope = scope.checkTypes(state);
        return new Let(sourceLocation, name, checkedValue, checkedScope,
            checkedScope.getType().unify(type, state).orElseGet(unification -> {
                state.error(typeError(unification, scope.getSourceLocation()));
                return type;
            }));
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator state) {
        return new CodeBlock() {{
            state.addMatch(name);
            append(value.generateBytecode(state));
            astore(state.getVariable(name));
            append(scope.generateBytecode(state));
        }};
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        IntermediateValue checkedScope = scope.generateIntermediateCode(state);
        IntermediateValue checkedValue = value.generateIntermediateCode(state);
        IntermediateValue result = assign(name, checkedValue, checkedScope);
        state.addArgument(name);
        return result;
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
