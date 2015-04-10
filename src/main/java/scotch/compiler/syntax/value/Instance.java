package scotch.compiler.syntax.value;

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
import scotch.compiler.syntax.reference.InstanceReference;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class Instance extends Value {

    private final SourceLocation    sourceLocation;
    private final InstanceReference reference;
    private final Type              type;

    Instance(SourceLocation sourceLocation, InstanceReference reference, Type type) {
        this.sourceLocation = sourceLocation;
        this.reference = reference;
        this.type = type;
    }

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        return Intermediates.instanceRef(reference, state.instanceGetter(reference));
    }

    @Override
    public Value bindMethods(TypeChecker typeChecker) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value checkTypes(TypeChecker typeChecker) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value bindTypes(TypeChecker typeChecker) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Instance withType(Type type) {
        return new Instance(sourceLocation, reference, type);
    }
}
