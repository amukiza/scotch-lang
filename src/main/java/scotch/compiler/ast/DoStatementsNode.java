package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class DoStatementsNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        doKeyword;
    private final AstNode        openCurly;
    private final List<AstNode>  statements;
    private final AstNode        closeCurly;

    public DoStatementsNode(SourceLocation sourceLocation, AstNode doKeyword, AstNode openCurly, List<AstNode> statements, AstNode closeCurly) {
        this.sourceLocation = sourceLocation;
        this.doKeyword = doKeyword;
        this.openCurly = openCurly;
        this.statements = ImmutableList.copyOf(statements);
        this.closeCurly = closeCurly;
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitDoStatementsNode(this);
    }
}
