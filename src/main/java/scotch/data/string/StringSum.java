package scotch.data.string;

import static scotch.symbol.type.Types.sum;

import java.util.List;
import com.google.common.collect.ImmutableList;
import scotch.symbol.DataType;
import scotch.symbol.TypeParameters;
import scotch.symbol.type.Type;

@SuppressWarnings("unused")
@DataType(memberName = "String")
public class StringSum {

    public static final Type TYPE = sum("scotch.data.string.String");

    @TypeParameters
    public static List<Type> parameters() {
        return ImmutableList.of();
    }
}
