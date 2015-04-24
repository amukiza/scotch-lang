package scotch.data.ord;

import static java.util.Arrays.asList;
import static scotch.runtime.RuntimeSupport.callable;

import java.util.List;
import scotch.data.double_.Double_;
import scotch.data.eq.Eq;
import scotch.runtime.Callable;
import scotch.symbol.InstanceGetter;
import scotch.symbol.TypeInstance;
import scotch.symbol.TypeParameters;
import scotch.symbol.type.Type;

@SuppressWarnings("unused")
@TypeInstance(typeClass = "scotch.data.ord.Ord")
public class OrdDouble implements Ord<Double> {

    private static final Callable<OrdDouble> INSTANCE = callable(OrdDouble::new);

    @InstanceGetter
    public static Callable<OrdDouble> instance() {
        return INSTANCE;
    }

    @TypeParameters
    public static List<Type> parameters() {
        return asList(Double_.TYPE);
    }

    private OrdDouble() {
        // intentionally empty
    }

    @Override
    public Callable<Boolean> lessThanEquals(Callable<Eq<Double>> eq, Callable<Double> left, Callable<Double> right) {
        return callable(() -> eq.call().eq(left, right).call() || left.call() < right.call());
    }
}
