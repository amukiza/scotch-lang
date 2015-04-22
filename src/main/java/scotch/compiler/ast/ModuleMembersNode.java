package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ModuleMembersNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final List<AstNode>  moduleMembers;

    public ModuleMembersNode(SourceLocation sourceLocation, List<AstNode> moduleMembers) {
        this.sourceLocation = sourceLocation;
        this.moduleMembers = ImmutableList.copyOf(moduleMembers);
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitModuleMembersNode(this);
    }
}
