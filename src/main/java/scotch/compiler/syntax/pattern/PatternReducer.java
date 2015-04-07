package scotch.compiler.syntax.pattern;

import scotch.compiler.syntax.value.FunctionValue;
import scotch.compiler.syntax.value.PatternMatcher;
import scotch.compiler.syntax.value.Value;

public interface PatternReducer {

    void addAssignment(CaptureMatch capture);

    void addCondition(Value condition);

    void beginPattern(PatternMatcher matcher);

    void beginPatternCase(PatternCase patternCase);

    void endPattern();

    void endPatternCase();

    void markFunction(FunctionValue function);

    Value reducePattern();
}
