package scotch.compiler.ast;

import java.util.List;
import java.util.Optional;
import scotch.compiler.scanner.Token;
import scotch.compiler.text.SourceLocation;

public final class AstNodes {

    public static AstNode bool(SourceLocation sourceLocation, AstNode value) {
        return new BoolNode(sourceLocation, value);
    }

    public static AstNode captureArgument(SourceLocation sourceLocation, AstNode identifier) {
        return new CaptureArgumentNode(sourceLocation, identifier);
    }

    public static AstNode char_(SourceLocation sourceLocation, AstNode value) {
        return new CharNode(sourceLocation, value);
    }

    public static AstNode classArgument(SourceLocation sourceLocation, AstNode name) {
        return new ClassArgumentNode(sourceLocation, name);
    }

    public static AstNode classDefinition(SourceLocation sourceLocation, AstNode classKeyword, Optional<AstNode> context, AstNode className, List<AstNode> arguments, AstNode where, AstNode classMembers) {
        return new ClassDefinitionNode(sourceLocation, classKeyword, context, className, arguments, where, classMembers);
    }

    public static AstNode classMembers(SourceLocation sourceLocation, AstNode openCurly, List<AstNode> classMembers, AstNode closeCurly) {
        return new ClassMembersNode(sourceLocation, openCurly, classMembers, closeCurly);
    }

    public static AstNode conditional(SourceLocation sourceLocation, AstNode ifKeyword, AstNode condition, AstNode thenKeyword, AstNode trueCase, AstNode elseKeyword, AstNode falseCase) {
        return new ConditionalNode(sourceLocation, ifKeyword, condition, thenKeyword, trueCase, elseKeyword, falseCase);
    }

    public static AstNode constantArgument(SourceLocation sourceLocation, AstNode constructor) {
        return new ConstantArgumentNode(sourceLocation, constructor);
    }

    public static AstNode constructorArgument(SourceLocation sourceLocation, AstNode constructor, AstNode fieldMatches) {
        return new ConstructorArgumentNode(sourceLocation, constructor, fieldMatches);
    }

    public static AstNode constructorType(SourceLocation sourceLocation, AstNode name, List<AstNode> parameters) {
        return new ConstructorTypeNode(sourceLocation, name, parameters);
    }

    public static AstNode context(SourceLocation sourceLocation, AstNode openParen, List<AstNode> typeContexts, AstNode closeParen, AstNode contextArrow) {
        return new ContextNode(sourceLocation, openParen, typeContexts, closeParen, contextArrow);
    }

    public static AstNode contextType(SourceLocation sourceLocation, AstNode context, AstNode type) {
        return new ContextTypeNode(sourceLocation, context, type);
    }

    public static AstNode dataConstant(SourceLocation sourceLocation, AstNode name) {
        return new DataConstantNode(sourceLocation, name);
    }

    public static AstNode dataRecord(SourceLocation sourceLocation, AstNode name, AstNode openCurly, List<AstNode> fieldsAndCommas, AstNode closeCurly) {
        return new DataRecordNode(sourceLocation, name, openCurly, fieldsAndCommas, closeCurly);
    }

    public static AstNode dataRecordField(SourceLocation sourceLocation, AstNode fieldName, AstNode hasType, AstNode type) {
        return new DataRecordFieldNode(sourceLocation, fieldName, hasType, type);
    }

    public static AstNode dataTuple(SourceLocation sourceLocation, AstNode name, List<AstNode> fields) {
        return new DataTupleNode(sourceLocation, name, fields);
    }

    public static AstNode dataTupleField(SourceLocation sourceLocation, AstNode type) {
        return new DataTupleFieldNode(sourceLocation, type);
    }

    public static AstNode dataType(SourceLocation sourceLocation, AstNode data, AstNode identifier, List<AstNode> parameters, AstNode is, List<AstNode> members) {
        return new DataTypeNode(sourceLocation, data, identifier, parameters, is, members);
    }

    public static AstNode doStatement(SourceLocation sourceLocation, AstNode expression, AstNode semicolon) {
        return new DoStatementNode(sourceLocation, expression, semicolon);
    }

    public static AstNode doStatements(SourceLocation sourceLocation, AstNode doKeyword, AstNode openCurly, List<AstNode> statements, AstNode closeCurly) {
        return new DoStatementsNode(sourceLocation, doKeyword, openCurly, statements, closeCurly);
    }

    public static AstNode double_(SourceLocation sourceLocation, AstNode value) {
        return new DoubleNode(sourceLocation, value);
    }

    public static AstNode drawFromStatement(SourceLocation sourceLocation, AstNode patternArguments, AstNode drawFrom, AstNode expression, AstNode terminal) {
        return new DrawFromStatementNode(sourceLocation, patternArguments, drawFrom, expression, terminal);
    }

