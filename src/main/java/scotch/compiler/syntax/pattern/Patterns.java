package scotch.compiler.syntax.pattern;

import java.util.List;
import java.util.Optional;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;
import scotch.compiler.syntax.value.Value;
import scotch.compiler.text.SourceLocation;

public final class Patterns {

    public static CaptureMatch capture(SourceLocation sourceLocation, Optional<Value> argument, Symbol symbol, Type type) {
        return new CaptureMatch(sourceLocation, argument, symbol, type);
    }

    public static EqualMatch equal(SourceLocation sourceLocation, Optional<Value> argument, Value value) {
        return new EqualMatch(sourceLocation, argument, value);
    }

    public static IgnorePattern ignore(SourceLocation sourceLocation, Type type) {
        return new IgnorePattern(sourceLocation, type);
    }

    public static StructField field(SourceLocation sourceLocation, String field, Type type, PatternMatch patternMatch) {
        return new StructField(sourceLocation, field, type, patternMatch);
    }

    public static PatternCase pattern(SourceLocation sourceLocation, Symbol symbol, List<PatternMatch> patternMatches, Value body) {
        return new PatternCase(sourceLocation, symbol, patternMatches, body);
    }

    public static StructMatch struct(SourceLocation sourceLocation, Optional<Value> argument, Symbol dataType, Type type, List<StructField> fields) {
        return new StructMatch(sourceLocation, argument, dataType, type, fields);
    }

    public static UnshuffledStructMatch unshuffledMatch(SourceLocation sourceLocation, Type type, List<PatternMatch> patternMatches) {
        return new UnshuffledStructMatch(sourceLocation, type, patternMatches);
    }

    private Patterns() {
        // intentionally empty
    }
}
