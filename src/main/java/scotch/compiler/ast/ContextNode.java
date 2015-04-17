package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ContextNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        openParen;
    private final List<AstNode>  typeContexts;
    private final AstNode        closeParen;
    private final AstNode        contextArrow;

    public ContextNode(SourceLocation sourceLocation, AstNode openParen, List<AstNode> typeContexts, AstNode closeParen, AstNode contextArrow) {
        this.sourceLocation = sourceLocation;
        this.openParen = openParen;
        this.typeContexts = ImmutableList.copyOf(typeContexts);
        this.closeParen = closeParen;
        this.contextArrow = contextArrow;
    }
}
