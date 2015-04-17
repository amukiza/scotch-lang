package scotch.compiler.ast;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static scotch.compiler.text.SourceLocation.NULL_SOURCE;
import static scotch.compiler.util.TestUtil.token;

import java.util.List;
import java.util.Optional;
import scotch.compiler.scanner.Token.TokenKind;

public final class TestUtil {

    public static AstNode bool(AstNode value) {
        return AstNodes.bool(NULL_SOURCE, value);
    }

    public static AstNode captureArgument(AstNode identifier) {
        return AstNodes.captureArgument(NULL_SOURCE, identifier);
    }

    public static AstNode char_(AstNode value) {
        return AstNodes.char_(NULL_SOURCE, value);
    }

    public static AstNode classArgument(AstNode name) {
        return AstNodes.classArgument(NULL_SOURCE, name);
    }

    public static AstNode classDefinition(AstNode classKeyword, AstNode context, AstNode className, List<AstNode> arguments, AstNode where, AstNode classMembers) {
        return AstNodes.classDefinition(NULL_SOURCE, classKeyword, Optional.of(context), className, arguments, where, classMembers);
    }

    public static AstNode classDefinition(AstNode classKeyword, AstNode className, List<AstNode> arguments, AstNode where, AstNode classMembers) {
        return AstNodes.classDefinition(NULL_SOURCE, classKeyword, Optional.empty(), className, arguments, where, classMembers);
    }

    public static AstNode classMembers(AstNode openCurly, List<AstNode> classMembers, AstNode closeCurly) {
        return AstNodes.classMembers(NULL_SOURCE, openCurly, classMembers, closeCurly);
    }

    public static AstNode conditional(AstNode ifKeyword, AstNode condition, AstNode thenKeyword, AstNode trueCase, AstNode elseKeyword, AstNode falseCase) {
        return AstNodes.conditional(NULL_SOURCE, ifKeyword, condition, thenKeyword, trueCase, elseKeyword, falseCase);
    }

    public static AstNode constantArgument(AstNode constructor) {
        return AstNodes.constantArgument(NULL_SOURCE, constructor);
    }

    public static AstNode constructorArgument(AstNode constructor, AstNode fieldMatches) {
        return AstNodes.constructorArgument(NULL_SOURCE, constructor, fieldMatches);
    }

    public static AstNode constructorType(AstNode name, List<AstNode> parameters) {
        return AstNodes.constructorType(NULL_SOURCE, name, parameters);
    }

    public static AstNode context(AstNode openParen, List<AstNode> typeContexts, AstNode closeParen, AstNode contextArrow) {
        return AstNodes.context(NULL_SOURCE, openParen, typeContexts, closeParen, contextArrow);
    }

    public static AstNode contextType(AstNode context, AstNode type) {
        return AstNodes.contextType(NULL_SOURCE, context, type);
    }

    public static AstNode dataConstant(AstNode name) {
        return AstNodes.dataConstant(NULL_SOURCE, name);
    }

    public static AstNode dataRecord(AstNode name, AstNode openCurly, List<AstNode> fieldsAndCommas, AstNode closeCurly) {
        return AstNodes.dataRecord(NULL_SOURCE, name, openCurly, fieldsAndCommas, closeCurly);
    }

    public static AstNode dataRecordField(AstNode fieldName, AstNode hasType, AstNode type) {
        return AstNodes.dataRecordField(NULL_SOURCE, fieldName, hasType, type);
    }

    public static AstNode dataTuple(AstNode name, List<AstNode> fields) {
        return AstNodes.dataTuple(NULL_SOURCE, name, fields);
    }

    public static AstNode dataTupleField(AstNode type) {
        return AstNodes.dataTupleField(NULL_SOURCE, type);
    }

    public static AstNode dataType(AstNode data, AstNode identifier, AstNode assign, List<AstNode> members) {
        return dataType(data, identifier, emptyList(), assign, members);
    }

    public static AstNode dataType(AstNode data, AstNode identifier, List<AstNode> parameters, AstNode assign, List<AstNode> members) {
        return AstNodes.dataType(NULL_SOURCE, data, identifier, parameters, assign, members);
    }

    public static AstNode doStatement(AstNode expression, AstNode semicolon) {
        return AstNodes.doStatement(NULL_SOURCE, expression, semicolon);
    }

    public static AstNode doStatements(AstNode doKeyword, AstNode openCurly, List<AstNode> statements, AstNode closeCurly) {
        return AstNodes.doStatements(NULL_SOURCE, doKeyword, openCurly, statements, closeCurly);
    }

