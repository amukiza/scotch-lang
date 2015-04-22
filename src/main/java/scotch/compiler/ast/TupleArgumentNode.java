package scotch.compiler.ast;

import static lombok.AccessLevel.PACKAGE;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class TupleArgumentNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        openParen;
    private final List<AstNode>  fieldMatches;
    private final AstNode        closeParen;

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitTupleArgumentNode(this);
    }
}
