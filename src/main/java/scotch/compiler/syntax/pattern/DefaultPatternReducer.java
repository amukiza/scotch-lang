package scotch.compiler.syntax.pattern;

import static java.util.Collections.reverse;
import static scotch.compiler.syntax.value.Values.fn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import scotch.compiler.syntax.value.PatternMatcher;
import scotch.compiler.syntax.value.Value;
import scotch.symbol.util.SymbolGenerator;

public class DefaultPatternReducer implements PatternReducer {

    private final SymbolGenerator     generator;
    private final Deque<PatternState> patterns;

    public DefaultPatternReducer(SymbolGenerator generator) {
        this.generator = generator;
        patterns = new ArrayDeque<>();
    }

    @Override
    public void addAssignment(CaptureMatch capture) {
        patterns.peek().addAssignment(capture);
    }

    @Override
    public void beginPattern(PatternMatcher matcher) {
        patterns.push(new PatternState(matcher));
    }

    @Override
    public void beginPatternCase(Value body) {
        patterns.peek().beginPatternCase(body);
    }

    @Override
    public void endPattern() {
        patterns.pop();
    }

    @Override
    public void endPatternCase() {
        patterns.peek().endPatternCase();
    }

    @Override
    public Value reducePattern() {
        return patterns.peek().reducePattern();
    }

    private final class PatternState {

        private final PatternMatcher matcher;
        private final List<CaseState> cases;
        private CaseState currentCase;

        public PatternState(PatternMatcher matcher) {
            this.matcher = matcher;
            this.cases = new ArrayList<>();
        }

        public void addAssignment(CaptureMatch capture) {
            currentCase.addAssignment(capture);
        }

        public void beginPatternCase(Value body) {
            currentCase = new CaseState();
            currentCase.beginPatternCase(body);
        }

        public void endPatternCase() {
            cases.add(currentCase);
            currentCase = null;
        }

        public Value reducePattern() {
            return fn(
                matcher.getSourceLocation(),
                matcher.getSymbol(),
                matcher.getArguments(),
                cases.get(0).reducePattern()
            );
        }
    }

    private final class CaseState {

        private final List<CaptureMatch> assignments;
        private       Value              body;

        public CaseState() {
            this.assignments = new ArrayList<>();
        }

        public void addAssignment(CaptureMatch capture) {
            assignments.add(capture);
        }

        public void beginPatternCase(Value body) {
            this.body = body;
        }

        public Value reducePattern() {
            Value result = body;
            List<CaptureMatch> reverseAssignments = new ArrayList<>(assignments);
            reverse(reverseAssignments);
            for (CaptureMatch match : reverseAssignments) {
                result = match.reducePattern(generator, result);
            }
            return result;
        }
    }
}
