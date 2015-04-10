package scotch.compiler.syntax.value;

import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.reference.DefinitionReference.valueRef;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.intermediate.Intermediates;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ConstantValue extends Value {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation sourceLocation;
    private final Symbol         symbol;
    private final Symbol         dataType;
    private final Type           type;

    ConstantValue(SourceLocation sourceLocation, Symbol dataType, Symbol symbol, Type type) {
        this.sourceLocation = sourceLocation;
        this.dataType = dataType;
        this.symbol = symbol;
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
        return Intermediates.valueRef(valueRef(symbol), state.valueSignature(valueRef(symbol)));
    }

    @Override
    public Value bindMethods(TypeChecker typeChecker) {
        return this;
    }

    @Override
    public Value bindTypes(TypeChecker typeChecker) {
        return withType(typeChecker.generate(type));
    }

    @Override
    public Value checkTypes(TypeChecker typeChecker) {
        return this;
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        return this;
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
        return this;
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        return withType(type.qualifyNames(state));
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        return this;
    }

    @Override
    public Value withType(Type type) {
        return new ConstantValue(sourceLocation, dataType, symbol, type);
    }

    public static class Builder implements SyntaxBuilder<ConstantValue> {

        private Optional<SourceLocation> sourceLocation;
        private Optional<Symbol>         dataType;
        private Optional<Symbol>         symbol;
        private Optional<Type>           type;

        private Builder() {
            sourceLocation = Optional.empty();
            dataType = Optional.empty();
            symbol = Optional.empty();
            type = Optional.empty();
        }

        @Override
        public ConstantValue build() {
            return new ConstantValue(
                require(sourceLocation, "Source location"),
                require(dataType, "Data type"),
                require(symbol, "Constant symbol"),
                require(type, "Constant type")
            );
        }

        public Builder withDataType(Symbol dataType) {
            this.dataType = Optional.of(dataType);
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
