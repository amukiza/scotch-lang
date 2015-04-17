package scotch.compiler.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ListLiteralNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        openSquare;
    private final List<AstNode>  elements;
    private final AstNode        closeSquare;

    ListLiteralNode(SourceLocation sourceLocation, AstNode openSquare, List<AstNode> elements, AstNode closeSquare) {
        this.sourceLocation = sourceLocation;
        this.openSquare = openSquare;
        this.elements = elements;
        this.closeSquare = closeSquare;
    }
}
