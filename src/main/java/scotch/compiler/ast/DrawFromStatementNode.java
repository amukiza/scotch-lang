package scotch.compiler.ast;

import static lombok.AccessLevel.PACKAGE;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class DrawFromStatementNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        patternArguments;
    private final AstNode        drawFrom;
    private final AstNode        expression;
    private final AstNode        terminal;
}
