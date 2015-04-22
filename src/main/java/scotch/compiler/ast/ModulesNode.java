package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ModulesNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    @Getter
    private final List<AstNode> modules;
    private final List<AstNode> terminators;
    private final AstNode       eof;

    ModulesNode(SourceLocation sourceLocation, List<AstNode> modules, List<AstNode> terminators, AstNode eof) {
        this.sourceLocation = sourceLocation;
        this.modules = ImmutableList.copyOf(modules);
        this.terminators = ImmutableList.copyOf(terminators);
        this.eof = eof;
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitModulesNode(this);
    }
}
