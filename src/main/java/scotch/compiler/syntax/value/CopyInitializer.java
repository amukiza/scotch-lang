package scotch.compiler.syntax.value;

import static java.util.stream.Collectors.toList;

import java.util.List;
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
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString
public class CopyInitializer extends Value {

    private final SourceLocation         sourceLocation;
    private final Value                  value;
    private final List<InitializerField> fields;

    public CopyInitializer(SourceLocation sourceLocation, Value value, List<InitializerField> fields) {
        this.sourceLocation = sourceLocation;
        this.value = value;
        this.fields = fields;
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
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value bindTypes(TypeChecker state) {
        return new CopyInitializer(sourceLocation, value.bindTypes(state), fields.stream()
            .map(field -> field.bindTypes(state))
            .collect(toList()));
    }

    @Override
    public Value bindMethods(TypeChecker state) {
        return new CopyInitializer(sourceLocation, value.bindTypes(state), fields.stream()
            .map(field -> field.bindMethods(state))
            .collect(toList()));
    }

    @Override
    public Value checkTypes(TypeChecker state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
    public Value withType(Type type) {
        throw new UnsupportedOperationException();
    }
}