    public static AstNode double_(AstNode value) {
        return AstNodes.double_(NULL_SOURCE, value);
    }

    public static AstNode drawFromStatement(AstNode patternArguments, AstNode drawFrom, AstNode expression, AstNode terminal) {
        return AstNodes.drawFromStatement(NULL_SOURCE, patternArguments, drawFrom, expression, terminal);
    }

    public static AstNode emptyNamedFields(AstNode openBrace, AstNode closeBrace) {
        return AstNodes.emptyNamedFields(NULL_SOURCE, openBrace, closeBrace);
    }

    public static AstNode expression(List<AstNode> primaryExpressions) {
        return AstNodes.expression(NULL_SOURCE, primaryExpressions);
    }

    public static AstNode functionType(AstNode argument, AstNode arrow, AstNode result) {
        return AstNodes.functionType(NULL_SOURCE, argument, arrow, result);
    }

    public static AstNode ignoreArgument(AstNode underscore) {
        return AstNodes.ignoreArgument(NULL_SOURCE, underscore);
    }

    public static AstNode importStmt(AstNode import_, AstNode semicolon) {
        return AstNodes.importStmt(NULL_SOURCE, import_, semicolon);
    }

    public static AstNode importStmts(List<AstNode> statements) {
        return AstNodes.importStmts(NULL_SOURCE, statements);
    }

    public static AstNode import_(AstNode importKeyword, AstNode qualifiedName) {
        return AstNodes.import_(NULL_SOURCE, importKeyword, qualifiedName);
    }

    public static AstNode infixOperator(AstNode side, AstNode infix) {
        return AstNodes.infixOperator(NULL_SOURCE, side, infix);
    }

    public static AstNode initializer(AstNode constructor, AstNode openCurly, List<AstNode> initializerFields, AstNode closeCurly) {
        return AstNodes.initializer(NULL_SOURCE, constructor, openCurly, initializerFields, closeCurly);
    }

    public static AstNode initializerField(AstNode fieldName, AstNode is, AstNode value) {
        return AstNodes.initializerField(NULL_SOURCE, fieldName, is, value);
    }

    public static AstNode instanceDefinition(AstNode instanceKeyword, AstNode instanceName, List<AstNode> instanceArguments, AstNode where, AstNode instanceMembers) {
        return AstNodes.instanceDefinition(NULL_SOURCE, instanceKeyword, instanceName, instanceArguments, where, instanceMembers);
    }

    public static AstNode instanceMembers(AstNode openCurly, List<AstNode> instanceMembers, AstNode closeCurly) {
        return AstNodes.instanceMembers(NULL_SOURCE, openCurly, instanceMembers, closeCurly);
    }

    public static AstNode integer(AstNode value) {
        return AstNodes.integer(NULL_SOURCE, value);
    }

    public static AstNode listLiteral(AstNode openSquare, List<AstNode> elements, AstNode closeSquare) {
        return AstNodes.listLiteral(NULL_SOURCE, openSquare, elements, closeSquare);
    }

    public static AstNode listType(AstNode openSquare, AstNode type, AstNode closeSquare) {
        return AstNodes.listType(NULL_SOURCE, openSquare, type, closeSquare);
    }

    public static AstNode literal(AstNode value) {
        return AstNodes.literal(NULL_SOURCE, value);
    }

    public static AstNode literalArgument(AstNode literal) {
        return AstNodes.literalArgument(NULL_SOURCE, literal);
    }

    public static AstNode module(AstNode module, AstNode moduleName, AstNode semicolon, AstNode importStatements, AstNode members) {
        return AstNodes.module(NULL_SOURCE, module, moduleName, semicolon, importStatements, members);
    }

    public static AstNode moduleMember(AstNode member, AstNode semicolon) {
        return AstNodes.moduleMember(NULL_SOURCE, member, semicolon);
    }

    public static AstNode moduleMembers(List<AstNode> moduleMembers) {
        return AstNodes.moduleMembers(NULL_SOURCE, moduleMembers);
    }

    public static AstNode namedFieldImplicitCapture(AstNode fieldName) {
        return AstNodes.namedFieldImplicitCapture(NULL_SOURCE, fieldName);
    }

    public static AstNode namedFieldMatch(AstNode name, AstNode equals, AstNode patternArgument) {
        return AstNodes.namedFieldMatch(NULL_SOURCE, name, equals, patternArgument);
    }

