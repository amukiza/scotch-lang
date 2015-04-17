package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class InstanceMembersNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        openCurly;
    private final List<AstNode>  instanceMembers;
    private final AstNode        closeCurly;

    public InstanceMembersNode(SourceLocation sourceLocation, AstNode openCurly, List<AstNode> instanceMembers, AstNode closeCurly) {
        this.sourceLocation = sourceLocation;
        this.openCurly = openCurly;
        this.instanceMembers = ImmutableList.copyOf(instanceMembers);
        this.closeCurly = closeCurly;
    }
}
