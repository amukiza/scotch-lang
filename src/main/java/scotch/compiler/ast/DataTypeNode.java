package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class DataTypeNode extends AstNode {

    @Getter
    private final SourceLocation         sourceLocation;
    private final AstNode                data;
    private final AstNode                identifier;
    private final ImmutableList<AstNode> parameters;
    private final AstNode                is;
    private final List<AstNode>          members;

    DataTypeNode(SourceLocation sourceLocation, AstNode data, AstNode identifier, List<AstNode> parameters, AstNode is, List<AstNode> members) {
        this.sourceLocation = sourceLocation;
        this.data = data;
        this.identifier = identifier;
        this.parameters = ImmutableList.copyOf(parameters);
        this.is = is;
        this.members = ImmutableList.copyOf(members);
    }
}
