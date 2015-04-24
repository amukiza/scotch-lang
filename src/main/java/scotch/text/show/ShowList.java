package scotch.text.show;

import static java.util.Arrays.asList;
import static scotch.runtime.RuntimeSupport.callable;
import static scotch.symbol.type.Types.sum;
import static scotch.symbol.type.Types.var;

import java.util.List;
import scotch.data.list.ConsList;
import scotch.runtime.Callable;
import scotch.symbol.InstanceGetter;
import scotch.symbol.TypeInstance;
import scotch.symbol.TypeParameters;
import scotch.symbol.type.Type;

// TODO should require show-able elements within list
@SuppressWarnings("unused")
@TypeInstance(typeClass = "scotch.text.show.Show")
public class ShowList implements Show<ConsList> {

    private static final Callable<ShowList> INSTANCE = callable(ShowList::new);

    @InstanceGetter
    public static Callable<ShowList> instance() {
        return INSTANCE;
    }

    @TypeParameters
    public static List<Type> parameters() {
        return asList(sum("scotch.data.list.[]", asList(var("a"))));
    }

    private ShowList() {
        // intentionally empty
    }

    @Override
    public Callable<String> show(Callable<ConsList> operand) {
        return callable(() -> operand.call().toString());
    }
}
