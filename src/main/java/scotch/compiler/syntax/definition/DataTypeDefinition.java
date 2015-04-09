package scotch.compiler.syntax.definition;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.reference.DefinitionReference.dataRef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PatternAnalyzer;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;
import scotch.symbol.descriptor.DataTypeDescriptor;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
public class DataTypeDefinition extends Definition {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation                         sourceLocation;
    private final Symbol                                 symbol;
    private final List<Type>                             parameters;
    private final Map<Symbol, DataConstructorDefinition> constructors;

    private DataTypeDefinition(SourceLocation sourceLocation, Symbol symbol, List<Type> parameters, List<DataConstructorDefinition> constructors) {
        List<DataConstructorDefinition> sortedConstructors = new ArrayList<>(constructors);
        sort(sortedConstructors);
        this.sourceLocation = sourceLocation;
        this.symbol = symbol;
        this.parameters = ImmutableList.copyOf(parameters);
        this.constructors = new LinkedHashMap<>();
        sortedConstructors.forEach(constructor -> this.constructors.put(constructor.getSymbol(), constructor));
    }

    @Override
    public Definition accumulateDependencies(DependencyAccumulator state) {
        return state.keep(this);
    }

    @Override
    public Definition accumulateNames(NameAccumulator state) {
        return state.scoped(this, () -> {
            state.defineDataType(symbol, getDescriptor());
            constructors.values().forEach(constructor -> constructor.accumulateNames(state));
            return this;
        });
    }

    @Override
    public Definition checkTypes(TypeChecker state) {
        return state.keep(this);
    }

    @Override
    public Definition defineOperators(OperatorAccumulator state) {
        return state.keep(this);
    }

    @Override
    public Optional<DefinitionReference> generateIntermediateCode(IntermediateGenerator generator) {
        return generator.defineData(symbol, parameters, constructors.values().stream()
            .map(constructor -> constructor.generateIntermediateCode(generator))
            .collect(toList()));
    }

    @Override
    public DefinitionReference getReference() {
        return dataRef(symbol);
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public Optional<Definition> parsePrecedence(PrecedenceParser state) {
        return Optional.of(state.keep(this));
    }

    @Override
    public Definition qualifyNames(ScopedNameQualifier state) {
        return state.scoped(this, () -> {
            DataTypeDefinition definition = new DataTypeDefinition(
                sourceLocation,
                symbol,
                state.qualifyTypeNames(parameters),
                constructors.values().stream()
                    .map(constructor -> constructor.qualifyNames(state))
                    .collect(toList())
            );
            state.redefineDataType(symbol, definition.getDescriptor());
            return definition;
        });
    }

    @Override
    public Definition reducePatterns(PatternAnalyzer state) {
        return state.keep(this);
    }

    @Override
    public String toString() {
        return symbol.getSimpleName()
            + (parameters.isEmpty() ? "" : " " + parameters.stream().map(Object::toString).collect(joining(", ")))
            + " = " + constructors.values().stream().map(Object::toString).collect(joining(" | "));
    }

    private DataTypeDescriptor getDescriptor() {
        return DataTypeDescriptor.builder(symbol)
            .withParameters(parameters)
            .withConstructors(constructors.values().stream()
                .map(DataConstructorDefinition::getDescriptor)
                .collect(toList()))
            .build();
    }

    public static class Builder implements SyntaxBuilder<DataTypeDefinition> {

        private Optional<SourceLocation>                  sourceLocation;
        private Optional<Symbol>                          symbol;
        private List<Type>                                parameters;
        private Optional<List<DataConstructorDefinition>> constructors;

        private Builder() {
            sourceLocation = Optional.empty();
            symbol = Optional.empty();
            parameters = new ArrayList<>();
            constructors = Optional.empty();
        }

        public Builder addConstructor(DataConstructorDefinition constructor) {
            if (!constructors.isPresent()) {
                constructors = Optional.of(new ArrayList<>());
            }
            constructors.map(list -> list.add(constructor));
            return this;
        }

        public Builder addParameter(Type type) {
            this.parameters.add(type);
            return this;
        }

        @Override
        public DataTypeDefinition build() {
            return new DataTypeDefinition(
                require(sourceLocation, "Source location"),
                require(symbol, "Data type symbol"),
                parameters,
                require(constructors, "No constructors defined")
            );
        }

        public Builder withConstructors(List<DataConstructorDefinition> constructors) {
            constructors.forEach(this::addConstructor);
            return this;
        }

        public Builder withParameters(List<Type> parameters) {
            parameters.forEach(this::addParameter);
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
    }
}
