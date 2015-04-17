package scotch.compiler.ast;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.text.SourceLocation;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class PatternArgumentsNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final List<AstNode>  patternArguments;

    PatternArgumentsNode(SourceLocation sourceLocation, List<AstNode> patternArguments) {
        this.sourceLocation = sourceLocation;
        this.patternArguments = ImmutableList.copyOf(patternArguments);
    }
}
