package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class InstanceDefinitionNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        instanceKeyword;
    private final AstNode        instanceName;
    private final List<AstNode>  instanceArguments;
    private final AstNode        where;
    private final AstNode        instanceMembers;

    public InstanceDefinitionNode(SourceLocation sourceLocation, AstNode instanceKeyword, AstNode instanceName, List<AstNode> instanceArguments, AstNode where, AstNode instanceMembers) {
        this.sourceLocation = sourceLocation;
        this.instanceKeyword = instanceKeyword;
        this.instanceName = instanceName;
        this.instanceArguments = ImmutableList.copyOf(instanceArguments);
        this.where = where;
        this.instanceMembers = instanceMembers;
    }
}
