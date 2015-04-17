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
public class NamedFieldMatchesNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        openBrace;
    private final List<AstNode>  fieldMatches;
    private final AstNode        closeBrace;
}
