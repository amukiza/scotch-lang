package scotch.runtime;

/**
 * A thunk representing a suspended state of computation (i.e. lazy evaluation).
 *
 * <p>Read <a href="http://en.wikipedia.org/wiki/Thunk">here</a> for more information on thunks.</p>
 *
 * @param <A> The type of the value to be returned from the Thunk.
 */
public abstract class Thunk<A> implements Callable<A> {

    /**
     * The value after it has been evaluated to normal form.
     */
    private volatile A value;

    /**
     * Evaluates the thunk down to normal form.
     *
     * <p><a href="http://stackoverflow.com/questions/6872898/haskell-what-is-weak-head-normal-form">Read here</a> for
     * explanation of what normal form is.</p>
     *
     * @return The normalized value.
     */
    @SuppressWarnings("unchecked")
    @Override
    public A call() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = evaluate();
                    while (value instanceof Callable) {
                        if (value instanceof Applicable) {
                            break;
                        }
                        value = ((Callable<A>) value).call();
                    }
                }
            }
        }
        return value;
    }

    /**
     * Evaluates the value.
     *
     * @return The evaluated value.
     */
    protected abstract A evaluate();
}
