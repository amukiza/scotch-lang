package scotch.compiler.ast;

import static lombok.AccessLevel.PACKAGE;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class OperatorDefinitionNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        fixity;
    private final AstNode        precedence;
    private final List<AstNode>  operators;
}
