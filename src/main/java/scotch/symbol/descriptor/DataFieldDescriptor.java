package scotch.symbol.descriptor;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import scotch.symbol.type.Type;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
public class DataFieldDescriptor implements Comparable<DataFieldDescriptor> {

    public static DataFieldDescriptor field(int ordinal, String name, String methodName, Type type) {
        return new DataFieldDescriptor(ordinal, name, methodName, type);
    }

    private final int    ordinal;
    private final String name;
    private final String methodName;
    private final Type   type;

    @Override
    public int compareTo(DataFieldDescriptor o) {
        return ordinal - o.ordinal;
    }

    public DataFieldDescriptor mapParameters(Map<Type, Type> mappedParameters) {
        return new DataFieldDescriptor(ordinal, name, methodName, type.mapVariables(variable -> mappedParameters.getOrDefault(variable, type)));
    }

    @Override
    public String toString() {
        return name + " :: " + type;
    }

    public DataFieldDescriptor withType(Type type) {
        return new DataFieldDescriptor(ordinal, name, methodName, type);
    }
}
