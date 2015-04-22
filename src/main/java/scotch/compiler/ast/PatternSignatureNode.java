package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class PatternSignatureNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final List<AstNode>  patternNames;
    private final AstNode        doubleColon;
    private final AstNode        typeSignature;

    PatternSignatureNode(SourceLocation sourceLocation, List<AstNode> patternNames, AstNode doubleColon, AstNode typeSignature) {
        this.sourceLocation = sourceLocation;
        this.patternNames = ImmutableList.copyOf(patternNames);
        this.doubleColon = doubleColon;
        this.typeSignature = typeSignature;
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitPatternSignatureNode(this);
    }
}
