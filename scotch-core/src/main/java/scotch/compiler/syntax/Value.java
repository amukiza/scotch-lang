package scotch.compiler.syntax;

import static java.util.Arrays.asList;
import static scotch.compiler.syntax.SourceRange.NULL_SOURCE;
import static scotch.compiler.syntax.Symbol.fromString;
import static scotch.compiler.util.TextUtil.stringify;

import java.util.List;
import java.util.Objects;
import com.google.common.collect.ImmutableList;

public abstract class Value implements SourceAware<Value> {

    public static Value apply(Value function, Value argument, Type type) {
        return new Apply(function.getSourceRange().extend(argument.getSourceRange()), function, argument, type);
    }

    public static Value id(String name, Type type) {
        return id(fromString(name), type);
    }

    public static Value id(Symbol symbol, Type type) {
        return new Identifier(NULL_SOURCE, symbol, type);
    }

    public static Value literal(Object value, Type type) {
        return new LiteralValue(NULL_SOURCE, value, type);
    }

    public static Value message(Value... members) {
        return message(asList(members));
    }

    public static Value message(List<Value> members) {
        return new Message(NULL_SOURCE, members);
    }

    public static Value patterns(Type type, PatternMatcher... patterns) {
        return patterns(type, asList(patterns));
    }

    public static Value patterns(Type type, List<PatternMatcher> patterns) {
        return new PatternMatchers(NULL_SOURCE, patterns, type);
    }

    private Value() {
        // intentionally empty
    }

    public abstract <T> T accept(ValueVisitor<T> visitor);

    @Override
    public abstract boolean equals(Object o);

    public abstract SourceRange getSourceRange();

    public abstract Type getType();

    @Override
    public abstract int hashCode();

    @Override
    public abstract String toString();

    public abstract Value withType(Type type);

    public interface ValueVisitor<T> {

        default T visit(Apply apply) {
            return visitOtherwise(apply);
        }

        default T visit(Identifier identifier) {
            return visitOtherwise(identifier);
        }

        default T visit(LiteralValue value) {
            return visitOtherwise(value);
        }

        default T visit(Message message) {
            return visitOtherwise(message);
        }

        default T visit(PatternMatchers matchers) {
            return visitOtherwise(matchers);
        }

        default T visitOtherwise(Value value) {
            throw new UnsupportedOperationException("Can't visit " + value.getClass().getSimpleName());
        }
    }

    public static class Apply extends Value {

        private final SourceRange sourceRange;
        private final Value       function;
        private final Value       argument;
        private final Type        type;

        private Apply(SourceRange sourceRange, Value function, Value argument, Type type) {
            this.sourceRange = sourceRange;
            this.function = function;
            this.argument = argument;
            this.type = type;
        }

        @Override
        public <T> T accept(ValueVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof Apply) {
                Apply other = (Apply) o;
                return Objects.equals(function, other.function)
                    && Objects.equals(argument, other.argument)
                    && Objects.equals(type, other.type);
            } else {
                return false;
            }
        }

        public Value getArgument() {
            return argument;
        }

        public Value getFunction() {
            return function;
        }

        @Override
        public SourceRange getSourceRange() {
            return sourceRange;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(function, argument, type);
        }

        @Override
        public String toString() {
            return stringify(this) + "(" + function + ", " + argument + ")";
        }

        public Apply withArgument(Value argument) {
            return new Apply(sourceRange, function, argument, type);
        }

        public Apply withFunction(Value function) {
            return new Apply(sourceRange, function, argument, type);
        }

        @Override
        public Apply withSourceRange(SourceRange sourceRange) {
            return new Apply(sourceRange, function, argument, type);
        }