    public static AstNode emptyNamedFields(SourceLocation sourceLocation, AstNode openBrace, AstNode closeBrace) {
        return new EmptyNamedFieldMatchesNode(sourceLocation, openBrace, closeBrace);
    }

    public static AstNode expression(SourceLocation sourceLocation, List<AstNode> primaryExpressions) {
        return new ExpressionNode(sourceLocation, primaryExpressions);
    }

    public static AstNode functionType(SourceLocation sourceLocation, AstNode argument, AstNode arrow, AstNode result) {
        return new FunctionTypeNode(sourceLocation, argument, arrow, result);
    }

    public static AstNode ignoreArgument(SourceLocation sourceLocation, AstNode underscore) {
        return new IgnoreArgumentNode(sourceLocation, underscore);
    }

    public static AstNode importScope(SourceLocation sourceLocation, AstNode importStatements, AstNode moduleMembers) {
        return new ImportScopeNode(sourceLocation, importStatements, moduleMembers);
    }

    public static AstNode importStatement(SourceLocation sourceLocation, AstNode import_, AstNode terminator) {
        return new ImportStatementNode(sourceLocation, import_, terminator);
    }

    public static AstNode importStatements(SourceLocation sourceLocation, List<AstNode> statements) {
        return new ImportStatementsNode(sourceLocation, statements);
    }

    public static AstNode import_(SourceLocation sourceLocation, AstNode importKeyword, AstNode qualifiedName) {
        return new ImportNode(sourceLocation, importKeyword, qualifiedName);
    }

    public static AstNode infixOperator(SourceLocation sourceLocation, AstNode side, AstNode infix) {
        return new InfixOperatorNode(sourceLocation, side, infix);
    }

    public static AstNode initializer(SourceLocation sourceLocation, AstNode constructor, AstNode openCurly, List<AstNode> initializerFields, AstNode closeCurly) {
        return new InitializerNode(sourceLocation, constructor, openCurly, initializerFields, closeCurly);
    }

    public static AstNode initializerField(SourceLocation sourceLocation, AstNode fieldName, AstNode is, AstNode value) {
        return new InitializerFieldNode(sourceLocation, fieldName, is, value);
    }

    public static AstNode instanceDefinition(SourceLocation sourceLocation, AstNode instanceKeyword, AstNode instanceName, List<AstNode> instanceArguments, AstNode where, AstNode instanceMembers) {
        return new InstanceDefinitionNode(sourceLocation, instanceKeyword, instanceName, instanceArguments, where, instanceMembers);
    }

    public static AstNode instanceMembers(SourceLocation sourceLocation, AstNode openCurly, List<AstNode> instanceMembers, AstNode closeCurly) {
        return new InstanceMembersNode(sourceLocation, openCurly, instanceMembers, closeCurly);
    }

    public static AstNode integer(SourceLocation sourceLocation, AstNode value) {
        return new IntegerNode(sourceLocation, value);
    }

    public static AstNode listLiteral(SourceLocation sourceLocation, AstNode openSquare, List<AstNode> elements, AstNode closeSquare) {
        return new ListLiteralNode(sourceLocation, openSquare, elements, closeSquare);
    }

    public static AstNode listType(SourceLocation sourceLocation, AstNode openSquare, AstNode type, AstNode closeSquare) {
        return new ListTypeNode(sourceLocation, openSquare, type, closeSquare);
    }

    public static AstNode literal(SourceLocation sourceLocation, AstNode value) {
        return new LiteralNode(sourceLocation, value);
    }

    public static AstNode literalArgument(SourceLocation sourceLocation, AstNode literal) {
        return new LiteralArgumentNode(sourceLocation, literal);
    }

    public static AstNode module(SourceLocation sourceLocation, AstNode module, AstNode moduleName, AstNode terminator, AstNode importScope) {
        return new ModuleNode(sourceLocation, module, moduleName, terminator, importScope);
    }

    public static AstNode moduleMember(SourceLocation sourceLocation, AstNode member, AstNode terminator) {
        return new ModuleMemberNode(sourceLocation, member, terminator);
    }

    public static AstNode moduleMembers(SourceLocation sourceLocation, List<AstNode> moduleMembers) {
        return new ModuleMembersNode(sourceLocation, moduleMembers);
    }

    public static AstNode modules(SourceLocation sourceLocation, List<AstNode> modules, List<AstNode> terminators, AstNode eof) {
        return new ModulesNode(sourceLocation, modules, terminators, eof);
    }

    public static AstNode namedFieldImplicitCapture(SourceLocation sourceLocation, AstNode fieldName) {
        return new NamedFieldImplicitCaptureNode(sourceLocation, fieldName);
    }

    public static AstNode namedFieldMatch(SourceLocation sourceLocation, AstNode name, AstNode equals, AstNode patternArgument) {
        return new NamedFieldMatchNode(sourceLocation, name, equals, patternArgument);
    }

