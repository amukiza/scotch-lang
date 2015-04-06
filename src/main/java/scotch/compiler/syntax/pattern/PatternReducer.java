package scotch.compiler.syntax.pattern;

import java.util.List;
import scotch.compiler.syntax.value.Argument;
import scotch.compiler.syntax.value.PatternMatcher;
import scotch.compiler.syntax.value.Value;

public interface PatternReducer {

    void addAssignment(CaptureMatch capture);

    void beginPattern(PatternMatcher matcher);

    void beginPatternCase(Value body);

    void endPattern();

    void endPatternCase();

    Value reducePattern();
}
