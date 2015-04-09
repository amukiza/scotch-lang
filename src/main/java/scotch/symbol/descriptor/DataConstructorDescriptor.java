package scotch.symbol.descriptor;

import static java.util.Collections.sort;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import scotch.runtime.Callable;
import scotch.symbol.MethodSignature;
import scotch.symbol.Symbol;

@EqualsAndHashCode(callSuper = false)
public class DataConstructorDescriptor implements Comparable<DataConstructorDescriptor> {

    public static Builder builder(int ordinal, Symbol dataType, Symbol symbol, String className) {
        return new Builder(ordinal, dataType, symbol, className);
    }

    @Getter private final int                       ordinal;
    @Getter private final Symbol                    dataType;
    @Getter private final Symbol                    symbol;
    @Getter private final String                    className;
    @Getter private final List<DataFieldDescriptor> fields;

    private DataConstructorDescriptor(int ordinal, Symbol dataType, Symbol symbol, String className, List<DataFieldDescriptor> fields) {
        List<DataFieldDescriptor> sortedFields = new ArrayList<>(fields);
        sort(sortedFields);
        this.ordinal = ordinal;
        this.dataType = dataType;
        this.symbol = symbol;
        this.className = className;
        this.fields = ImmutableList.copyOf(sortedFields);
    }

    @Override
    public int compareTo(DataConstructorDescriptor o) {
        return ordinal - o.ordinal;
    }

    public MethodSignature getConstructorSignature() {
        return MethodSignature.constructor(className + ":<init>:"
            + sig(void.class, fields.stream().map(field -> Callable.class).collect(toList()).toArray(new Class<?>[fields.size()])));
    }

    @Override
    public String toString() {
        return symbol.getSimpleName()
            + (fields.isEmpty() ? "" : " { " + fields.stream().map(Object::toString).collect(joining(", ")) + " }");
    }

    public static final class Builder {

        private final int                       ordinal;
        private final Symbol                    dataType;
        private final Symbol                    symbol;
        private final String                    className;
        private       List<DataFieldDescriptor> fields;

        private Builder(int ordinal, Symbol dataType, Symbol symbol, String className) {
            this.ordinal = ordinal;
            this.dataType = dataType;
            this.symbol = symbol;
            this.className = className;
            this.fields = new ArrayList<>();
        }

        public Builder addField(DataFieldDescriptor field) {
            this.fields.add(field);
            return this;
        }

        public DataConstructorDescriptor build() {
            sort(fields);
            return new DataConstructorDescriptor(
                ordinal,
                dataType,
                symbol,
                className,
                fields
            );
        }

        public Builder withFields(List<DataFieldDescriptor> fields) {
            fields.forEach(this::addField);
            return this;
        }
    }
}
