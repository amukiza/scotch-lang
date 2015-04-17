package scotch.compiler.ast;

import static lombok.AccessLevel.PACKAGE;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.scanner.Token;
import scotch.compiler.text.SourceLocation;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString
public class TerminalNode extends AstNode {

    private final Token token;

    @Override
    public SourceLocation getSourceLocation() {
        return token.getSourceLocation();
    }
}
