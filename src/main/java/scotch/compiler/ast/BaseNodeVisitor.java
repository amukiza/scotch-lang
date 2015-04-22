package scotch.compiler.ast;

public abstract class BaseNodeVisitor<T> implements AstNodeVisitor<T> {

    @Override
    public T visitApplyNode(ApplyNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitBoolNode(BoolNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitCaptureArgumentNode(CaptureArgumentNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitCharNode(CharNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitClassArgumentNode(ClassArgumentNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitClassDefinitionNode(ClassDefinitionNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitClassMembersNode(ClassMembersNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitConditionalNode(ConditionalNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitConstantArgumentNode(ConstantArgumentNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitConstructorArgumentNode(ConstructorArgumentNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitConstructorTypeNode(ConstructorTypeNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitContextNode(ContextNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitContextTypeNode(ContextTypeNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDataConstantNode(DataConstantNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDataRecordFieldNode(DataRecordFieldNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDataRecordNode(DataRecordNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDataTupleNode(DataTupleNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDataTupleFieldNode(DataTupleFieldNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDataTypeNode(DataTypeNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDoStatementNode(DoStatementNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDoStatementsNode(DoStatementsNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDoubleNode(DoubleNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitDrawFromStatementNode(DrawFromStatementNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitEmptyNamedFieldMatchesNode(EmptyNamedFieldMatchesNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitExpressionNode(ExpressionNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitFunctionTypeNode(FunctionTypeNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitIgnoreArgumentNode(IgnoreArgumentNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitImportNode(ImportNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitImportStatementNode(ImportStatementNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitImportStatementsNode(ImportStatementsNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitInfixOperatorNode(InfixOperatorNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitInitializerFieldNode(InitializerFieldNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitInitializerNode(InitializerNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitInstanceDefinitionNode(InstanceDefinitionNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitInstanceMembersNode(InstanceMembersNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitIntegerNode(IntegerNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitListLiteralNode(ListLiteralNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitListTypeNode(ListTypeNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitLiteralArgumentNode(LiteralArgumentNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitLiteralNode(LiteralNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitModuleMemberNode(ModuleMemberNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitModuleMembersNode(ModuleMembersNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitModuleNode(ModuleNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitModulesNode(ModulesNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitNameReferenceNode(NameReferenceNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitNamedFieldImplicitCaptureNode(NamedFieldImplicitCaptureNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitNamedFieldMatchNode(NamedFieldMatchNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitNamedFieldMatchesNode(NamedFieldMatchesNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitOperatorDefinitionNode(OperatorDefinitionNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitOperatorFixityNode(OperatorFixityNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitOperatorNameNode(OperatorNameNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitParenthesizedNode(ParenthesizedNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitPatternArgumentsNode(PatternArgumentsNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitPatternLiteralNode(PatternLiteralNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitPatternNameNode(PatternNameNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitPatternNode(PatternNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitPatternSignatureNode(PatternSignatureNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitPrefixOperatorNode(PrefixOperatorNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitPrimaryExpressionNode(PrimaryExpressionNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitQualifiedNameNode(QualifiedNameNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitStringNode(StringNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitSumTypeNode(SumTypeNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitTerminalNode(TerminalNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitTupleArgumentNode(TupleArgumentNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitTupleFieldMatchNode(TupleFieldMatchNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitTupleLiteralNode(TupleLiteralNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitTupleTypeFieldNode(TupleTypeFieldNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitTupleTypeNode(TupleTypeNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitTypeContextNode(TypeContextNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitTypeSignatureNode(TypeSignatureNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitUnshuffledArgumentNode(UnshuffledArgumentNode node) {
        return visitDefault(node);
    }

    @Override
    public T visitVariableTypeNode(VariableTypeNode node) {
        return visitDefault(node);
    }

    protected abstract T visitDefault(AstNode node);
}
