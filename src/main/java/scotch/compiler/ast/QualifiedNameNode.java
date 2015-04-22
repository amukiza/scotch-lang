package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class QualifiedNameNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final List<AstNode>  dotNames;

    QualifiedNameNode(SourceLocation sourceLocation, List<AstNode> dotNames) {
        this.sourceLocation = sourceLocation;
        this.dotNames = ImmutableList.copyOf(dotNames);
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitQualifiedNameNode(this);
    }
}