    public static AstNode namedFieldMatches(AstNode openBrace, List<AstNode> fieldMatches, AstNode closeBrace) {
        return AstNodes.namedFieldMatches(NULL_SOURCE, openBrace, fieldMatches, closeBrace);
    }

    public static AstNode operator(AstNode operator) {
        return AstNodes.operator(NULL_SOURCE, operator);
    }

    public static AstNode operatorDefinition(AstNode fixity, AstNode precedence, List<AstNode> operators) {
        return AstNodes.operatorDefinition(NULL_SOURCE, fixity, precedence, operators);
    }

    public static AstNode operatorFixity(AstNode fixity) {
        return AstNodes.operatorFixity(NULL_SOURCE, fixity);
    }

    public static AstNode parenthesized(AstNode openParen, AstNode argument, AstNode closeParen) {
        return AstNodes.parenthesized(NULL_SOURCE, openParen, argument, closeParen);
    }

    public static AstNode pattern(AstNode arguments, AstNode assign, AstNode expression) {
        return AstNodes.pattern(NULL_SOURCE, arguments, assign, expression);
    }

    public static AstNode patternArguments(List<AstNode> patternArguments) {
        return AstNodes.patternArguments(NULL_SOURCE, patternArguments);
    }

    public static AstNode patternLiteral(AstNode lambdaSlash, AstNode patternArguments, AstNode arrow, AstNode expression) {
        return AstNodes.patternLiteral(NULL_SOURCE, lambdaSlash, patternArguments, arrow, expression);
    }

    public static AstNode patternName(AstNode name) {
        return AstNodes.patternName(NULL_SOURCE, name);
    }

    public static AstNode patternSignature(List<AstNode> patternNames, AstNode doubleColon, AstNode typeSignature) {
        return AstNodes.patternSignature(NULL_SOURCE, patternNames, doubleColon, typeSignature);
    }

    public static AstNode prefixOperator(AstNode prefix) {
        return AstNodes.prefixOperator(NULL_SOURCE, prefix);
    }

    public static AstNode primary(AstNode value) {
        return AstNodes.primary(NULL_SOURCE, value);
    }

    public static AstNode qualified(AstNode name) {
        return qualified(asList(name));
    }

    public static AstNode qualified(List<AstNode> dotNames) {
        return AstNodes.qualified(NULL_SOURCE, dotNames);
    }

    public static AstNode reference(AstNode name) {
        return AstNodes.reference(NULL_SOURCE, name);
    }

    public static AstNode string(AstNode value) {
        return AstNodes.string(NULL_SOURCE, value);
    }

    public static AstNode sumType(AstNode name) {
        return sumType(name, emptyList());
    }

    public static AstNode sumType(AstNode name, List<AstNode> parameters) {
        return AstNodes.sumType(NULL_SOURCE, name, parameters);
    }

    public static AstNode terminal(TokenKind tokenKind, Object value) {
        return AstNodes.terminal(token(tokenKind, value));
    }

    public static AstNode tupleArgument(AstNode openParen, List<AstNode> fieldMatches, AstNode closeParen) {
        return AstNodes.tupleArgument(NULL_SOURCE, openParen, fieldMatches, closeParen);
    }

    public static AstNode tupleFieldMatch(AstNode argument) {
        return AstNodes.tupleFieldMatch(NULL_SOURCE, argument);
    }

    public static AstNode tupleLiteral(AstNode openParen, List<AstNode> fieldsAndCommas, AstNode closeParen) {
        return AstNodes.tupleLiteral(NULL_SOURCE, openParen, fieldsAndCommas, closeParen);
    }

    public static AstNode tupleType(AstNode openParen, List<AstNode> fieldsAndCommas, AstNode closeParen) {
        return AstNodes.tupleType(NULL_SOURCE, openParen, fieldsAndCommas, closeParen);
    }

    public static AstNode tupleTypeField(AstNode type) {
        return AstNodes.tupleTypeField(NULL_SOURCE, type);
    }

    public static AstNode typeContext(AstNode typeClass, AstNode variableType) {
        return AstNodes.typeContext(NULL_SOURCE, typeClass, variableType);
    }

    public static AstNode typeSignature(AstNode type) {
        return AstNodes.typeSignature(NULL_SOURCE, type);
    }

    public static AstNode unshuffledArgument(List<AstNode> arguments) {
        return AstNodes.unshuffledArgument(NULL_SOURCE, arguments);
    }

    public static AstNode variableType(AstNode name) {
        return AstNodes.variableType(NULL_SOURCE, name);
    }

    private TestUtil() {
        // intentionally empty
    }
}
