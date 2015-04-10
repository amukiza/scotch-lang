package scotch.compiler.syntax.value;

import static java.util.Collections.reverse;
import static java.util.stream.Collectors.toList;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.definition.Definitions.scopeDef;
import static scotch.compiler.syntax.reference.DefinitionReference.scopeRef;
import static scotch.compiler.syntax.value.Values.matcher;
import static scotch.symbol.type.Types.fn;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.PrecedenceParser.ArityMismatch;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.syntax.Scoped;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.definition.Definition;
import scotch.compiler.syntax.pattern.PatternCase;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation", doNotUseGetters = true)
public class PatternMatcher extends Value implements Scoped {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation    sourceLocation;
    private final Symbol            symbol;
    private final List<Argument>    arguments;
    private final List<PatternCase> patternCases;
    private final Type              type;

    PatternMatcher(SourceLocation sourceLocation, Symbol symbol, List<Argument> arguments, List<PatternCase> patternCases, Type type) {
        this.sourceLocation = sourceLocation;
        this.symbol = symbol;
        this.arguments = ImmutableList.copyOf(arguments);
        this.patternCases = ImmutableList.copyOf(patternCases);
        this.type = type;
    }

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        return state.scoped(this, () -> map(
            argument -> argument.accumulateNames(state),
            patternCase -> patternCase.accumulateNames(state)
        ));
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value bindMethods(TypeChecker typeChecker) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value bindTypes(TypeChecker typeChecker) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value checkTypes(TypeChecker typeChecker) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        return state.scoped(this, () -> map(
            argument -> argument,
            patternCase -> patternCase.defineOperators(state)
        ));
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public Definition getDefinition() {
        return scopeDef(sourceLocation, symbol);
    }

    @Override
    public DefinitionReference getReference() {
        return scopeRef(symbol);
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        Symbol s = symbol.getMemberNames().size() == 1 ? state.reserveSymbol() : symbol;
        patternCases.stream()
            .filter(pattern -> pattern.getArity() != arguments.size())
            .map(pattern -> new ArityMismatch(s, arguments.size(), pattern.getArity(), pattern.getSourceLocation()))
            .forEach(state::error);
        return state.named(s, () -> state.scoped(this, () -> withSymbol(s).withPatternCases(patternCases.stream()
            .map(matcher -> matcher.parsePrecedence(state))
            .collect(toList()))));
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        return state.named(symbol, () -> state.scoped(this, () -> map(
            argument -> argument.qualifyNames(state),
            patternCase -> patternCase.qualifyNames(state)
        )));
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        reducer.beginPattern(this);
        try {
            patternCases.forEach(patternCase -> patternCase.reducePatterns(reducer));
            return reducer.reducePattern();
        } finally {
            reducer.endPattern();
        }
    }

    @Override
    public Value unwrap() {
        return withPatternCases(
            patternCases.stream()
                .map(matcher -> matcher.withBody(matcher.getBody().unwrap()))
                .collect(toList())
        );
    }

    @Override
    public WithArguments withArguments() {
        return WithArguments.withArguments(this);
    }

    public PatternMatcher withArguments(List<Argument> arguments) {
        return new PatternMatcher(sourceLocation, symbol, arguments, patternCases, type);
    }

    public PatternMatcher withPatternCases(List<PatternCase> patternCases) {
        return new PatternMatcher(sourceLocation, symbol, arguments, patternCases, type);
    }

    public PatternMatcher withSourceLocation(SourceLocation sourceLocation) {
        return new PatternMatcher(sourceLocation, symbol, arguments, patternCases, type);
    }

    public PatternMatcher withSymbol(Symbol symbol) {
        return new PatternMatcher(sourceLocation, symbol, arguments, patternCases, type);
    }

    @Override
    public PatternMatcher withType(Type type) {
        return new PatternMatcher(sourceLocation, symbol, arguments, patternCases, type);
    }

    private Type calculateType(Type returnType) {
        List<Argument> args = new ArrayList<>(arguments);
        reverse(args);
        return args.stream()
            .map(Argument::getType)
            .reduce(returnType, (result, arg) -> fn(arg, result));
    }

    private PatternMatcher encloseArguments(TypeChecker state, Supplier<PatternMatcher> supplier) {
        return state.enclose(this, () -> {
            arguments.stream()
                .map(Argument::getType)
                .forEach(state::specialize);
            arguments.stream()
                .map(Argument::getSymbol)
                .forEach(state::addLocal);
            try {
                return supplier.get();
            } finally {
                arguments.stream()
                    .map(Argument::getType)
                    .forEach(state::generalize);
            }
        });
    }

    private PatternMatcher map(Function<Argument, Argument> argumentMapper, Function<PatternCase, PatternCase> patternCaseMapper) {
        return new PatternMatcher(
            sourceLocation, symbol,
            arguments.stream().map(argumentMapper).collect(toList()),
            patternCases.stream().map(patternCaseMapper).collect(toList()),
            type
        );
    }

    public static class Builder implements SyntaxBuilder<PatternMatcher> {

        private Optional<SourceLocation>    sourceLocation = Optional.empty();
        private Optional<List<Argument>>    arguments      = Optional.empty();
        private Optional<List<PatternCase>> patternCases   = Optional.empty();
        private Optional<Type>              type           = Optional.empty();
        private Optional<Symbol>            symbol         = Optional.empty();

        private Builder() {
            // intentionally empty
        }

        @Override
        public PatternMatcher build() {
            return matcher(
                require(sourceLocation, "Source location"),
                require(symbol, "Symbol"),
                require(type, "Pattern type"),
                require(arguments, "Arguments"),
                require(patternCases, "Pattern cases")
            );
        }

        public Builder withArguments(List<Argument> arguments) {
            this.arguments = Optional.of(arguments);
            return this;
        }

        public Builder withPatterns(List<PatternCase> patterns) {
            this.patternCases = Optional.of(patterns);
            return this;
        }

        @Override
        public Builder withSourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = Optional.of(sourceLocation);
            return this;
        }

        public Builder withSymbol(Symbol symbol) {
            this.symbol = Optional.of(symbol);
            return this;
        }

        public Builder withType(Type type) {
            this.type = Optional.of(type);
            return this;
        }
    }
}
