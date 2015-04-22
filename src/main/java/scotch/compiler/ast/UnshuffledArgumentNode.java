package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class UnshuffledArgumentNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final List<AstNode>  arguments;

    UnshuffledArgumentNode(SourceLocation sourceLocation, List<AstNode> arguments) {
        this.sourceLocation = sourceLocation;
        this.arguments = ImmutableList.copyOf(arguments);
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitUnshuffledArgumentNode(this);
    }
}
