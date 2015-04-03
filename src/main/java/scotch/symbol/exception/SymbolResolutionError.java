package scotch.symbol.exception;

public class SymbolResolutionError extends Error {

    public SymbolResolutionError(String message) {
        super(message);
    }

    public SymbolResolutionError(Throwable cause) {
        super(cause);
    }
}
