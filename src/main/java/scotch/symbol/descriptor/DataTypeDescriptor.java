package scotch.symbol.descriptor;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static scotch.symbol.type.Types.sum;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;
import scotch.symbol.type.VariableType;

public class DataTypeDescriptor {

    public static Builder builder(Symbol symbol) {
        return new Builder(symbol);
    }

    private final Symbol                                 symbol;
    private final List<Type>                             parameters;
    private final Map<Symbol, DataConstructorDescriptor> constructors;

    private DataTypeDescriptor(Symbol symbol, List<Type> parameters, List<DataConstructorDescriptor> constructors) {
        List<DataConstructorDescriptor> sortedConstructors = new ArrayList<>(constructors);
        sort(sortedConstructors);
        this.symbol = symbol;
        this.parameters = new ArrayList<>(parameters);
        this.constructors = new LinkedHashMap<>();
        sortedConstructors.forEach(constructor -> this.constructors.put(constructor.getSymbol(), constructor));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof DataTypeDescriptor) {
            DataTypeDescriptor other = (DataTypeDescriptor) o;
            return Objects.equals(symbol, other.symbol)
                && Objects.equals(parameters, other.parameters)
                && Objects.equals(constructors, other.constructors);
        } else {
            return false;
        }
    }

    public Optional<DataConstructorDescriptor> getConstructor(Symbol symbol) {
        return Optional.ofNullable(constructors.get(symbol));
    }

    public List<DataConstructorDescriptor> getConstructors() {
        return ImmutableList.copyOf(constructors.values());
    }

    public List<Type> getParameters() {
        return parameters;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Type getType() {
        return sum(symbol, parameters);
    }

    public Type getType(Supplier<VariableType> generator) {
        return sum(symbol, parameters.stream()
            .map(parameter -> generator.get().withContext(parameter.getContext()))
            .collect(toList()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, parameters, constructors);
    }

    public DataTypeDescriptor mapParameters(Map<Type, Type> mappedParameters) {
        List<Type> reifiedParameters = parameters.stream()
            .map(parameter -> Optional.of(mappedParameters.get(parameter)).<RuntimeException>orElseThrow(UnsupportedOperationException::new /* TODO */))
            .collect(toList());
        return new DataTypeDescriptor(symbol, reifiedParameters, constructors.values().stream()
            .map(constructor -> constructor.mapParameters(mappedParameters))
            .collect(toList()));
    }

    @Override
    public String toString() {
        return symbol.getSimpleName()
            + (parameters.isEmpty() ? "" : " " + parameters.stream().map(Object::toString).collect(joining(", ")))
            + " = " + constructors.values().stream().map(Object::toString).collect(joining(" | "));
    }

    public static final class Builder {

        private final Symbol                          symbol;
        private       Optional<String>                className;
        private       List<Type>                      parameters;
        private       List<DataConstructorDescriptor> constructors;

        private Builder(Symbol symbol) {
            this.symbol = symbol;
            this.className = Optional.empty();
            this.parameters = new ArrayList<>();
            this.constructors = new ArrayList<>();
        }

        public Builder addConstructor(DataConstructorDescriptor constructor) {
            constructors.add(constructor);
            return this;
        }

        public Builder addParameter(Type type) {
            parameters.add(type);
            return this;
        }

        public DataTypeDescriptor build() {
            return new DataTypeDescriptor(symbol, parameters, constructors);
        }

        public Builder withClassName(String className) {
            this.className = Optional.of(className);
            return this;
        }

        public Builder withConstructors(List<DataConstructorDescriptor> constructors) {
            constructors.forEach(this::addConstructor);
            return this;
        }

        public Builder withParameters(List<Type> parameters) {
            parameters.forEach(this::addParameter);
            return this;
        }
    }
}
