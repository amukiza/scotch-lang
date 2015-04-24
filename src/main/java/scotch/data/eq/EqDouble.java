package scotch.data.eq;

import static java.util.Arrays.asList;
import static scotch.runtime.RuntimeSupport.callable;

import java.util.List;
import scotch.data.double_.Double_;
import scotch.runtime.Callable;
import scotch.symbol.InstanceGetter;
import scotch.symbol.TypeInstance;
import scotch.symbol.TypeParameters;
import scotch.symbol.type.Type;

@SuppressWarnings("unused")
@TypeInstance(typeClass = "scotch.data.eq.Eq")
public class EqDouble implements Eq<Double> {

    private static final Callable<EqDouble> INSTANCE = callable(EqDouble::new);

    @InstanceGetter
    public static Callable<EqDouble> instance() {
        return INSTANCE;
    }

    @TypeParameters
    public static List<Type> parameters() {
        return asList(Double_.TYPE);
    }

    private EqDouble() {
        // intentionally empty
    }

    @Override
    public Callable<Boolean> eq(Callable<Double> left, Callable<Double> right) {
        return callable(() -> left.call().equals(right.call()));
    }
}