    public static AstNode namedFieldMatches(SourceLocation sourceLocation, AstNode openBrace, List<AstNode> fieldMatches, AstNode closeBrace) {
        return new NamedFieldMatchesNode(sourceLocation, openBrace, fieldMatches, closeBrace);
    }

    public static AstNode operator(SourceLocation sourceLocation, AstNode operator) {
        return new OperatorNameNode(sourceLocation, operator);
    }

    public static AstNode operatorDefinition(SourceLocation sourceLocation, AstNode fixity, AstNode precedence, List<AstNode> operators) {
        return new OperatorDefinitionNode(sourceLocation, fixity, precedence, operators);
    }

    public static AstNode operatorFixity(SourceLocation sourceLocation, AstNode fixity) {
        return new OperatorFixityNode(sourceLocation, fixity);
    }

    public static AstNode parenthesized(SourceLocation sourceLocation, AstNode openParen, AstNode argument, AstNode closeParen) {
        return new ParenthesizedNode(sourceLocation, openParen, argument, closeParen);
    }

    public static AstNode pattern(SourceLocation sourceLocation, AstNode arguments, AstNode is, AstNode expression) {
        return new PatternNode(sourceLocation, arguments, is, expression);
    }

    public static AstNode patternArguments(SourceLocation sourceLocation, List<AstNode> patternArguments) {
        return new PatternArgumentsNode(sourceLocation, patternArguments);
    }

    public static AstNode patternLiteral(SourceLocation sourceLocation, AstNode lambdaSlash, AstNode patternArguments, AstNode arrow, AstNode expression) {
        return new PatternLiteralNode(sourceLocation, lambdaSlash, patternArguments, arrow, expression);
    }

    public static AstNode patternName(SourceLocation sourceLocation, AstNode name) {
        return new PatternNameNode(sourceLocation, name);
    }

    public static AstNode patternSignature(SourceLocation sourceLocation, List<AstNode> patternNames, AstNode doubleColon, AstNode typeSignature) {
        return new PatternSignatureNode(sourceLocation, patternNames, doubleColon, typeSignature);
    }

    public static AstNode prefixOperator(SourceLocation sourceLocation, AstNode prefix) {
        return new PrefixOperatorNode(sourceLocation, prefix);
    }

    public static AstNode primary(SourceLocation sourceLocation, AstNode value) {
        return new PrimaryExpressionNode(sourceLocation, value);
    }

    public static AstNode qualified(SourceLocation sourceLocation, List<AstNode> dotNames) {
        return new QualifiedNameNode(sourceLocation, dotNames);
    }

    public static AstNode reference(SourceLocation sourceLocation, AstNode name) {
        return new NameReferenceNode(sourceLocation, name);
    }

    public static AstNode string(SourceLocation sourceLocation, AstNode value) {
        return new StringNode(sourceLocation, value);
    }

    public static AstNode sumType(SourceLocation sourceLocation, AstNode name, List<AstNode> parameters) {
        return new SumTypeNode(sourceLocation, name, parameters);
    }

    public static AstNode terminal(Token token) {
        return new TerminalNode(token);
    }

    public static AstNode tupleArgument(SourceLocation sourceLocation, AstNode openParen, List<AstNode> fieldMatches, AstNode closeParen) {
        return new TupleArgumentNode(sourceLocation, openParen, fieldMatches, closeParen);
    }

    public static AstNode tupleFieldMatch(SourceLocation sourceLocation, AstNode match) {
        return new TupleFieldMatchNode(sourceLocation, match);
    }

    public static AstNode tupleLiteral(SourceLocation sourceLocation, AstNode openParen, List<AstNode> fieldsAndCommas, AstNode closeParen) {
        return new TupleLiteralNode(sourceLocation, openParen, fieldsAndCommas, closeParen);
    }

    public static AstNode tupleType(SourceLocation sourceLocation, AstNode openParen, List<AstNode> fieldsAndCommas, AstNode closeParen) {
        return new TupleTypeNode(sourceLocation, openParen, fieldsAndCommas, closeParen);
    }

    public static AstNode tupleTypeField(SourceLocation sourceLocation, AstNode type) {
        return new TupleTypeFieldNode(sourceLocation, type);
    }

    public static AstNode typeContext(SourceLocation sourceLocation, AstNode typeClass, AstNode variableType) {
        return new TypeContextNode(sourceLocation, typeClass, variableType);
    }

    public static AstNode typeSignature(SourceLocation sourceLocation, AstNode type) {
        return new TypeSignatureNode(sourceLocation, type);
    }

    public static AstNode unshuffledArgument(SourceLocation sourceLocation, List<AstNode> arguments) {
        return new UnshuffledArgumentNode(sourceLocation, arguments);
    }

    public static AstNode variableType(SourceLocation sourceLocation, AstNode name) {
        return new VariableTypeNode(sourceLocation, name);
    }

    private AstNodes() {
        // intentionally empty
    }
}
