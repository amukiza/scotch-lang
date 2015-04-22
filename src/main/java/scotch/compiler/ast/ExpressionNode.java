package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ExpressionNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final List<AstNode> primaryExpressions;

    public ExpressionNode(SourceLocation sourceLocation, List<AstNode> primaryExpressions) {
        this.sourceLocation = sourceLocation;
        this.primaryExpressions = ImmutableList.copyOf(primaryExpressions);
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitExpressionNode(this);
    }
}
