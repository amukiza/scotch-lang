package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class DataRecordNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        name;
    private final AstNode        openCurly;
    private final List<AstNode>  fieldsAndCommas;
    private final AstNode        closeCurly;

    public DataRecordNode(SourceLocation sourceLocation, AstNode name, AstNode openCurly, List<AstNode> fieldsAndCommas, AstNode closeCurly) {
        this.sourceLocation = sourceLocation;
        this.name = name;
        this.openCurly = openCurly;
        this.fieldsAndCommas = ImmutableList.copyOf(fieldsAndCommas);
        this.closeCurly = closeCurly;
    }
}
