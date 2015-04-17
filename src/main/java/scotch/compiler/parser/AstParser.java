package scotch.compiler.parser;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static scotch.compiler.ast.AstNodes.bool;
import static scotch.compiler.ast.AstNodes.captureArgument;
import static scotch.compiler.ast.AstNodes.char_;
import static scotch.compiler.ast.AstNodes.classArgument;
import static scotch.compiler.ast.AstNodes.classDefinition;
import static scotch.compiler.ast.AstNodes.classMembers;
import static scotch.compiler.ast.AstNodes.conditional;
import static scotch.compiler.ast.AstNodes.constantArgument;
import static scotch.compiler.ast.AstNodes.constructorArgument;
import static scotch.compiler.ast.AstNodes.constructorType;
import static scotch.compiler.ast.AstNodes.context;
import static scotch.compiler.ast.AstNodes.contextType;
import static scotch.compiler.ast.AstNodes.dataConstant;
import static scotch.compiler.ast.AstNodes.dataRecord;
import static scotch.compiler.ast.AstNodes.dataRecordField;
import static scotch.compiler.ast.AstNodes.dataTuple;
import static scotch.compiler.ast.AstNodes.dataTupleField;
import static scotch.compiler.ast.AstNodes.dataType;
import static scotch.compiler.ast.AstNodes.doStatement;
import static scotch.compiler.ast.AstNodes.doStatements;
import static scotch.compiler.ast.AstNodes.double_;
import static scotch.compiler.ast.AstNodes.drawFromStatement;
import static scotch.compiler.ast.AstNodes.emptyNamedFields;
import static scotch.compiler.ast.AstNodes.expression;
import static scotch.compiler.ast.AstNodes.functionType;
import static scotch.compiler.ast.AstNodes.ignoreArgument;
import static scotch.compiler.ast.AstNodes.importStmt;
import static scotch.compiler.ast.AstNodes.importStmts;
import static scotch.compiler.ast.AstNodes.import_;
import static scotch.compiler.ast.AstNodes.infixOperator;
import static scotch.compiler.ast.AstNodes.initializer;
import static scotch.compiler.ast.AstNodes.initializerField;
import static scotch.compiler.ast.AstNodes.instanceDefinition;
import static scotch.compiler.ast.AstNodes.instanceMembers;
import static scotch.compiler.ast.AstNodes.integer;
import static scotch.compiler.ast.AstNodes.listLiteral;
import static scotch.compiler.ast.AstNodes.listType;
import static scotch.compiler.ast.AstNodes.literal;
import static scotch.compiler.ast.AstNodes.literalArgument;
import static scotch.compiler.ast.AstNodes.module;
import static scotch.compiler.ast.AstNodes.moduleMember;
import static scotch.compiler.ast.AstNodes.moduleMembers;
import static scotch.compiler.ast.AstNodes.modules;
import static scotch.compiler.ast.AstNodes.namedFieldImplicitCapture;
import static scotch.compiler.ast.AstNodes.namedFieldMatch;
import static scotch.compiler.ast.AstNodes.namedFieldMatches;
import static scotch.compiler.ast.AstNodes.operator;
import static scotch.compiler.ast.AstNodes.operatorDefinition;
import static scotch.compiler.ast.AstNodes.operatorFixity;
import static scotch.compiler.ast.AstNodes.parenthesized;
import static scotch.compiler.ast.AstNodes.pattern;
import static scotch.compiler.ast.AstNodes.patternArguments;
import static scotch.compiler.ast.AstNodes.patternLiteral;
import static scotch.compiler.ast.AstNodes.patternName;
import static scotch.compiler.ast.AstNodes.patternSignature;
import static scotch.compiler.ast.AstNodes.prefixOperator;
import static scotch.compiler.ast.AstNodes.primary;
import static scotch.compiler.ast.AstNodes.qualified;
import static scotch.compiler.ast.AstNodes.reference;
import static scotch.compiler.ast.AstNodes.string;
import static scotch.compiler.ast.AstNodes.sumType;
import static scotch.compiler.ast.AstNodes.terminal;
import static scotch.compiler.ast.AstNodes.tupleArgument;
import static scotch.compiler.ast.AstNodes.tupleLiteral;
import static scotch.compiler.ast.AstNodes.tupleType;
import static scotch.compiler.ast.AstNodes.tupleTypeField;
import static scotch.compiler.ast.AstNodes.typeContext;
import static scotch.compiler.ast.AstNodes.typeSignature;
import static scotch.compiler.ast.AstNodes.unshuffledArgument;
import static scotch.compiler.ast.AstNodes.variableType;
import static scotch.compiler.scanner.Token.TokenKind.ALTERNATIVE;
import static scotch.compiler.scanner.Token.TokenKind.ARROW;
import static scotch.compiler.scanner.Token.TokenKind.BOOL;
import static scotch.compiler.scanner.Token.TokenKind.CHAR;
import static scotch.compiler.scanner.Token.TokenKind.CLASS;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_CURLY;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_PAREN;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_SQUARE;
import static scotch.compiler.scanner.Token.TokenKind.COMMA;
import static scotch.compiler.scanner.Token.TokenKind.CONTEXT_ARROW;
import static scotch.compiler.scanner.Token.TokenKind.DATA;
import static scotch.compiler.scanner.Token.TokenKind.DO;
import static scotch.compiler.scanner.Token.TokenKind.DOT;
import static scotch.compiler.scanner.Token.TokenKind.DOUBLE;
import static scotch.compiler.scanner.Token.TokenKind.DRAW_FROM;
import static scotch.compiler.scanner.Token.TokenKind.ELSE;
import static scotch.compiler.scanner.Token.TokenKind.EOF;
import static scotch.compiler.scanner.Token.TokenKind.HAS_TYPE;
import static scotch.compiler.scanner.Token.TokenKind.ID;
import static scotch.compiler.scanner.Token.TokenKind.IF;
import static scotch.compiler.scanner.Token.TokenKind.IMPORT;
import static scotch.compiler.scanner.Token.TokenKind.INFIX;
import static scotch.compiler.scanner.Token.TokenKind.INSTANCE;
import static scotch.compiler.scanner.Token.TokenKind.INT;
import static scotch.compiler.scanner.Token.TokenKind.IS;
import static scotch.compiler.scanner.Token.TokenKind.LAMBDA_SLASH;
import static scotch.compiler.scanner.Token.TokenKind.LEFT;
import static scotch.compiler.scanner.Token.TokenKind.MODULE;
import static scotch.compiler.scanner.Token.TokenKind.OPEN_CURLY;
import static scotch.compiler.scanner.Token.TokenKind.OPEN_PAREN;
import static scotch.compiler.scanner.Token.TokenKind.OPEN_SQUARE;
import static scotch.compiler.scanner.Token.TokenKind.PREFIX;
import static scotch.compiler.scanner.Token.TokenKind.RIGHT;
import static scotch.compiler.scanner.Token.TokenKind.SEMICOLON;
import static scotch.compiler.scanner.Token.TokenKind.STRING;
import static scotch.compiler.scanner.Token.TokenKind.THEN;
import static scotch.compiler.scanner.Token.TokenKind.WHERE;
import static scotch.compiler.scanner.Token.TokenKind.UNDERSCORE;
import static scotch.symbol.Symbol.symbol;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import scotch.compiler.ast.AstNode;
import scotch.compiler.ast.AstNodes;
import scotch.compiler.scanner.Scanner;
import scotch.compiler.scanner.Token;
import scotch.compiler.scanner.Token.TokenKind;
import scotch.compiler.text.NamedSourcePoint;
import scotch.compiler.text.SourceLocation;

