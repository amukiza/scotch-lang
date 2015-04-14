package scotch.compiler.syntax.pattern;

import scotch.compiler.syntax.value.FunctionValue;
import scotch.compiler.syntax.value.IsConstructor;
import scotch.compiler.syntax.value.PatternMatcher;
import scotch.compiler.syntax.value.Value;

public interface PatternReducer {

    void addAssignment(CaptureMatch capture);

    void addCondition(Value argument, Value value);

    void addCondition(IsConstructor constructor);

    void addTaggedArgument(Value taggedArgument);

    void beginPattern(PatternMatcher matcher);

    void beginPatternCase(PatternCase patternCase);

    void endPattern();

    void endPatternCase();

    Value getTaggedArgument(Value argument);

    void markFunction(FunctionValue function);

    Value reducePattern();
}
