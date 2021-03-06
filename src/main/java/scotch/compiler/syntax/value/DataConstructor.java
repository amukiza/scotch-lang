package scotch.compiler.syntax.value;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static scotch.compiler.syntax.builder.BuilderUtil.require;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
public class DataConstructor extends Value {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation sourceLocation;
    private final Symbol         symbol;
    private final List<Value>    arguments;
    private final Type           type;

    DataConstructor(SourceLocation sourceLocation, Symbol symbol, Type type, List<Value> arguments) {
        this.sourceLocation = sourceLocation;
        this.symbol = symbol;
        this.arguments = ImmutableList.copyOf(arguments);
        this.type = type;
    }

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return this;
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        return this;
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        return state.createConstructor(
            symbol,
            state.getDataConstructor(symbol).getClassName(),
            state.getDataConstructor(symbol).getConstructorSignature(),
            arguments.stream()
                .map(argument -> argument.generateIntermediateCode(state))
                .collect(toList()));
    }

    @Override
    public Value bindMethods(TypeChecker typeChecker) {
        return withArguments(typeChecker.bindMethods(arguments));
    }

    @Override
    public Value bindTypes(TypeChecker typeChecker) {
        return withType(typeChecker.generate(type))
            .withArguments(typeChecker.bindTypes(arguments));
    }

    @Override
    public Value checkTypes(TypeChecker typeChecker) {
        return withArguments(typeChecker.checkTypes(arguments));
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        return withArguments(state.defineValueOperators(arguments));
    }

    private DataConstructor withArguments(List<Value> arguments) {
        return new DataConstructor(sourceLocation, symbol, type, arguments);
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        return withArguments(arguments.stream()
            .map(argument -> argument.parsePrecedence(state))
            .collect(toList()));
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        return withArguments(state.qualifyValueNames(arguments))
            .withType(type.qualifyNames(state));
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        return withArguments(arguments.stream().map(argument -> argument.reducePatterns(reducer)).collect(toList()));
    }

    @Override
    public String toString() {
        return symbol.toString() + "(" + arguments.stream().map(Object::toString).collect(joining(", ")) + ")";
    }

    @Override
    public DataConstructor withType(Type type) {
        return new DataConstructor(sourceLocation, symbol, type, arguments);
    }

    public static class Builder implements SyntaxBuilder<DataConstructor> {

        private Optional<SourceLocation> sourceLocation;
        private Optional<Symbol>         symbol;
        private List<Value>              arguments;
        private Optional<Type>           type;

        public Builder() {
            sourceLocation = Optional.empty();
            symbol = Optional.empty();
            arguments = new ArrayList<>();
            type = Optional.empty();
        }

        @Override
        public DataConstructor build() {
            return new DataConstructor(
                require(sourceLocation, "Source location"),
                require(symbol, "Constructor symbol"),
                require(type, "Constructor type"),
                arguments
            );
        }

        public Builder withArguments(List<Value> arguments) {
            this.arguments.addAll(arguments);
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
