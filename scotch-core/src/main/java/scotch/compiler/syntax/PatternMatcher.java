package scotch.compiler.syntax;

import static scotch.compiler.syntax.DefinitionReference.patternRef;
import static scotch.compiler.syntax.SourceRange.NULL_SOURCE;
import static scotch.compiler.syntax.Type.fn;
import static scotch.compiler.util.TextUtil.stringify;

import java.util.List;
import java.util.Objects;
import com.google.common.collect.ImmutableList;

public class PatternMatcher {

    public static PatternMatcher pattern(Symbol symbol, List<PatternMatch> matches, Value body) {
        return new PatternMatcher(NULL_SOURCE, symbol, matches, body);
    }

    private final SourceRange        sourceRange;
    private final Symbol             symbol;
    private final List<PatternMatch> matches;
    private final Value              body;

    private PatternMatcher(SourceRange sourceRange, Symbol symbol, List<PatternMatch> matches, Value body) {
        this.sourceRange = sourceRange;
        this.symbol = symbol;
        this.matches = ImmutableList.copyOf(matches);
        this.body = body;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof PatternMatcher) {
            PatternMatcher other = (PatternMatcher) o;
            return Objects.equals(symbol, other.symbol)
                && Objects.equals(matches, other.matches)
                && Objects.equals(body, other.body);
        } else {
            return false;
        }
    }

    public Value getBody() {
        return body;
    }

    public List<PatternMatch> getMatches() {
        return matches;
    }

    public DefinitionReference getReference() {
        return patternRef(symbol);
    }

    public SourceRange getSourceRange() {
        return sourceRange;
    }

    public Type getType() {
        return matches.stream()
            .map(PatternMatch::getType)
            .reduce(body.getType(), (result, argument) -> fn(argument, result));
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, matches, body);
    }

    @Override
    public String toString() {
        return stringify(this) + "(" + symbol + ")";
    }

    public PatternMatcher withBody(Value body) {
        return new PatternMatcher(sourceRange, symbol, matches, body);
    }

    public PatternMatcher withMatches(List<PatternMatch> matches) {
        return new PatternMatcher(sourceRange, symbol, matches, body);
    }

    public PatternMatcher withSourceRange(SourceRange sourceRange) {
        return new PatternMatcher(sourceRange, symbol, matches, body);
    }

    public PatternMatcher withType(Type type) {
        return new PatternMatcher(sourceRange, symbol, matches, body.withType(type));
    }
}
