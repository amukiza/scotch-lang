package scotch.runtime;

import static scotch.runtime.RuntimeSupport.callable;

public class AccessorSupport {

    public static Callable access(Callable target, String fieldName) {
        return callable(() -> {
            try {
                return (Callable) target.call().getClass().getMethod(fieldName).invoke(target.call());
            } catch (ReflectiveOperationException exception) {
                throw new RuntimeException(exception);
            }
        });
    }
}
