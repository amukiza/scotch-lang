package scotch.symbol.descriptor;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static scotch.symbol.Symbol.symbol;
import static scotch.symbol.descriptor.DataFieldDescriptor.field;
import static scotch.symbol.type.Types.t;
import static scotch.symbol.type.Types.var;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import scotch.symbol.type.Type;

public class DataTypeDescriptorTest {

    @Test
    public void shouldReifyConstructorFieldType() {
        DataConstructorDescriptor dataConstructor = DataConstructorDescriptor.builder(0, symbol("Maybe"), symbol("Just"), "Just")
            .addField(field(0, "_0", var("a")))
            .build();
        DataTypeDescriptor dataType = DataTypeDescriptor.builder(symbol("Maybe"))
            .addParameter(var("a"))
            .addConstructor(dataConstructor)
            .build();

        Map<Type, Type> mappedParameters = new HashMap<Type, Type>() {{
            put(var("a"), t(2));
        }};

        DataTypeDescriptor reifiedDataType = dataType.mapParameters(mappedParameters);

        assertThat(reifiedDataType.getParameters(), contains(t(2)));
        assertThat(reifiedDataType.getConstructor(symbol("Just")).get().getField("_0"), is(Optional.of(field(0, "_0", t(2)))));
    }
}
