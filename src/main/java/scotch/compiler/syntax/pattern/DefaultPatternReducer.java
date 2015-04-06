package scotch.compiler.syntax.pattern;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import com.google.common.collect.ImmutableList;
import scotch.compiler.syntax.value.Argument;
import scotch.compiler.syntax.value.Value;
import scotch.symbol.util.SymbolGenerator;

public class DefaultPatternReducer implements PatternReducer {

    private final SymbolGenerator generator;
    private final Deque<State>    patterns;

    public DefaultPatternReducer(SymbolGenerator generator) {
        this.generator = generator;
        patterns = new ArrayDeque<>();
    }

    @Override
    public void beginPattern(List<Argument> arguments) {
        patterns.push(new State(arguments));
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
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value reducePattern() {
        return patterns.pop().reducePattern();
    }

    private final class State {

        private final List<Argument> arguments;
        private final List<Value>    bodies;

        private State(List<Argument> arguments) {
            this.arguments = ImmutableList.copyOf(arguments);
            this.bodies = new ArrayList<>();
        }

        public void beginPatternCase(Value body) {
            bodies.add(body);
        }

        public Value reducePattern() {
            throw new UnsupportedOperationException(); // TODO
        }
    }
}
