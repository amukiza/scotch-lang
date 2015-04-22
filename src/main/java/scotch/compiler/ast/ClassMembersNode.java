package scotch.compiler.ast;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ClassMembersNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        openCurly;
    private final List<AstNode>  classMembers;
    private final AstNode        closeCurly;

    public ClassMembersNode(SourceLocation sourceLocation, AstNode openCurly, List<AstNode> classMembers, AstNode closeCurly) {
        this.sourceLocation = sourceLocation;
        this.openCurly = openCurly;
        this.classMembers = classMembers;
        this.closeCurly = closeCurly;
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitClassMembersNode(this);
    }
}
