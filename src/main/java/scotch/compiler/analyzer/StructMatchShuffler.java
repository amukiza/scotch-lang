package scotch.compiler.analyzer;

import static scotch.compiler.error.ParseError.parseError;
import static scotch.compiler.syntax.pattern.Patterns.field;
import static scotch.compiler.util.Either.left;
import static scotch.compiler.util.Either.right;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import scotch.compiler.error.SyntaxError;
import scotch.compiler.syntax.pattern.EqualMatch;
import scotch.compiler.syntax.pattern.PatternMatch;
import scotch.compiler.syntax.pattern.StructMatch;
import scotch.compiler.syntax.pattern.StructMatch.Builder;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.util.Either;
import scotch.symbol.Symbol;
import scotch.symbol.descriptor.DataFieldDescriptor;
import scotch.symbol.type.Type;

public class StructMatchShuffler {

    private static Either<SyntaxError, PatternMatch> success(PatternMatch patternMatch) {
        return right(patternMatch);
    }

    public Either<SyntaxError, PatternMatch> shuffle(Scope scope, List<PatternMatch> patternMatches) {
        if (patternMatches.size() == 1) {
            return success(patternMatches.get(0));
        } else {
            try {
                return success(new Shuffler(scope, patternMatches).shuffle());
            } catch (ShuffleException exception) {
                return left(exception.syntaxError);
            }
        }
    }

    private static final class ShuffleException extends RuntimeException {

        private final SyntaxError syntaxError;

        private ShuffleException(SyntaxError syntaxError) {
            super(syntaxError.prettyPrint());
            this.syntaxError = syntaxError;
        }
    }

    private final class Shuffler {

        private final Scope              scope;
        private final List<PatternMatch> patternMatches;

        public Shuffler(Scope scope, List<PatternMatch> patternMatches) {
            this.scope = scope;
            this.patternMatches = patternMatches;
        }

        public PatternMatch shuffle() {
            Deque<PatternMatch> input = new ArrayDeque<>(patternMatches);
            Deque<Either<OperatorPair<EqualMatch>, PatternMatch>> output = new ArrayDeque<>();
            Deque<OperatorPair<EqualMatch>> stack = new ArrayDeque<>();
            boolean expectsPrefix = isOperator(input.peek());
            while (!input.isEmpty()) {
                if (isOperator(input.peek())) {
                    OperatorPair<EqualMatch> o1 = getOperator(input.poll(), expectsPrefix);
                    while (!stack.isEmpty() && o1.isLessPrecedentThan(stack.peek())) {
                        output.push(left(stack.pop()));
                    }
                    stack.push(o1);
                    expectsPrefix = isOperator(input.peek());
                } else {
                    output.push(right(shuffleNext(input)));
                    while (expectsArgument(input)) {
                        output.push(appendStructMatch(
                            output.pop().orElseGet(OperatorPair::getValue),
                            shuffleNext(input)
                        ));
                    }
                }
            }
            while (!stack.isEmpty()) {
                output.push(left(stack.pop()));
            }
            return shuffleApply(output);
        }

        private PatternMatch shuffleApply(Deque<Either<OperatorPair<EqualMatch>, PatternMatch>> message) {
            Deque<PatternMatch> stack = new ArrayDeque<>();
            while (!message.isEmpty()) {
                stack.push(message.pollLast().orElseGet(pair -> {
                    if (pair.isPrefix()) {
                        return createStructMatch(pair.getValue(), stack.pop());
                    } else {
                        PatternMatch right = stack.pop();
                        PatternMatch left = stack.pop();
                        return createStructMatch(pair.getValue(), left, right);
                    }
                }));
            }
            if (stack.size() > 1) {
                throw new UnsupportedOperationException(); // TODO
            }
            return stack.pop();
        }

        private PatternMatch createStructMatch(EqualMatch equalMatch, PatternMatch... fieldMatches) {
            Symbol constructor = equalMatch.getSymbol();
            List<DataFieldDescriptor> fields = scope.getDataConstructor(constructor).get().getFields();
            if (fields.size() == fieldMatches.length) {
                Builder builder = StructMatch.builder()
                    .withType(reserveType())
                    .withConstructor(constructor)
                    .withSourceLocation(equalMatch.getSourceLocation());
                for (int i = 0; i < fields.size(); i++) {
                    builder.withField(field(
                        fieldMatches[i].getSourceLocation(),
                        fields.get(i).getName(),
                        reserveType(),
                        fieldMatches[i]
                    ));
                }
                return builder.build();
            } else {
                throw new UnsupportedOperationException(); // TODO
            }
        }

        private Type reserveType() {
            return scope.reserveType();
        }

        private Either<OperatorPair<EqualMatch>, PatternMatch> appendStructMatch(PatternMatch patternMatch, PatternMatch patternMatch1) {
            throw new UnsupportedOperationException(); // TODO
        }

        private boolean expectsArgument(Deque<PatternMatch> input) {
            return !input.isEmpty() && expectsArgument_(input);
        }

        private boolean expectsArgument_(Deque<PatternMatch> input) {
            return !isOperator(input.peek());
        }

        private OperatorPair<EqualMatch> getOperator(PatternMatch patternMatch, boolean expectsPrefix) {
            return patternMatch.asConstructorOperator(scope)
                .map(pair -> pair.into((match, operator) -> {
                    if (expectsPrefix && !operator.isPrefix()) {
                        throw new ShuffleException(parseError("Unexpected binary operator " + match.getSymbol(), match.getSourceLocation()));
                    } else {
                        return new OperatorPair<>(operator, match);
                    }
                }))
                .orElseThrow(() -> new ShuffleException(parseError("Value " + patternMatch.prettyPrint() + " is not an operator", patternMatch.getSourceLocation())));
        }

        private boolean isOperator(PatternMatch value) {
            return value.isOperator(scope);
        }

        private PatternMatch shuffleNext(Deque<PatternMatch> input) {
            return input.poll().destructure()
                .map(patternMatches -> StructMatchShuffler.this.shuffle(scope, patternMatches).orElseThrow(ShuffleException::new))
                .orElseGet(left -> left);
        }
    }
}
