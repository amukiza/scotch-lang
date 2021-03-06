package scotch.compiler.syntax.value;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static scotch.compiler.error.SymbolNotFoundError.symbolNotFound;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.reference.DefinitionReference.valueRef;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.syntax.value.Values.arg;
import static scotch.compiler.syntax.value.Values.id;
import static scotch.compiler.syntax.value.Values.method;
import static scotch.compiler.syntax.value.Values.unboundMethod;
import static scotch.compiler.util.Pair.pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.analyzer.TypeQualifier;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.text.SourceLocation;
import scotch.compiler.util.Pair;
import scotch.symbol.Operator;
import scotch.symbol.Symbol;
import scotch.symbol.Symbol.QualifiedSymbol;
import scotch.symbol.Symbol.SymbolVisitor;
import scotch.symbol.Symbol.UnqualifiedSymbol;
import scotch.symbol.descriptor.DataFieldDescriptor;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class Identifier extends Value {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation sourceLocation;
    private final Symbol         symbol;
    private final Type           type;

    Identifier(SourceLocation sourceLocation, Symbol symbol, Type type) {
        this.sourceLocation = sourceLocation;
        this.symbol = symbol;
        this.type = type;
    }

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return state.addDependency(this);
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        return this;
    }

    @Override
    public Optional<Value> asInitializer(Initializer initializer, TypeChecker state) {
        if (symbol.isConstructorName()) {
            return state.getDataConstructor(symbol)
                .flatMap(constructor -> {
                    List<InitializerField> initializerFields = checkInitializerFields(initializer.getFields(), state);
                    List<DataFieldDescriptor> descriptorFields = checkConstructorFields(constructor.getFields(), state);
                    if (!initializerFields.stream().map(InitializerField::getName).collect(toList())
                        .containsAll(descriptorFields.stream().map(DataFieldDescriptor::getName).collect(toList()))
                        || !descriptorFields.stream().map(DataFieldDescriptor::getName).collect(toList())
                        .containsAll(initializerFields.stream().map(InitializerField::getName).collect(toList()))) {
                        state.error(symbolNotFound(symbol, sourceLocation)); // TODO
                        return Optional.empty(); // TODO
                    }
                    List<InitializerField> sortedFields = sort(initializerFields, descriptorFields);
                    Value value = this;
                    for (InitializerField field : sortedFields) {
                        value = apply(value, field.getValue(), state.reserveType());
                    }
                    return Optional.of(value.checkTypes(state));
                });
        } else {
            return super.asInitializer(initializer, state);
        }
    }

    @Override
    public Optional<Pair<Identifier, Operator>> asOperator(Scope scope) {
        return scope.qualify(symbol)
            .flatMap(scope::getOperator)
            .map(operator -> pair(this, operator));
    }

    public Optional<Value> bind(Scope scope) {
        return scope.getValue(symbol).map(
            valueType -> symbol.accept(new SymbolVisitor<Value>() {
                @Override
                public Value visit(QualifiedSymbol symbol) {
                    if (scope.isMember(symbol) || valueType.hasContext()) {
                        return unboundMethod(sourceLocation, valueRef(symbol), valueType);
                    } else {
                        return method(sourceLocation, valueRef(symbol), asList(), valueType);
                    }
                }

                @Override
                public Value visit(UnqualifiedSymbol symbol) {
                    return arg(sourceLocation, symbol.getSimpleName(), valueType, Optional.empty());
                }
            }));
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
        return bind(typeChecker.scope())
            .map(value -> value.checkTypes(typeChecker))
            .orElseGet(() -> {
                typeChecker.error(symbolNotFound(symbol, sourceLocation));
                return this;
            });
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        return this;
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Type getType() {
        return type;
    }

    public boolean isConstructor() {
        return symbol.isConstructorName();
    }

    @Override
    public boolean isOperator(Scope scope) {
        return scope.isOperator(symbol);
    }

    @Override
    public Identifier parsePrecedence(PrecedenceParser state) {
        if (state.isOperator(symbol)) {
            return state.qualify(symbol)
                .map(this::withSymbol)
                .orElseGet(() -> {
                    state.symbolNotFound(symbol, sourceLocation);
                    return this;
                });
        } else {
            return this;
        }
    }

    @Override
    public Identifier qualifyNames(ScopedNameQualifier state) {
        return state.qualify(symbol)
            .map(this::withSymbol)
            .orElseGet(() -> {
                state.symbolNotFound(symbol, sourceLocation);
                return this;
            });
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        return this;
    }

    public Identifier withSourceLocation(SourceLocation sourceLocation) {
        return new Identifier(sourceLocation, symbol, type);
    }

    public Identifier withSymbol(Symbol symbol) {
        return new Identifier(sourceLocation, symbol, type);
    }

    public Identifier withType(Type type) {
        return new Identifier(sourceLocation, symbol, type);
    }

    private List<DataFieldDescriptor> checkConstructorFields(List<DataFieldDescriptor> fields, TypeChecker state) {
        List<DataFieldDescriptor> checkedFields = new ArrayList<>();
        fields.forEach(field -> {
            checkedFields.add(field.withType(field.getType().qualifyNames(new TypeQualifier(state)))); // TODO should not have to qualify type names here
        });
        return checkedFields;
    }

    private List<InitializerField> checkInitializerFields(List<InitializerField> fields, TypeChecker state) {
        return fields.stream()
            .map(field -> field.withType(field.getType().qualifyNames(new TypeQualifier(state)))) // TODO should not have to qualify type names here
            .reduce(
                new ArrayList<>(),
                (list, field) -> {
                    list.add(field);
                    return list;
                },
                (left, right) -> {
                    left.addAll(right);
                    return left;
                }
            );
    }

    private List<InitializerField> sort(List<InitializerField> initializerFields, List<DataFieldDescriptor> descriptorFields) {
        List<InitializerField> sortedFields = new ArrayList<>();
        List<String> initializerNames = initializerFields.stream().map(InitializerField::getName).collect(toList());
        List<String> descriptorNames = descriptorFields.stream().map(DataFieldDescriptor::getName).collect(toList());
        for (int i = 0; i < descriptorNames.size(); i++) {
            int index = initializerNames.indexOf(descriptorNames.get(i));
            sortedFields.add(i, initializerFields.get(index));
        }
        return sortedFields;
    }

    public static class Builder implements SyntaxBuilder<Identifier> {

        private Optional<Symbol>         symbol;
        private Optional<Type>           type;
        private Optional<SourceLocation> sourceLocation;

        private Builder() {
            symbol = Optional.empty();
            type = Optional.empty();
            sourceLocation = Optional.empty();
        }

        @Override
        public Identifier build() {
            return id(
                require(sourceLocation, "Source location"),
                require(symbol, "Identifier symbol"),
                require(type, "Identifier type")
            );
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
