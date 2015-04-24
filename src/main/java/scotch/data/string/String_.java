package scotch.data.string;

import static scotch.runtime.RuntimeSupport.applicable;
import static scotch.runtime.RuntimeSupport.callable;
import static scotch.symbol.Value.Fixity.LEFT_INFIX;
import static scotch.symbol.type.Types.fn;
import static scotch.symbol.type.Types.sum;

import java.util.List;
import com.google.common.collect.ImmutableList;
import scotch.runtime.Applicable;
import scotch.symbol.DataType;
import scotch.symbol.TypeParameters;
import scotch.symbol.Value;
import scotch.symbol.ValueType;
import scotch.symbol.type.Type;

@SuppressWarnings("unused")
@DataType(memberName = "String")
public class String_ {

    public static final Type TYPE = sum("scotch.data.string.String");

    @TypeParameters
    public static List<Type> parameters() {
        return ImmutableList.of();
    }

    @Value(memberName = "++", fixity = LEFT_INFIX, precedence = 7)
    public static Applicable<String, Applicable<String, String>> concatenate() {
        return applicable(left -> applicable(right -> callable(() -> left.call() + right.call())));
    }

    @ValueType(forMember = "++")
    public static Type concatenate$type() {
        return fn(TYPE, fn(TYPE, TYPE));
    }
}
