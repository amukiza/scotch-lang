package scotch.data.double_;

import static java.util.Collections.emptyList;
import static scotch.symbol.type.Types.sum;

import java.util.List;
import scotch.symbol.DataType;
import scotch.symbol.TypeParameters;
import scotch.symbol.type.Type;

@SuppressWarnings("unused")
@DataType(memberName = "Double")
public class Double_ {

    public static Type TYPE = sum("scotch.data.double.Double");

    @TypeParameters
    public static List<Type> parameters() {
        return emptyList();
    }
}
