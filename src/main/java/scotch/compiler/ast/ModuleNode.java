package scotch.compiler.ast;

import static scotch.symbol.Symbol.symbol;

import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.parser.ModuleNameVisitor;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ModuleNode extends AstNode {

    @Getter
    private final SourceLocation sourceLocation;
    private final AstNode        moduleKeyword;
    @Getter
    private final AstNode        moduleName;
    private final AstNode        terminator;
    @Getter
    private final List<AstNode>  importScopes;

    ModuleNode(SourceLocation sourceLocation, AstNode moduleKeyword, AstNode moduleName, AstNode terminator, List<AstNode> importScopes) {
        this.sourceLocation = sourceLocation;
        this.moduleKeyword = moduleKeyword;
        this.moduleName = moduleName;
        this.terminator = terminator;
        this.importScopes = ImmutableList.copyOf(importScopes);
    }

    @Override
    public <T> T accept(AstNodeVisitor<T> visitor) {
        return visitor.visitModuleNode(this);
    }

    public Symbol getModuleSymbol() {
        return symbol(moduleName.accept(new ModuleNameVisitor()));
    }

    public ModuleNode merge(ModuleNode node) {
        return new ModuleNode(sourceLocation, moduleKeyword, moduleName, terminator, new ArrayList<AstNode>() {{
            addAll(importScopes);
            addAll(node.importScopes);
        }});
    }
}
