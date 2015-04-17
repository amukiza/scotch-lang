package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class InitializerNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        constructor;
    private final AstNode        openCurly;
    private final List<AstNode>  initializerFields;
    private final AstNode        closeCurly;

    InitializerNode(SourceLocation sourceLocation, AstNode constructor, AstNode openCurly, List<AstNode> initializerFields, AstNode closeCurly) {
        this.sourceLocation = sourceLocation;
        this.constructor = constructor;
        this.openCurly = openCurly;
        this.initializerFields = ImmutableList.copyOf(initializerFields);
        this.closeCurly = closeCurly;
    }
}
