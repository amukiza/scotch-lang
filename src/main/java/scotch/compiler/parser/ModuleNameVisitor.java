package scotch.compiler.parser;

import static java.util.stream.Collectors.joining;

import scotch.compiler.ast.AstNode;
import scotch.compiler.ast.BaseNodeVisitor;
import scotch.compiler.ast.QualifiedNameNode;
import scotch.compiler.ast.TerminalNode;

public class ModuleNameVisitor extends BaseNodeVisitor<String> {

    @Override
    public String visitQualifiedNameNode(QualifiedNameNode node) {
        return node.getDotNames().stream()
            .map(n -> n.accept(this))
            .collect(joining(""));
    }

    @Override
    public String visitTerminalNode(TerminalNode node) {
        return node.getToken().getValueAs(String.class);
    }

    @Override
    protected String visitDefault(AstNode node) {
        throw new UnsupportedOperationException();
    }
}