public class AstParser {

    private static final List<TokenKind> LITERALS = asList(INT, BOOL, DOUBLE, CHAR, STRING);
    private final LookAheadScanner        scanner;
    private final Deque<NamedSourcePoint> positions;

    public AstParser(Scanner scanner) {
        this.scanner = new LookAheadScanner(scanner);
        this.positions = new ArrayDeque<>();
    }

    public AstNode createReference(AstNode reference) {
        return reference(getSourceLocation(), reference);
    }

    public boolean expectsOperatorDefinition() {
        return (expects("left") || expects("right")) && expectsAt(1, "infix")
            || expects("prefix");
    }

    public boolean expectsTupleArgument() {
        return expects(OPEN_PAREN);
    }

    public AstNode parseBool() {
        markPosition();
        AstNode value = parse(BOOL);
        return bool(getSourceLocation(), value);
    }

    public AstNode parseCaptureArgument() {
        markPosition();
        AstNode reference = parseName();
        return captureArgument(getSourceLocation(), reference);
    }

    public AstNode parseChar() {
        markPosition();
        AstNode value = parse(CHAR);
        return char_(getSourceLocation(), value);
    }

    public AstNode parseClassDefinition() {
        markPosition();
        AstNode classKeyword = require("class", CLASS);
        Optional<AstNode> context = parseClassContext();
        AstNode className = parse(ID);
        List<AstNode> arguments = new ArrayList<>();
        arguments.add(parseClassArgument());
        while (expectsClassArgument()) {
            arguments.add(parseClassArgument());
        }
        AstNode where = parse(WHERE);
        AstNode classMembers = parseClassMembers();
        return classDefinition(getSourceLocation(), classKeyword, context, className, arguments, where, classMembers);
    }

    public AstNode parseDataType() {
        markPosition();
        AstNode data = require("data", DATA);
        AstNode name = parseName();
        List<AstNode> parameters = new ArrayList<>();
        while (expectsVariableType()) {
            parameters.add(parseVariableType());
        }
        AstNode assign = parse(IS);
        List<AstNode> members = new ArrayList<>(asList(parseDataMember()));
        while (expects(ALTERNATIVE)) {
            members.add(parse(ALTERNATIVE));
            members.add(parseDataMember());
        }
        return dataType(getSourceLocation(), data, name, parameters, assign, members);
    }

    public AstNode parseDot() {
        return parse(DOT);
    }

    public AstNode parseDouble() {
        markPosition();
        AstNode value = parse(DOUBLE);
        return double_(getSourceLocation(), value);
    }

    public AstNode parseExpression() {
        markPosition();
        List<AstNode> primaryExpressions = new ArrayList<>();
        do {
            primaryExpressions.add(parsePrimary());
        } while (expectsPrimary());
        return expression(getSourceLocation(), primaryExpressions);
    }

