package scotch.compiler.ast;

import java.util.List;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ClassDefinitionNode extends AstNode {

    @Getter
    private final SourceLocation    sourceLocation;
    private final AstNode           classKeyword;
    private final Optional<AstNode> context;
    private final AstNode           className;
    private final List<AstNode>     arguments;
    private final AstNode           where;
    private final AstNode           classMembers;

    public ClassDefinitionNode(SourceLocation sourceLocation, AstNode classKeyword, Optional<AstNode> context, AstNode className, List<AstNode> arguments, AstNode where, AstNode classMembers) {
        this.sourceLocation = sourceLocation;
        this.classKeyword = classKeyword;
        this.context = context;
        this.className = className;
        this.arguments = ImmutableList.copyOf(arguments);
        this.where = where;
        this.classMembers = classMembers;
    }
}
