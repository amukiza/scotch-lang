package scotch.runtime;

public class AccessorSupport {

    public static Callable access(Callable target, String fieldName) throws ReflectiveOperationException {
        return (Callable) target.call().getClass().getMethod(fieldName).invoke(target.call());
    }
}
