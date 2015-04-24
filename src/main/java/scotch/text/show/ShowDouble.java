package scotch.text.show;

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
@TypeInstance(typeClass = "scotch.text.show.Show")
public class ShowDouble implements Show<Double> {

    private static final Callable<ShowDouble> INSTANCE = callable(ShowDouble::new);

    @InstanceGetter
    public static Callable<ShowDouble> instance() {
        return INSTANCE;
    }

    @TypeParameters
    public static List<Type> parameters() {
        return asList(Double_.TYPE);
    }

    private ShowDouble() {
        // intentionally empty
    }

    @Override
    public Callable<String> show(Callable<Double> operand) {
        return callable(() -> operand.call().toString());
    }
}
