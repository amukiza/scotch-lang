package scotch.compiler.syntax.definition;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static me.qmx.jitescript.util.CodegenUtils.ci;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static scotch.compiler.intermediate.Intermediates.constructor;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.symbol.FieldSignature.fieldSignature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.intermediate.IntermediateConstructorDefinition;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.text.SourceLocation;
import scotch.runtime.Callable;
import scotch.symbol.FieldSignature;
import scotch.symbol.Symbol;
import scotch.symbol.descriptor.DataConstructorDescriptor;

@EqualsAndHashCode(callSuper = false, doNotUseGetters = true)
public class DataConstructorDefinition implements Comparable<DataConstructorDefinition> {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation                   sourceLocation;
    private final int                              ordinal;
    private final Symbol                           dataType;
    private final Symbol                           symbol;
    private final Map<String, DataFieldDefinition> fields;
    private final Optional<FieldSignature>         constantField;

    private DataConstructorDefinition(SourceLocation sourceLocation, int ordinal, Symbol dataType, Symbol symbol, List<DataFieldDefinition> fields) {
        List<DataFieldDefinition> sortedFields = new ArrayList<>(fields);
        sort(sortedFields);
        this.sourceLocation = sourceLocation;
        this.ordinal = ordinal;
        this.dataType = dataType;
        this.symbol = symbol;
        this.fields = new LinkedHashMap<>();
        sortedFields.forEach(field -> this.fields.put(field.getName(), field));
        if (fields.isEmpty()) {
            String className = symbol.getClassNameAsChildOf(dataType);
            constantField = Optional.of(fieldSignature(className, ACC_STATIC | ACC_PUBLIC | ACC_FINAL, "INSTANCE", ci(Callable.class)));
        } else {
            constantField = Optional.empty();
        }
    }

    public void accumulateNames(NameAccumulator state) {
        state.defineDataConstructor(symbol, getDescriptor());
    }

    @Override
    public int compareTo(DataConstructorDefinition o) {
        return ordinal - o.ordinal;
    }

    public IntermediateConstructorDefinition generateIntermediateCode(IntermediateGenerator state) {
        return constructor(symbol, dataType, fields.values().stream()
            .map(field -> field.generateIntermediateCode(state))
            .collect(toList()));
    }

    public FieldSignature getConstantField() {
        return constantField.orElseThrow(() -> new IllegalStateException("Data constructor " + symbol + " is not niladic"));
    }

    public Symbol getDataType() {
        return dataType;
    }

    public DataConstructorDescriptor getDescriptor() {
        return DataConstructorDescriptor.builder(ordinal, dataType, symbol, symbol.getClassNameAsChildOf(dataType))
            .withFields(fields.values().stream()
                .map(DataFieldDefinition::getDescriptor)
                .collect(toList()))
            .build();
    }

    public List<DataFieldDefinition> getFields() {
        return new ArrayList<>(fields.values());
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public boolean isNiladic() {
        return fields.isEmpty();
    }

    public DataConstructorDefinition qualifyNames(ScopedNameQualifier state) {
        DataConstructorDefinition definition = withFields(fields.values().stream()
            .map(field -> field.qualifyNames(state))
            .collect(toList()));
        state.redefineDataConstructor(symbol, getDescriptor());
        return definition;
    }

    @Override
    public String toString() {
        return symbol.getSimpleName()
            + (fields.isEmpty() ? "" : " { " + fields.values().stream().map(Object::toString).collect(joining(", ")) + " }");
    }

    private Class<?>[] getParameters() {
        List<Class<?>> parameters = fields.values().stream()
            .map(DataFieldDefinition::getJavaType)
            .collect(toList());
        return parameters.toArray(new Class<?>[parameters.size()]);
    }

    private DataConstructorDefinition withFields(List<DataFieldDefinition> fields) {
        return new DataConstructorDefinition(sourceLocation, ordinal, dataType, symbol, fields);
    }

    public static class Builder implements SyntaxBuilder<DataConstructorDefinition> {

        private Optional<SourceLocation>  sourceLocation = Optional.empty();
        private Optional<Integer>         ordinal     = Optional.empty();
        private Optional<Symbol>          dataType    = Optional.empty();
        private Optional<Symbol>          symbol      = Optional.empty();
        private List<DataFieldDefinition> fields      = new ArrayList<>();

        public Builder addField(DataFieldDefinition field) {
            fields.add(field);
            return this;
        }

        @Override
        public DataConstructorDefinition build() {
            return new DataConstructorDefinition(
                require(sourceLocation, "Source location"),
                require(ordinal, "Ordinal"),
                require(dataType, "Constructor data type"),
                require(symbol, "Constructor symbol"),
                fields
            );
        }

        public Builder withDataType(Symbol dataType) {
            this.dataType = Optional.of(dataType);
            return this;
        }

        public Builder withFields(List<DataFieldDefinition> fields) {
            fields.forEach(this::addField);
            return this;
        }

        public Builder withOrdinal(int ordinal) {
            this.ordinal = Optional.of(ordinal);
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
