package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ImportStatementsNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final List<AstNode>  statements;

    ImportStatementsNode(SourceLocation sourceLocation, List<AstNode> statements) {
        this.sourceLocation = sourceLocation;
        this.statements = ImmutableList.copyOf(statements);
    }
}
