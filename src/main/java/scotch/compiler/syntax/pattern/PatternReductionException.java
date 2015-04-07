package scotch.compiler.syntax.pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
public class PatternReductionException extends RuntimeException {

    @Getter private final String         message;
    @Getter private final SourceLocation sourceLocation;

    public PatternReductionException(String message, SourceLocation sourceLocation) {
        super(message + " " + sourceLocation.prettyPrint());
        this.message = message;
        this.sourceLocation = sourceLocation;
    }
}
