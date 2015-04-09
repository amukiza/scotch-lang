package scotch.compiler.syntax.value;

import static lombok.AccessLevel.PACKAGE;

import lombok.AllArgsConstructor;
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
import scotch.compiler.intermediate.Intermediates;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class Accessor extends Value {

    @Getter
    private final SourceLocation sourceLocation;
    private final Value          target;
    private final String         field;
    @Getter
    private final Type           type;

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return new Accessor(sourceLocation, target.accumulateDependencies(state), field, type);
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value bindMethods(TypeChecker state) {
        return new Accessor(sourceLocation, target.bindMethods(state), field, type);
    }

    @Override
    public Value bindTypes(TypeChecker state) {
        return new Accessor(sourceLocation, target.bindTypes(state), field, state.generate(type));
    }

    @Override
    public Value checkTypes(TypeChecker state) {
        return new Accessor(sourceLocation, target.checkTypes(state), field, type);
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        IntermediateValue intermediateTarget = target.generateIntermediateCode(state);
        return Intermediates.access(state.capture(), intermediateTarget, field);
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
        return new Accessor(sourceLocation, target, field, type);
    }
}
