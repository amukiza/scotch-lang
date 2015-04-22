package scotch.compiler.ast;

import static java.util.Arrays.asList;
import static lombok.AccessLevel.PACKAGE;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString
public class ApplyNode extends AstNode {

    private final AstNode function;
    private final AstNode argument;

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitApplyNode(this);
    }

    @Override
    public SourceLocation getSourceLocation() {
        return SourceLocation.extent(asList(function.getSourceLocation(), argument.getSourceLocation()));
    }
}
