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
public class InfixOperatorNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        side;
    private final AstNode        infix;

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitInfixOperatorNode(this);
    }
}
