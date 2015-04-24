package scotch.data.bool;

import static java.util.Collections.emptyList;
import static scotch.runtime.RuntimeSupport.applicable;
import static scotch.runtime.RuntimeSupport.callable;
import static scotch.symbol.Value.Fixity.LEFT_INFIX;
import static scotch.symbol.type.Types.fn;
import static scotch.symbol.type.Types.sum;

import java.util.List;
import scotch.runtime.Applicable;
import scotch.symbol.DataType;
import scotch.symbol.TypeParameters;
import scotch.symbol.Value;
import scotch.symbol.ValueType;
import scotch.symbol.type.Type;

@SuppressWarnings("unused")
@DataType(memberName = "Bool")
public final class Bool {

    public static Type TYPE = sum("scotch.data.bool.Bool");

    @Value(memberName = "&&", fixity = LEFT_INFIX, precedence = 4)
    public static Applicable<Boolean, Applicable<Boolean, Boolean>> and() {
        return applicable(left -> applicable(right -> callable(() -> left.call() && right.call())));
    }

    @ValueType(forMember = "&&")
    public static Type and$type() {
        return fn(TYPE, fn(TYPE, TYPE));
    }

    @Value(memberName = "not")
    public static Applicable<Boolean, Boolean> not() {
        return applicable(operand -> callable(() -> !operand.call()));
    }

    @ValueType(forMember = "not")
    public static Type not$type() {
        return fn(TYPE, TYPE);
    }

    @Value(memberName = "||", fixity = LEFT_INFIX, precedence = 3)
    public static Applicable<Boolean, Applicable<Boolean, Boolean>> or() {
        return applicable(left -> applicable(right -> callable(() -> left.call() || right.call())));
    }

    @ValueType(forMember = "||")
    public static Type or$type() {
        return fn(TYPE, fn(TYPE, TYPE));
    }

    @TypeParameters
    public static List<Type> parameters() {
        return emptyList();
    }

    private Bool() {
        // intentionally empty
    }
}