        @Override
        public Apply withType(Type type) {
            return new Apply(sourceRange, function, argument, type);
        }
    }

    public static class Identifier extends Value {

        private final SourceRange sourceRange;
        private final Symbol      symbol;
        private final Type        type;

        private Identifier(SourceRange sourceRange, Symbol symbol, Type type) {
            this.sourceRange = sourceRange;
            this.symbol = symbol;
            this.type = type;
        }

        @Override
        public <T> T accept(ValueVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof Identifier) {
                Identifier other = (Identifier) o;
                return Objects.equals(symbol, other.symbol)
                    && Objects.equals(type, other.type);
            } else {
                return false;
            }
        }

        @Override
        public SourceRange getSourceRange() {
            return sourceRange;
        }

        public Symbol getSymbol() {
            return symbol;
        }

        public Type getType() {
            return type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(symbol, type);
        }

        @Override
        public String toString() {
            return stringify(this) + "(" + symbol + ")";
        }

        @Override
        public Identifier withSourceRange(SourceRange sourceRange) {
            return new Identifier(sourceRange, symbol, type);
        }

        public Identifier withSymbol(Symbol symbol) {
            return new Identifier(sourceRange, symbol, type);
        }

        public Identifier withType(Type type) {
            return new Identifier(sourceRange, symbol, type);
        }
    }

    public static class LiteralValue extends Value {

        private final SourceRange sourceRange;
        private final Object      value;
        private final Type        type;

        private LiteralValue(SourceRange sourceRange, Object value, Type type) {
            this.sourceRange = sourceRange;
            this.value = value;
            this.type = type;
        }

        @Override
        public <T> T accept(ValueVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof LiteralValue) {
                LiteralValue other = (LiteralValue) o;
                return Objects.equals(value, other.value)
                    && Objects.equals(type, other.type);
            } else {
                return false;
            }
        }

        @Override
        public SourceRange getSourceRange() {
            return sourceRange;
        }

        @Override
        public Type getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, type);
        }

        @Override
        public String toString() {
            return stringify(this) + "(" + value + ")";
        }

        @Override
        public LiteralValue withSourceRange(SourceRange sourceRange) {
            return new LiteralValue(sourceRange, value, type);
        }

        public LiteralValue withType(Type type) {
            return new LiteralValue(sourceRange, value, type);
        }
    }

    public static class Message extends Value {

        private final SourceRange sourceRange;
        private final List<Value> members;

        private Message(SourceRange sourceRange, List<Value> members) {
            this.sourceRange = sourceRange;
            this.members = ImmutableList.copyOf(members);
        }

        @Override
        public <T> T accept(ValueVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            return o == this || o instanceof Message && Objects.equals(members, ((Message) o).members);
        }

        public List<Value> getMembers() {
            return members;
        }

        @Override
        public SourceRange getSourceRange() {
            return sourceRange;
        }

        @Override
        public Type getType() {
            throw new IllegalStateException();
        }

        @Override
        public int hashCode() {
            return Objects.hash(members);
        }

        @Override
        public String toString() {
            return stringify(this) + "(" + members + ")";
        }

        @Override
        public Value withType(Type type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Message withSourceRange(SourceRange sourceRange) {
            return new Message(sourceRange, members);
        }
    }

    public static class PatternMatchers extends Value {

        private final SourceRange          sourceRange;
        private final List<PatternMatcher> matchers;
        private final Type                 type;

        private PatternMatchers(SourceRange sourceRange, List<PatternMatcher> matchers, Type type) {
            this.sourceRange = sourceRange;
            this.matchers = matchers;
            this.type = type;
        }

        @Override

        public <T> T accept(ValueVisitor<T> visitor) {
            return visitor.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof PatternMatchers) {
                PatternMatchers other = (PatternMatchers) o;
                return Objects.equals(matchers, other.matchers)
                    && Objects.equals(type, other.type);
            } else {
                return false;
            }
        }

        public List<PatternMatcher> getMatchers() {
            return matchers;
        }

        @Override
        public SourceRange getSourceRange() {
            return sourceRange;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(matchers);
        }

        @Override
        public String toString() {
            return stringify(this) + "(" + matchers + ")";
        }

        public PatternMatchers withMatchers(List<PatternMatcher> matchers) {
            return new PatternMatchers(sourceRange, matchers, type);
        }

        @Override
        public PatternMatchers withSourceRange(SourceRange sourceRange) {
            return new PatternMatchers(sourceRange, matchers, type);
        }

        @Override
        public PatternMatchers withType(Type type) {
            return new PatternMatchers(sourceRange, matchers, type);
        }
    }
}
