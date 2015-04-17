package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class DataTupleNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        name;
    private final List<AstNode>  fields;

    public DataTupleNode(SourceLocation sourceLocation, AstNode name, List<AstNode> fields) {
        this.sourceLocation = sourceLocation;
        this.name = name;
        this.fields = ImmutableList.copyOf(fields);
    }
}