    public AstNode parseImport() {
        markPosition();
        AstNode importKeyword = require("import", IMPORT);
        AstNode qualifiedName = parseQualifiedName();
        return import_(getSourceLocation(), importKeyword, qualifiedName);
    }

    public AstNode parseImportStatement() {
        markPosition();
        AstNode import_ = parseImport();
        AstNode terminator = parseTerminator();
        return importStmt(getSourceLocation(), import_, terminator);
    }

    public AstNode parseImportStatements() {
        markPosition();
        List<AstNode> importStmts = new ArrayList<>();
        while (expects("import")) {
            importStmts.add(parseImportStatement());
        }
        return importStmts(getSourceLocation(), importStmts);
    }

    public AstNode parseInstanceDefinition() {
        markPosition();
        AstNode instanceKeyword = require("instance", INSTANCE);
        AstNode instanceName = parseName();
        List<AstNode> arguments = new ArrayList<>(asList(parseAtomicSum()));
        while (expectsSumType()) {
            arguments.add(parseAtomicSum());
        }
        AstNode where = parse(WHERE);
        AstNode instanceMembers = parseInstanceMembers();
        return instanceDefinition(getSourceLocation(), instanceKeyword, instanceName, arguments, where, instanceMembers);
    }

    public AstNode parseInt() {
        markPosition();
        AstNode value = parse(INT);
        return integer(getSourceLocation(), value);
    }

    public AstNode parseLiteral() {
        markPosition();
        AstNode value = null;
        if (expects(INT)) {
            value = parseInt();
        } else if (expects(DOUBLE)) {
            value = parseDouble();
        } else if (expects(CHAR)) {
            value = parseChar();
        } else if (expects(STRING)) {
            value = parseString();
        } else if (expects(BOOL)) {
            value = parseBool();
        }
        if (value == null) {
            throw new UnsupportedOperationException(); // TODO
        } else {
            return literal(getSourceLocation(), value);
        }
    }

    public AstNode parseLiteralArgument() {
        markPosition();
        AstNode literal = parseLiteral();
        return literalArgument(getSourceLocation(), literal);
    }

    public AstNode parseModule() {
        markPosition();
        AstNode module = require("module", MODULE);
        AstNode moduleName = parseQualifiedName();
        AstNode terminator = parseTerminator();
        AstNode importStatements = parseImportStatements();
        AstNode moduleMembers = parseModuleMembers();
        return module(getSourceLocation(), module, moduleName, terminator, importStatements, moduleMembers);
    }

    public AstNode parseModuleMember() {
        markPosition();
        AstNode member;
        if (expects("class")) {
            member = parseClassDefinition();
        } else if (expects("instance")) {
            member = parseInstanceDefinition();
        } else if (expectsOperatorDefinition()) {
            member = parseOperatorDefinition();
        } else if (expects("data")) {
            member = parseDataType();
        } else if (expectsPatternSignature()) {
            member = parsePatternSignature();
        } else {
            member = parsePattern();
        }
        AstNode terminator = parseTerminator();
        return moduleMember(getSourceLocation(), member, terminator);
    }

    public AstNode parseModuleMembers() {
        markPosition();
        List<AstNode> moduleMembers = new ArrayList<>();
        while (!expects("module") && !expects(EOF)) {
            moduleMembers.add(parseModuleMember());
        }
        return moduleMembers(getSourceLocation(), moduleMembers);
    }

    public AstNode parseModules() {
        markPosition();
        List<AstNode> modules = new ArrayList<>();
        while (expects("module")) {
            modules.add(parseModule());
        }
        List<AstNode> terminators = new ArrayList<>();
        while (expectsTerminator()) {
            terminators.add(parseTerminator());
        }
        AstNode eof = parse(EOF);
        return modules(getSourceLocation(), modules, terminators, eof);
    }

    public AstNode parseName() {
        return parse(ID);
    }

