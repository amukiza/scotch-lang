package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class SumTypeNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        name;
    private final List<AstNode>  parameters;

    SumTypeNode(SourceLocation sourceLocation, AstNode name, List<AstNode> parameters) {
        this.sourceLocation = sourceLocation;
        this.name = name;
        this.parameters = ImmutableList.copyOf(parameters);
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitSumTypeNode(this);
    }
}
