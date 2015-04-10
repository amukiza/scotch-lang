package scotch.compiler.syntax.value;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.error.SymbolNotFoundError.symbolNotFound;
import static scotch.compiler.syntax.TypeError.typeError;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.intermediate.Intermediates;
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
    public Value bindMethods(TypeChecker typeChecker) {
        return new IsConstructor(sourceLocation, value.bindMethods(typeChecker), constructor);
    }

    @Override
    public Value bindTypes(TypeChecker typeChecker) {
        return new IsConstructor(sourceLocation, value.bindTypes(typeChecker), constructor);
    }

    @Override
    public Value checkTypes(TypeChecker typeChecker) {
        return typeChecker.getDataConstructorType(constructor)
            .map(parentType -> {
                Value checkedValue = value.checkTypes(typeChecker);
                parentType.unify(checkedValue.getType(), typeChecker)
                    .orElseGet(unification -> {
                        typeChecker.error(typeError(unification, checkedValue.getSourceLocation()));
                        return checkedValue.getType();
                    });
                return new IsConstructor(sourceLocation, checkedValue, constructor);
            })
            .orElseGet(() -> {
                typeChecker.error(symbolNotFound(constructor, sourceLocation));
                return this;
            });
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        return Intermediates.instanceOf(
            value.generateIntermediateCode(state),
            state.getDataConstructor(constructor).getClassName()
        );
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
