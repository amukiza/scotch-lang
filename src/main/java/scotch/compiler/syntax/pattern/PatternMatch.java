package scotch.compiler.syntax.pattern;

import static scotch.compiler.util.Either.left;

import java.util.List;
import java.util.Optional;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.Value;
import scotch.compiler.text.SourceLocation;
import scotch.compiler.util.Either;
import scotch.compiler.util.Pair;
import scotch.symbol.Operator;
import scotch.symbol.type.Type;

public abstract class PatternMatch {

    PatternMatch() {
        // intentionally empty
    }

    public abstract PatternMatch accumulateDependencies(DependencyAccumulator state);

    public abstract PatternMatch accumulateNames(NameAccumulator state);

    public Either<PatternMatch, CaptureMatch> asCapture() {
        return left(this);
    }

    public Optional<Pair<CaptureMatch, Operator>> asCaptureOperator(Scope scope) {
        return Optional.empty();
    }

    public Optional<Pair<EqualMatch, Operator>> asConstructorOperator(Scope scope) {
        return Optional.empty();
    }

    public abstract PatternMatch bind(Value argument, Scope scope);

    public abstract PatternMatch bindMethods(TypeChecker state);

    public abstract PatternMatch bindTypes(TypeChecker state);

    public abstract PatternMatch checkTypes(TypeChecker state);

    public Either<PatternMatch, List<PatternMatch>> destructure() {
        return left(this);
    }

    @Override
    public abstract boolean equals(Object o);

    public abstract SourceLocation getSourceLocation();

    public abstract Type getType();

    @Override
    public abstract int hashCode();

    public boolean isOperator(Scope scope) {
        return false;
    }

    public String prettyPrint() {
        return "[" + getClass().getSimpleName() + "]";
    }

    public abstract PatternMatch qualifyNames(ScopedNameQualifier state);

    public abstract void reducePatterns(PatternReducer reducer);

    public PatternMatch shuffle(PrecedenceParser state) {
        return this;
    }

    @Override
    public abstract String toString();

    public abstract PatternMatch withType(Type type);
}
