package scotch.compiler.ast;

public interface AstNodeVisitor<T> {

    T visitApplyNode(ApplyNode node);

    T visitBoolNode(BoolNode node);

    T visitCaptureArgumentNode(CaptureArgumentNode node);

    T visitCharNode(CharNode node);

    T visitClassArgumentNode(ClassArgumentNode node);

    T visitClassDefinitionNode(ClassDefinitionNode node);

    T visitClassMembersNode(ClassMembersNode node);

    T visitConditionalNode(ConditionalNode node);

    T visitConstantArgumentNode(ConstantArgumentNode node);

    T visitConstructorArgumentNode(ConstructorArgumentNode node);

    T visitConstructorTypeNode(ConstructorTypeNode node);

    T visitContextNode(ContextNode node);

    T visitContextTypeNode(ContextTypeNode node);

    T visitDataConstantNode(DataConstantNode node);

    T visitDataRecordFieldNode(DataRecordFieldNode node);

    T visitDataRecordNode(DataRecordNode node);

    T visitDataTupleNode(DataTupleNode node);

    T visitDataTupleFieldNode(DataTupleFieldNode node);

    T visitDataTypeNode(DataTypeNode node);

    T visitDoStatementNode(DoStatementNode node);

    T visitDoStatementsNode(DoStatementsNode node);

    T visitDoubleNode(DoubleNode node);

    T visitDrawFromStatementNode(DrawFromStatementNode node);

    T visitEmptyNamedFieldMatchesNode(EmptyNamedFieldMatchesNode node);

    T visitExpressionNode(ExpressionNode node);

    T visitFunctionTypeNode(FunctionTypeNode node);

    T visitIgnoreArgumentNode(IgnoreArgumentNode node);

    T visitImportNode(ImportNode node);

    T visitImportStatementNode(ImportStatementNode node);

    T visitImportStatementsNode(ImportStatementsNode node);

    T visitInfixOperatorNode(InfixOperatorNode node);

    T visitInitializerFieldNode(InitializerFieldNode node);

    T visitInitializerNode(InitializerNode node);

    T visitInstanceDefinitionNode(InstanceDefinitionNode node);

    T visitInstanceMembersNode(InstanceMembersNode node);

    T visitIntegerNode(IntegerNode node);

    T visitListLiteralNode(ListLiteralNode node);

    T visitListTypeNode(ListTypeNode node);

    T visitLiteralArgumentNode(LiteralArgumentNode node);

    T visitLiteralNode(LiteralNode node);

    T visitModuleMemberNode(ModuleMemberNode node);

    T visitModuleMembersNode(ModuleMembersNode node);

    T visitModuleNode(ModuleNode node);

    T visitModulesNode(ModulesNode node);

    T visitNameReferenceNode(NameReferenceNode node);

    T visitNamedFieldImplicitCaptureNode(NamedFieldImplicitCaptureNode node);

    T visitNamedFieldMatchNode(NamedFieldMatchNode node);

    T visitNamedFieldMatchesNode(NamedFieldMatchesNode node);

    T visitOperatorDefinitionNode(OperatorDefinitionNode node);

    T visitOperatorFixityNode(OperatorFixityNode node);

    T visitOperatorNameNode(OperatorNameNode node);

    T visitParenthesizedNode(ParenthesizedNode node);

    T visitPatternArgumentsNode(PatternArgumentsNode node);

    T visitPatternLiteralNode(PatternLiteralNode node);

    T visitPatternNameNode(PatternNameNode node);

    T visitPatternNode(PatternNode node);

    T visitPatternSignatureNode(PatternSignatureNode node);

    T visitPrefixOperatorNode(PrefixOperatorNode node);

    T visitPrimaryExpressionNode(PrimaryExpressionNode node);

    T visitQualifiedNameNode(QualifiedNameNode node);

    T visitStringNode(StringNode node);

    T visitSumTypeNode(SumTypeNode node);

    T visitTerminalNode(TerminalNode node);

    T visitTupleArgumentNode(TupleArgumentNode node);

    T visitTupleFieldMatchNode(TupleFieldMatchNode node);

    T visitTupleLiteralNode(TupleLiteralNode node);

    T visitTupleTypeFieldNode(TupleTypeFieldNode node);

    T visitTupleTypeNode(TupleTypeNode node);

    T visitTypeContextNode(TypeContextNode node);

    T visitTypeSignatureNode(TypeSignatureNode node);

    T visitUnshuffledArgumentNode(UnshuffledArgumentNode node);

    T visitVariableTypeNode(VariableTypeNode node);
}

