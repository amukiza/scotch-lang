package scotch.compiler.syntax.value;

import static scotch.compiler.syntax.builder.BuilderUtil.require;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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
import scotch.symbol.FieldSignature;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ConstantReference extends Value {

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    private final SourceLocation sourceLocation;
    private final Symbol         symbol;
    private final Symbol         dataType;
    private final FieldSignature constantField;
    @Getter
    private final Type           type;

    @java.beans.ConstructorProperties({ "sourceLocation", "symbol", "dataType", "constantField", "type" })
    ConstantReference(SourceLocation sourceLocation, Symbol symbol, Symbol dataType, FieldSignature constantField, Type type) {
        this.sourceLocation = sourceLocation;
        this.symbol = symbol;
        this.dataType = dataType;
        this.constantField = constantField;
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
        return state.constantReference(symbol, dataType, constantField);
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
    public Value parsePrecedence(PrecedenceParser state) {
        return this;
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        return this;
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        return this;
    }

    @Override
    public Value withType(Type type) {
        return new ConstantReference(sourceLocation, symbol, dataType, constantField, type);
    }

    public static class Builder implements SyntaxBuilder<ConstantReference> {

        private Optional<SourceLocation> sourceLocation   = Optional.empty();
        private Optional<Symbol>         symbol        = Optional.empty();
        private Optional<Symbol>         dataType      = Optional.empty();
        private Optional<Type>           type          = Optional.empty();
        private Optional<FieldSignature> constantField = Optional.empty();

        private Builder() {
            // intentionally empty
        }

        @Override
        public ConstantReference build() {
            return new ConstantReference(
                require(sourceLocation, "Source location"),
                require(symbol, "Symbol"),
                require(dataType, "Data type"),
                require(constantField, "Constant field"),
                require(type, "Type")
            );
        }

        public Builder withConstantField(FieldSignature constantField) {
            this.constantField = Optional.of(constantField);
            return this;
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