    public AstNode parseOperatorDefinition() {
        if (expectsOperatorDefinition()) {
            markPosition();
            AstNode fixity = parseOperatorFixity();
            AstNode precedence = parse(INT);
            List<AstNode> operators = new ArrayList<>();
            operators.add(parseOperatorName());
            while (expectsComma()) {
                operators.add(parseComma());
                operators.add(parseOperatorName());
            }
            return operatorDefinition(getSourceLocation(), fixity, precedence, operators);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    public AstNode parsePattern() {
        markPosition();
        AstNode patternArguments = parsePatternArguments();
        AstNode assign = parse(IS);
        AstNode expression = parseExpression();
        return pattern(getSourceLocation(), patternArguments, assign, expression);
    }

    public AstNode parsePatternArgument() {
        if (expectsLiteral()) {
            return parseLiteralArgument();
        } else if (expectsIgnoreArgument()) {
            return parseIgnoreArgument();
        } else if (expectsConstructorArgument()) {
            return parseConstructorArgument();
        } else if (expectsCaptureArgument()) {
            return parseCaptureArgument();
        } else if (expectsTupleArgument()) {
            return parseTupleArgument();
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    public AstNode parsePatternArguments() {
        markPosition();
        List<AstNode> patternArguments = new ArrayList<>();
        do {
            patternArguments.add(parsePatternArgument());
        } while (expectsPatternArgument());
        return patternArguments(getSourceLocation(), patternArguments);
    }

    public AstNode parsePatternSignature() {
        if (expectsPatternSignature()) {
            markPosition();
            List<AstNode> patternNames = new ArrayList<>(asList(parsePatternName()));
            while (expectsComma()) {
                patternNames.add(parseComma());
                patternNames.add(parsePatternName());
            }
            AstNode doubleColon = parse(HAS_TYPE);
            AstNode typeSignature = parseTypeSignature();
            return patternSignature(getSourceLocation(), patternNames, doubleColon, typeSignature);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    public AstNode parsePrimary() {
        markPosition();
        AstNode expression;
        if (expects(LAMBDA_SLASH)) {
            expression = parsePatternLiteral();
        } else if (expects(IF)) {
            expression = parseConditional();
        } else if (expects(OPEN_PAREN)) {
            expression = parsePrimaryParenthesized();
        } else if (expects(OPEN_SQUARE)) {
            expression = parseListLiteral();
        } else if (expectsLiteral()) {
            expression = parseLiteral();
        } else if (expectsReference()) {
            expression = parseReference();
        } else if (expectsDoNotation()) {
            expression = parseDoNotation();
        } else {
            throw new ParseException(
                "Wanted parenthesized expression, literal, name reference, conditional, or do-notation; got " + token().getKind(),
                token().getSourceLocation()
            );
        }
        return primary(getSourceLocation(), expression);
    }

    public AstNode parsePrimaryParenthesized() {
        markPosition();
        AstNode openParen = parse(OPEN_PAREN);
        AstNode member = parseExpression();
        if (expectsComma()) {
            List<AstNode> members = new ArrayList<>(asList(member));
            int counter = 0;
            while (expectsComma()) {
                counter++;
                members.add(parse(COMMA));
                members.add(parseExpression());
            }
            AstNode closeParen = parse(CLOSE_PAREN);
            if (counter >= 12) {
                throw new ParseException("Tuples cannot exceed 12 members in size", currentSourceLocation());
            } else {
                return tupleLiteral(getSourceLocation(), openParen, members, closeParen);
            }
        } else {
            AstNode closeParen = parse(CLOSE_PAREN);
            return parenthesized(getSourceLocation(), openParen, member, closeParen);
        }
    }

    public AstNode parseQualifiedName() {
        markPosition();
        List<AstNode> dotNames = new ArrayList<>(asList(parseName()));
        while (expectsDot()) {
            dotNames.add(parseDot());
            dotNames.add(parseName());
        }
        return qualified(getSourceLocation(), dotNames);
    }

    public AstNode parseReference() {
        markPosition();
        markPosition();
        AstNode reference = parseQualifiedName();
        if (expects(OPEN_CURLY)) {
            return parseInitializer(createReference(reference));
        } else {
            getSourceLocation();
            return createReference(reference);
        }
    }

    public AstNode parseString() {
        markPosition();
        AstNode value = parse(STRING);
        return string(getSourceLocation(), value);
    }

    public AstNode parseTerminator() {
        return parse(SEMICOLON);
    }

    public AstNode parseTypeSignature() {
        markPosition();
        AstNode context = parseContextType();
        return typeSignature(getSourceLocation(), context);
    }

    private boolean expects(TokenKind tokenKind) {
        return token().is(tokenKind);
    }

    private boolean expects(String word) {
        return token().is(ID) && word.equals(token().getValueAs(String.class));
    }

    private boolean expectsAt(int offset, TokenKind tokenKind) {
        return scanner.peekAt(offset).is(tokenKind);
    }

    private boolean expectsAt(int offset, String word) {
        return expectsAt(offset, ID) && word.equals(scanner.peekAt(offset).getValueAs(String.class));
    }

    private boolean expectsCaptureArgument() {
        return expectsName();
    }

    private boolean expectsClassArgument() {
        return expects(ID);
    }

    private boolean expectsComma() {
        return expects(COMMA);
    }

    private boolean expectsConstructorArgument() {
        return token().is(ID) && symbol(token().getValueAs(String.class)).isConstructorName();
    }

    private boolean expectsDataRecord() {
        return expects(OPEN_CURLY);
    }

    private boolean expectsDoNotation() {
        return expects(DO);
    }

    private boolean expectsDot() {
        return expects(DOT);
    }

    private boolean expectsDrawFromStatement() {
        int offset = 0;
        while (true) {
            if (expectsAt(offset, SEMICOLON)) {
                return false;
            } else if (expectsAt(offset, DRAW_FROM)) {
                return true;
            }
            offset++;
        }
    }

    private boolean expectsIgnoreArgument() {
        return expects("_");
    }

    private boolean expectsLiteral() {
        for (TokenKind tokenKind : LITERALS) {
            if (expects(tokenKind)) {
                return true;
            }
        }
        return false;
    }

    private boolean expectsName() {
        return expects(ID);
    }

    private boolean expectsPatternArgument() {
        return expectsLiteral()
            || expectsConstructorArgument()
            || expectsCaptureArgument();
    }

    private boolean expectsPatternSignature() {
        return expects(OPEN_PAREN) && expectsAt(1, ID) && expectsAt(2, CLOSE_PAREN) && (expectsAt(3, HAS_TYPE) || expectsAt(3, COMMA))
            || expectsName() && (expectsAt(1, HAS_TYPE) || expectsAt(1, COMMA));
    }

    private boolean expectsPrimary() {
        return expectsLiteral() || expectsReference() || expects(OPEN_PAREN);
    }

    private boolean expectsReference() {
        return expectsName();
    }

    private boolean expectsSumType() {
        return expectsName() && (expectsAt(1, DOT) || isUpperCase(token().getValueAs(String.class).charAt(0)));
    }

    private boolean expectsTerminator() {
        return expects(SEMICOLON);
    }

    private boolean expectsTupleField() {
        return expectsName() || expects(OPEN_PAREN);
    }

    private boolean expectsTypePrimary() {
        return expects(OPEN_PAREN) || expectsName();
    }

    private boolean expectsVariableType() {
        return expectsName() && isLowerCase(token().getValueAs(String.class).charAt(0));
    }

    private SourceLocation getSourceLocation() {
        return unmarkPosition().to(scanner.getPreviousPosition());
    }

    private void markPosition() {
        positions.push(scanner.getPosition());
    }

    private AstNode parse(TokenKind tokenKind) {
        return terminal(require(tokenKind));
    }

    private AstNode parseAtomicSum() {
        markPosition();
        AstNode name = parseQualifiedName();
        return sumType(getSourceLocation(), name, emptyList());
    }

    private AstNode parseClassArgument() {
        markPosition();
        AstNode name = parseName();
        return classArgument(getSourceLocation(), name);
    }

    private Optional<AstNode> parseClassContext() {
        if (expects(OPEN_PAREN)) {
            return Optional.of(parseContext());
        } else {
            return Optional.empty();
        }
    }

    private AstNode parseClassMember() {
        if (expectsPatternArgument()) {
            return parsePattern();
        } else if (expectsPatternSignature()) {
            return parsePatternSignature();
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    private AstNode parseClassMembers() {
        markPosition();
        AstNode openCurly = parse(OPEN_CURLY);
        List<AstNode> classMembers = new ArrayList<>();
        classMembers.add(parseClassMember());
        classMembers.addAll(parseTerminators());
        while (!expects(CLOSE_CURLY)) {
            classMembers.add(parseClassMember());
            classMembers.addAll(parseTerminators());
        }
        AstNode closeCurly = parse(CLOSE_CURLY);
        return classMembers(getSourceLocation(), openCurly, classMembers, closeCurly);
    }

    private AstNode parseComma() {
        return parse(COMMA);
    }

    private AstNode parseComplexArgument() {
        if (expectsPatternArgument()) {
            markPosition();
            AstNode argument = parsePatternArgument();
            if (expectsPatternArgument()) {
                List<AstNode> arguments = new ArrayList<>(asList(argument));
                while (expectsPatternArgument()) {
                    arguments.add(parsePatternArgument());
                }
                return unshuffledArgument(getSourceLocation(), arguments);
            } else {
                getSourceLocation();
                return argument;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private AstNode parseConditional() {
        markPosition();
        AstNode ifKeyword = parse(IF);
        AstNode condition = parseExpression();
        AstNode thenKeyword = parse(THEN);
        AstNode trueCase = parseExpression();
        AstNode elseKeyword = parse(ELSE);
        AstNode falseCase = parseExpression();
        return conditional(getSourceLocation(), ifKeyword, condition, thenKeyword, trueCase, elseKeyword, falseCase);
    }

    private AstNode parseConstructorArgument() {
        markPosition();
        AstNode name = parseQualifiedName();
        if (expects(OPEN_CURLY)) {
            AstNode fieldMatches = parseNamedFieldMatches();
            return constructorArgument(getSourceLocation(), name, fieldMatches);
        } else {
            return constantArgument(getSourceLocation(), name);
        }
    }

    private AstNode parseContext() {
        markPosition();
        AstNode openParen = parse(OPEN_PAREN);
        List<AstNode> typeContexts = new ArrayList<>(asList(parseTypeContext()));
        while (expectsComma()) {
            typeContexts.add(parseComma());
            typeContexts.add(parseTypeContext());
        }
        AstNode closeParen = parse(CLOSE_PAREN);
        AstNode contextArrow = parse(CONTEXT_ARROW);
        return context(getSourceLocation(), openParen, typeContexts, closeParen, contextArrow);
    }

    private AstNode parseContextType() {
        if (expects(OPEN_PAREN)) {
            int offset = 0;
            while (!expectsAt(offset, SEMICOLON)) {
                if (expectsAt(offset, CLOSE_PAREN) && expectsAt(offset + 1, CONTEXT_ARROW)) {
                    return parseContextType_();
                } else {
                    offset++;
                }
            }
        }
        return parseType();
    }

    private AstNode parseContextType_() {
        markPosition();
        AstNode context = parseContext();
        AstNode type = parseType();
        return contextType(getSourceLocation(), context, type);
    }

    private AstNode parseDataMember() {
        markPosition();
        AstNode name = parseName();
        if (expectsTupleField()) {
            List<AstNode> fields = new ArrayList<>();
            while (expectsTupleField()) {
                fields.add(parseTupleField());
            }
            return dataTuple(getSourceLocation(), name, fields);
        } else if (expectsDataRecord()) {
            AstNode openCurly = parse(OPEN_CURLY);
            List<AstNode> fieldsAndCommas = new ArrayList<>();
            fieldsAndCommas.add(parseDataRecordField());
            while (expectsComma()) {
                fieldsAndCommas.add(parseComma());
                fieldsAndCommas.add(parseDataRecordField());
            }
            AstNode closeCurly = parse(CLOSE_CURLY);
            return dataRecord(getSourceLocation(), name, openCurly, fieldsAndCommas, closeCurly);
        } else {
            return dataConstant(getSourceLocation(), name);
        }
    }

    private AstNode parseDataRecordField() {
        markPosition();
        AstNode name = parseName();
        AstNode hasType = parse(HAS_TYPE);
        AstNode type = parseType();
        return dataRecordField(getSourceLocation(), name, hasType, type);
    }

    private AstNode parseDoNotation() {
        markPosition();
        AstNode doKeyword = parse(DO);
        AstNode openCurly = parse(OPEN_CURLY);
        List<AstNode> doStatements = parseDoStatements();
        AstNode closeCurly = parse(CLOSE_CURLY);
        return doStatements(getSourceLocation(), doKeyword, openCurly, doStatements, closeCurly);
    }

    private AstNode parseDoStatement() {
        if (expectsDrawFromStatement()) {
            return parseDrawFromStatement();
        } else {
            markPosition();
            AstNode expression = parseExpression();
            AstNode terminator = parseTerminator();
            return doStatement(getSourceLocation(), expression, terminator);
        }
    }

    private List<AstNode> parseDoStatements() {
        List<AstNode> doStatements = new ArrayList<>();
        while (!expects(CLOSE_CURLY)) {
            if (expectsTerminator()) {
                doStatements.add(parseTerminator());
                break;
            } else {
                doStatements.add(parseDoStatement());
            }
        }
        return doStatements;
    }

    private AstNode parseDrawFromStatement() {
        markPosition();
        AstNode patternArguments = parsePatternArguments();
        AstNode drawFrom = parse(DRAW_FROM);
        AstNode expression = parseExpression();
        AstNode terminal = parseTerminator();
        return drawFromStatement(getSourceLocation(), patternArguments, drawFrom, expression, terminal);
    }

    private AstNode parseIgnoreArgument() {
        markPosition();
        AstNode underscore = require("_", UNDERSCORE);
        return ignoreArgument(getSourceLocation(), underscore);
    }

    private AstNode parseInitializer(AstNode reference) {
        AstNode openCurly = parse(OPEN_CURLY);
        List<AstNode> fieldsAndCommas = parseInitializerFields();
        AstNode closeCurly = parse(CLOSE_CURLY);
        return initializer(getSourceLocation(), reference, openCurly, fieldsAndCommas, closeCurly);
    }

    private AstNode parseInitializerField() {
        markPosition();
        AstNode field = parseName();
        AstNode is = parse(IS);
        AstNode value = parseExpression();
        return initializerField(getSourceLocation(), field, is, value);
    }

    private List<AstNode> parseInitializerFields() {
        List<AstNode> fieldsAndCommas = new ArrayList<>(asList(parseInitializerField()));
        while (expectsComma()) {
            fieldsAndCommas.add(parseComma());
            if (expectsName()) {
                fieldsAndCommas.add(parseInitializerField());
            } else {
                break;
            }
        }
        return fieldsAndCommas;
    }

    private AstNode parseInstanceMembers() {
        markPosition();
        AstNode openCurly = parse(OPEN_CURLY);
        List<AstNode> instanceMembers = new ArrayList<>();
        instanceMembers.add(parsePattern());
        instanceMembers.addAll(parseTerminators());
        while (!expects(CLOSE_CURLY)) {
            instanceMembers.add(parsePattern());
            instanceMembers.addAll(parseTerminators());
        }
        AstNode closeCurly = parse(CLOSE_CURLY);
        return instanceMembers(getSourceLocation(), openCurly, instanceMembers, closeCurly);
    }

    private AstNode parseLambdaArgument() {
        if (expectsIgnoreArgument()) {
            return parseIgnoreArgument();
        } else if (expectsConstructorArgument()) {
            return parseConstructorArgument();
        } else if (expectsCaptureArgument()) {
            return parseCaptureArgument();
        } else if (expectsTupleArgument()) {
            return parseTupleArgument();
        } else {
            throw new ParseException("Unexpected " + token().getKind(), token().getSourceLocation());
        }
    }

    private AstNode parseLambdaArguments() {
        markPosition();
        List<AstNode> arguments = new ArrayList<>();
        while (!expects(ARROW)) {
            arguments.add(parseLambdaArgument());
        }
        return patternArguments(getSourceLocation(), arguments);
    }

    private List<AstNode> parseListElements() {
        List<AstNode> elements = new ArrayList<>();
        while (!expects(CLOSE_SQUARE)) {
            elements.add(parseExpression());
            if (expectsComma()) {
                elements.add(parseComma());
            } else if (expects(CLOSE_SQUARE)) {
                break;
            }
        }
        return elements;
    }

    private AstNode parseListLiteral() {
        markPosition();
        AstNode openSquare = parse(OPEN_SQUARE);
        List<AstNode> elements = parseListElements();
        AstNode closeSquare = parse(CLOSE_SQUARE);
        return listLiteral(getSourceLocation(), openSquare, elements, closeSquare);
    }

    private AstNode parseNamedFieldMatch() {
        markPosition();
        AstNode name = parseName();
        if (expects(IS)) {
            AstNode equals = parse(IS);
            AstNode match = parseComplexArgument();
            return namedFieldMatch(getSourceLocation(), name, equals, match);
        } else {
            return namedFieldImplicitCapture(getSourceLocation(), name);
        }
    }

    private AstNode parseNamedFieldMatches() {
        markPosition();
        AstNode leftCurly = parse(OPEN_CURLY);
        if (expectsName()) {
            List<AstNode> fieldMatches = new ArrayList<>();
            while (expectsName()) {
                AstNode fieldMatch = parseNamedFieldMatch();
                if (expectsComma()) {
                    fieldMatches.add(fieldMatch);
                    fieldMatches.add(parseComma());
                } else {
                    fieldMatches.add(fieldMatch);
                    break;
                }
            }
            AstNode rightCurly = parse(CLOSE_CURLY);
            return namedFieldMatches(getSourceLocation(), leftCurly, fieldMatches, rightCurly);
        } else {
            AstNode rightCurly = parse(CLOSE_CURLY);
            return emptyNamedFields(getSourceLocation(), leftCurly, rightCurly);
        }
    }

    private AstNode parseOperatorFixity() {
        if (expects("left")) {
            markPosition();
            AstNode left = require("left", LEFT);
            AstNode infix = require("infix", INFIX);
            SourceLocation sourceLocation = getSourceLocation();
            return operatorFixity(sourceLocation, infixOperator(sourceLocation, left, infix));
        } else if (expects("right")) {
            markPosition();
            AstNode right = require("right", RIGHT);
            AstNode infix = require("infix", INFIX);
            SourceLocation sourceLocation = getSourceLocation();
            return operatorFixity(sourceLocation, infixOperator(sourceLocation, right, infix));
        } else if (expects("prefix")) {
            markPosition();
            AstNode prefix = require("prefix", PREFIX);
            SourceLocation sourceLocation = getSourceLocation();
            return operatorFixity(sourceLocation, prefixOperator(sourceLocation, prefix));
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    private AstNode parseOperatorName() {
        if (expects(OPEN_PAREN)) {
            markPosition();
            AstNode openParen = parse(OPEN_PAREN);
            AstNode operatorName = parseName();
            AstNode closeParen = parse(CLOSE_PAREN);
            SourceLocation sourceLocation = getSourceLocation();
            return operator(sourceLocation, parenthesized(sourceLocation, openParen, operatorName, closeParen));
        } else if (expectsName()) {
            markPosition();
            AstNode operatorName = parseName();
            return operator(getSourceLocation(), operatorName);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    private AstNode parseParenthesizedArgument(AstNode openParen, AstNode fieldMatch) {
        AstNode closeParen = parse(CLOSE_PAREN);
        return parenthesized(getSourceLocation(), openParen, fieldMatch, closeParen);
    }

    private AstNode parsePatternLiteral() {
        markPosition();
        AstNode lambdaSlash = parse(LAMBDA_SLASH);
        AstNode patternArguments = parseLambdaArguments();
        AstNode arrow = parse(ARROW);
        AstNode expression = parseExpression();
        return patternLiteral(getSourceLocation(), lambdaSlash, patternArguments, arrow, expression);
    }

    private AstNode parsePatternName() {
        if (expectsName()) {
            markPosition();
            AstNode name = parseName();
            return patternName(getSourceLocation(), name);
        } else if (expects(OPEN_PAREN) && expectsAt(1, ID) && expectsAt(2, CLOSE_PAREN)) {
            markPosition();
            AstNode openParen = parse(OPEN_PAREN);
            AstNode name = parseName();
            AstNode closeParen = parse(CLOSE_PAREN);
            SourceLocation sourceLocation = getSourceLocation();
            return patternName(sourceLocation, parenthesized(sourceLocation, openParen, name, closeParen));
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    private AstNode parseSumParameter() {
        if (expects(OPEN_PAREN)) {
            return parseTypePrimary();
        } else if (expectsSumType()) {
            return parseAtomicSum();
        } else {
            markPosition();
            AstNode name = parseName();
            return variableType(getSourceLocation(), name);
        }
    }

    private List<AstNode> parseTerminators() {
        List<AstNode> terminators = new ArrayList<>();
        terminators.add(parseTerminator());
        while (expectsTerminator()) {
            terminators.add(parseTerminator());
        }
        return terminators;
    }

    private AstNode parseTupleArgument() {
        markPosition();
        AstNode openParen = parse(OPEN_PAREN);
        AstNode fieldMatch = parseComplexArgument();
        if (expectsComma()) {
            return parseTupleArgument_(openParen, fieldMatch);
        } else {
            return parseParenthesizedArgument(openParen, fieldMatch);
        }
    }

    private AstNode parseTupleArgument_(AstNode openParen, AstNode argument) {
        List<AstNode> commasAndFieldMatches = new ArrayList<>(asList(tupleFieldMatch(argument)));
        while (expectsComma()) {
            markPosition();
            commasAndFieldMatches.add(parseComma());
            commasAndFieldMatches.add(tupleFieldMatch(parseComplexArgument()));
        }
        AstNode closeParen = parse(CLOSE_PAREN);
        return tupleArgument(getSourceLocation(), openParen, commasAndFieldMatches, closeParen);
    }

    private AstNode parseTupleField() {
        markPosition();
        AstNode type;
        if (expects(OPEN_PAREN)) {
            type = parseTypePrimary();
        } else if (expectsSumType()) {
            markPosition();
            AstNode name = parseQualifiedName();
            type = sumType(getSourceLocation(), name, emptyList());
        } else {
            markPosition();
            AstNode name = parseName();
            type = variableType(getSourceLocation(), name);
        }
        return dataTupleField(getSourceLocation(), type);
    }

    private AstNode parseTupleTypeField() {
        markPosition();
        AstNode type = parseType();
        return tupleTypeField(getSourceLocation(), type);
    }

    private AstNode parseType() {
        markPosition();
        AstNode argument = parseTypeArgument();
        if (expects(ARROW)) {
            AstNode arrow = parse(ARROW);
            AstNode result = parseType();
            return functionType(getSourceLocation(), argument, arrow, result);
        } else {
            getSourceLocation();
            return argument;
        }
    }

    private AstNode parseTypeArgument() {
        markPosition();
        if (expects(OPEN_PAREN)) {
            AstNode openParen = parse(OPEN_PAREN);
            AstNode type = parseType();
            if (expectsComma()) {
                List<AstNode> fieldsAndCommas = new ArrayList<>(asList(tupleTypeField(type.getSourceLocation(), type)));
                while (expectsComma()) {
                    fieldsAndCommas.add(parseComma());
                    fieldsAndCommas.add(parseTupleTypeField());
                }
                AstNode closeParen = parse(CLOSE_PAREN);
                return tupleType(getSourceLocation(), openParen, fieldsAndCommas, closeParen);
            } else {
                AstNode closeParen = parse(CLOSE_PAREN);
                return parenthesized(getSourceLocation(), openParen, type, closeParen);
            }
        } else {
            return parseTypePrimary();
        }
    }

    private AstNode parseTypeContext() {
        markPosition();
        AstNode context = parseQualifiedName();
        AstNode variable = parseVariableType();
        return typeContext(getSourceLocation(), context, variable);
    }

    private AstNode parseTypePrimary() {
        markPosition();
        if (expects(OPEN_PAREN)) {
            AstNode openParen = parse(OPEN_PAREN);
            AstNode type = parseType();
            AstNode closeParen = parse(CLOSE_PAREN);
            return parenthesized(getSourceLocation(), openParen, type, closeParen);
        } else if (expectsSumType()) {
            AstNode name = parseQualifiedName();
            List<AstNode> parameters = new ArrayList<>();
            while (expectsTypePrimary()) {
                parameters.add(parseSumParameter());
            }
            return sumType(getSourceLocation(), name, parameters);
        } else if (expects(OPEN_SQUARE)) {
            AstNode openSquare = parse(OPEN_SQUARE);
            AstNode type = parseType();
            AstNode closeSquare = parse(CLOSE_SQUARE);
            return listType(getSourceLocation(), openSquare, type, closeSquare);
        } else {
            AstNode name = parseName();
            if (expectsTypePrimary()) {
                List<AstNode> parameters = new ArrayList<>();
                while (expectsVariableType()) {
                    parameters.add(parseVariableType());
                }
                return constructorType(getSourceLocation(), name, parameters);
            } else {
                return variableType(getSourceLocation(), name);
            }
        }
    }

    private AstNode parseVariableType() {
        if (expectsVariableType()) {
            markPosition();
            AstNode name = parseName();
            return variableType(getSourceLocation(), name);
        } else {
            throw new UnsupportedOperationException(); // TODO
        }
    }

    private AstNode require(String word, TokenKind tokenKind) {
        if (expects(word)) {
            return terminal(scanner.nextToken().withKind(tokenKind));
        } else {
            throw new RuntimeException(); // TODO
        }
    }

    private Token require(TokenKind tokenKind) {
        if (expects(tokenKind)) {
            return scanner.nextToken();
        } else {
            throw new ParseException("Unexpected " + token().getKind() + "; wanted " + tokenKind, token().getSourceLocation());
        }
    }

    private Token token() {
        return scanner.peekAt(0);
    }

    private AstNode tupleFieldMatch(AstNode fieldMatch) {
        return AstNodes.tupleFieldMatch(fieldMatch.getSourceLocation(), fieldMatch);
    }

    private NamedSourcePoint unmarkPosition() {
        return positions.pop();
    }

    private SourceLocation currentSourceLocation() {
        return positions.peek().to(scanner.getPreviousPosition());
    }
}
