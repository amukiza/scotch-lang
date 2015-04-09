package scotch.compiler.syntax.pattern;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.syntax.value.Values.access;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.Value;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class TupleField {

    private final SourceLocation   sourceLocation;
    private final Optional<String> field;
    private final Type             type;
    private final PatternMatch     patternMatch;

    public TupleField accumulateNames(NameAccumulator state) {
        return withPatternMatch(patternMatch.accumulateNames(state));
    }

    public TupleField bind(Value argument, int ordinal, Scope scope) {
        String field = "_" + ordinal;
        return new TupleField(
            sourceLocation,
            Optional.of(field),
            type,
            patternMatch.bind(access(sourceLocation, argument, field, scope.reserveType()), scope)
        );
    }

    public TupleField bindMethods(TypeChecker state) {
        return new TupleField(sourceLocation, field, type, patternMatch.bindMethods(state));
    }

    public TupleField bindTypes(TypeChecker state) {
        return new TupleField(sourceLocation, field, state.generate(type), patternMatch.bindTypes(state));
    }

    public TupleField checkTypes(TypeChecker state) {
        PatternMatch checkedMatch = patternMatch.checkTypes(state);
        return new TupleField(sourceLocation, field, checkedMatch.getType(), checkedMatch);
    }

    public Type getType() {
        return type;
    }

    public void reducePatterns(PatternReducer reducer) {
        patternMatch.reducePatterns(reducer);
    }

    private TupleField withPatternMatch(PatternMatch patternMatch) {
        return new TupleField(sourceLocation, field, type, patternMatch);
    }
}
