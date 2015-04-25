package scotch.compiler.parser;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static scotch.compiler.ast.TestUtil.bool;
import static scotch.compiler.ast.TestUtil.captureArgument;
import static scotch.compiler.ast.TestUtil.char_;
import static scotch.compiler.ast.TestUtil.classArgument;
import static scotch.compiler.ast.TestUtil.classDefinition;
import static scotch.compiler.ast.TestUtil.classMembers;
import static scotch.compiler.ast.TestUtil.conditional;
import static scotch.compiler.ast.TestUtil.constantArgument;
import static scotch.compiler.ast.TestUtil.constructorArgument;
import static scotch.compiler.ast.TestUtil.constructorType;
import static scotch.compiler.ast.TestUtil.context;
import static scotch.compiler.ast.TestUtil.contextType;
import static scotch.compiler.ast.TestUtil.dataConstant;
import static scotch.compiler.ast.TestUtil.dataRecord;
import static scotch.compiler.ast.TestUtil.dataRecordField;
import static scotch.compiler.ast.TestUtil.dataTuple;
import static scotch.compiler.ast.TestUtil.dataTupleField;
import static scotch.compiler.ast.TestUtil.dataType;
import static scotch.compiler.ast.TestUtil.doStatement;
import static scotch.compiler.ast.TestUtil.doStatements;
import static scotch.compiler.ast.TestUtil.double_;
import static scotch.compiler.ast.TestUtil.drawFromStatement;
import static scotch.compiler.ast.TestUtil.emptyNamedFields;
import static scotch.compiler.ast.TestUtil.expression;
import static scotch.compiler.ast.TestUtil.functionType;
import static scotch.compiler.ast.TestUtil.ignoreArgument;
import static scotch.compiler.ast.TestUtil.importScope;
import static scotch.compiler.ast.TestUtil.importStatement;
import static scotch.compiler.ast.TestUtil.importStatements;
import static scotch.compiler.ast.TestUtil.import_;
import static scotch.compiler.ast.TestUtil.infixOperator;
import static scotch.compiler.ast.TestUtil.initializer;
import static scotch.compiler.ast.TestUtil.initializerField;
import static scotch.compiler.ast.TestUtil.instanceDefinition;
import static scotch.compiler.ast.TestUtil.instanceMembers;
import static scotch.compiler.ast.TestUtil.integer;
import static scotch.compiler.ast.TestUtil.listLiteral;
import static scotch.compiler.ast.TestUtil.listType;
import static scotch.compiler.ast.TestUtil.literal;
import static scotch.compiler.ast.TestUtil.literalArgument;
import static scotch.compiler.ast.TestUtil.module;
import static scotch.compiler.ast.TestUtil.moduleMember;
import static scotch.compiler.ast.TestUtil.moduleMembers;
import static scotch.compiler.ast.TestUtil.namedFieldImplicitCapture;
import static scotch.compiler.ast.TestUtil.namedFieldMatch;
import static scotch.compiler.ast.TestUtil.namedFieldMatches;
import static scotch.compiler.ast.TestUtil.operator;
import static scotch.compiler.ast.TestUtil.operatorDefinition;
import static scotch.compiler.ast.TestUtil.operatorFixity;
import static scotch.compiler.ast.TestUtil.parenthesized;
import static scotch.compiler.ast.TestUtil.pattern;
import static scotch.compiler.ast.TestUtil.patternArguments;
import static scotch.compiler.ast.TestUtil.patternLiteral;
import static scotch.compiler.ast.TestUtil.patternName;
import static scotch.compiler.ast.TestUtil.patternSignature;
import static scotch.compiler.ast.TestUtil.prefixOperator;
import static scotch.compiler.ast.TestUtil.primary;
import static scotch.compiler.ast.TestUtil.qualified;
import static scotch.compiler.ast.TestUtil.reference;
import static scotch.compiler.ast.TestUtil.string;
import static scotch.compiler.ast.TestUtil.sumType;
import static scotch.compiler.ast.TestUtil.terminal;
import static scotch.compiler.ast.TestUtil.tupleArgument;
import static scotch.compiler.ast.TestUtil.tupleFieldMatch;
import static scotch.compiler.ast.TestUtil.tupleLiteral;
import static scotch.compiler.ast.TestUtil.tupleType;
import static scotch.compiler.ast.TestUtil.tupleTypeField;
import static scotch.compiler.ast.TestUtil.typeContext;
import static scotch.compiler.ast.TestUtil.typeSignature;
import static scotch.compiler.ast.TestUtil.unshuffledArgument;
import static scotch.compiler.ast.TestUtil.variableType;
import static scotch.compiler.scanner.Token.TokenKind.PIPE;
import static scotch.compiler.scanner.Token.TokenKind.ARROW;
import static scotch.compiler.scanner.Token.TokenKind.BOOL;
import static scotch.compiler.scanner.Token.TokenKind.CHAR;
import static scotch.compiler.scanner.Token.TokenKind.CLASS;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_CURLY;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_PAREN;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_SQUARE;
import static scotch.compiler.scanner.Token.TokenKind.COMMA;
import static scotch.compiler.scanner.Token.TokenKind.DOUBLE_ARROW;
import static scotch.compiler.scanner.Token.TokenKind.DATA;
import static scotch.compiler.scanner.Token.TokenKind.DO;
import static scotch.compiler.scanner.Token.TokenKind.DOT;
import static scotch.compiler.scanner.Token.TokenKind.DOUBLE;
import static scotch.compiler.scanner.Token.TokenKind.BACKWARDS_ARROW;
import static scotch.compiler.scanner.Token.TokenKind.ELSE;
import static scotch.compiler.scanner.Token.TokenKind.DOUBLE_COLON;
import static scotch.compiler.scanner.Token.TokenKind.ID;
import static scotch.compiler.scanner.Token.TokenKind.IF;
import static scotch.compiler.scanner.Token.TokenKind.IMPORT;
import static scotch.compiler.scanner.Token.TokenKind.INFIX;
import static scotch.compiler.scanner.Token.TokenKind.INSTANCE;
import static scotch.compiler.scanner.Token.TokenKind.INT;
import static scotch.compiler.scanner.Token.TokenKind.EQUALS;
import static scotch.compiler.scanner.Token.TokenKind.BACKSLASH;
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
import static scotch.compiler.scanner.Token.TokenKind.UNDERSCORE;
import static scotch.compiler.scanner.Token.TokenKind.WHERE;

import java.net.URI;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import scotch.compiler.ast.AstNode;
import scotch.compiler.ast.ModulesNode;
import scotch.compiler.scanner.Scanner;

public class AstParserTest {

    @Rule public final TestName testName = new TestName();
    @Rule public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldParseQualifiedName() {
        assertThat(parse("scotch.test.name").parseQualifiedName(), is(
            qualified(asList(
                terminal(ID, "scotch"),
                terminal(DOT, "."),
                terminal(ID, "test"),
                terminal(DOT, "."),
                terminal(ID, "name")
            ))
        ));
    }

    @Test
    public void shouldParseImport() {
        AstNode import_ = parse("import scotch.data.list").parseImport();
        assertThat(import_, is(
            import_(
                terminal(IMPORT, "import"),
                qualified(asList(
                    terminal(ID, "scotch"),
                    terminal(DOT, "."),
                    terminal(ID, "data"),
                    terminal(DOT, "."),
                    terminal(ID, "list")
                ))
            )
        ));
    }

    @Test
    public void shouldParseImportStatement() {
        AstNode import_ = parse("import scotch.data.list\n").parseImportStatement();
        assertThat(import_, is(
            importStatement(
                import_(
                    terminal(IMPORT, "import"),
                    qualified(asList(
                        terminal(ID, "scotch"),
                        terminal(DOT, "."),
                        terminal(ID, "data"),
                        terminal(DOT, "."),
                        terminal(ID, "list")
                    ))
                ),
                terminal(SEMICOLON, ";")
            )
        ));
    }

    @Test
    public void shouldParseImports() {
        AstNode imports = parse(
            "import scotch.data.list",
            "",
            "import scotch.test"
        ).parseImportStatements();
        assertThat(imports, is(
            importStatements(asList(
                importStatement(
                    import_(
                        terminal(IMPORT, "import"),
                        qualified(asList(
                            terminal(ID, "scotch"),
                            terminal(DOT, "."),
                            terminal(ID, "data"),
                            terminal(DOT, "."),
                            terminal(ID, "list")
                        ))
                    ),
                    terminal(SEMICOLON, ";")
                ),
                importStatement(
                    import_(
                        terminal(IMPORT, "import"),
                        qualified(asList(
                            terminal(ID, "scotch"),
                            terminal(DOT, "."),
                            terminal(ID, "test")
                        ))
                    ),
                    terminal(SEMICOLON, ";")
                )
            ))
        ));
    }

    @Test
    public void shouldParseModule() {
        AstNode module = parse(
            "module scotch.test",
            "import scotch.data.list",
            "three = 2 + 1"
        ).parseModule();
        assertThat(module, is(
            module(
                terminal(MODULE, "module"),
                qualified(asList(
                    terminal(ID, "scotch"),
                    terminal(DOT, "."),
                    terminal(ID, "test")
                )),
                terminal(SEMICOLON, ";"),
                importScope(
                    importStatements(asList(
                        importStatement(
                            import_(
                                terminal(IMPORT, "import"),
                                qualified(asList(
                                    terminal(ID, "scotch"),
                                    terminal(DOT, "."),
                                    terminal(ID, "data"),
                                    terminal(DOT, "."),
                                    terminal(ID, "list")
                                ))
                            ),
                            terminal(SEMICOLON, ";")
                        )
                    )),
                    moduleMembers(asList(
                        moduleMember(
                            pattern(
                                patternArguments(asList(
                                    captureArgument(terminal(ID, "three"))
                                )),
                                terminal(EQUALS, "="),
                                expression(asList(
                                    primary(literal(integer(terminal(INT, 2)))),
                                    primary(reference(qualified(terminal(ID, "+")))),
                                    primary(literal(integer(terminal(INT, 1))))
                                ))
                            ),
                            terminal(SEMICOLON, ";")
                        )
                    ))
                )
            )
        ));
    }

    @Test
    public void shouldParseLiteralMatch() {
        AstNode pattern = parse("1").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                literalArgument(literal(integer(terminal(INT, 1))))
            ))
        ));
    }

    @Test
    public void shouldParseConstantMatch() {
        AstNode pattern = parse("Nothing").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                constantArgument(qualified(terminal(ID, "Nothing")))
            ))
        ));
    }

    @Test
    public void shouldParseConstructorMatchWithFields() {
        AstNode pattern = parse("Node { left = l, right = r }").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                constructorArgument(
                    qualified(terminal(ID, "Node")),
                    namedFieldMatches(
                        terminal(OPEN_CURLY, "{"),
                        asList(
                            namedFieldMatch(terminal(ID, "left"), terminal(EQUALS, "="), captureArgument(terminal(ID, "l"))),
                            terminal(COMMA, ","),
                            namedFieldMatch(terminal(ID, "right"), terminal(EQUALS, "="), captureArgument(terminal(ID, "r")))
                        ),
                        terminal(CLOSE_CURLY, "}")
                    )
                )
            ))
        ));
    }

    @Test
    public void shouldParseConstructorMatchWithTrailingComma() {
        AstNode pattern = parse("Node { left = l, right = r, }").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                constructorArgument(
                    qualified(terminal(ID, "Node")),
                    namedFieldMatches(
                        terminal(OPEN_CURLY, "{"),
                        asList(
                            namedFieldMatch(terminal(ID, "left"), terminal(EQUALS, "="), captureArgument(terminal(ID, "l"))),
                            terminal(COMMA, ","),
                            namedFieldMatch(terminal(ID, "right"), terminal(EQUALS, "="), captureArgument(terminal(ID, "r"))),
                            terminal(COMMA, ",")
                        ),
                        terminal(CLOSE_CURLY, "}")
                    )
                )
            ))
        ));
    }

    @Test
    public void shouldParseConstructorMatchWithoutFields() {
        AstNode pattern = parse("Node {}").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                constructorArgument(
                    qualified(terminal(ID, "Node")),
                    emptyNamedFields(
                        terminal(OPEN_CURLY, "{"),
                        terminal(CLOSE_CURLY, "}")
                    )
                )
            ))
        ));
    }

    @Test
    public void shouldParseImplicitCapture() {
        AstNode pattern = parse("Node { left, right = 0 }").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                constructorArgument(
                    qualified(terminal(ID, "Node")),
                    namedFieldMatches(
                        terminal(OPEN_CURLY, "{"),
                        asList(
                            namedFieldImplicitCapture(terminal(ID, "left")),
                            terminal(COMMA, ","),
                            namedFieldMatch(terminal(ID, "right"), terminal(EQUALS, "="), literalArgument(literal(integer(terminal(INT, 0)))))
                        ),
                        terminal(CLOSE_CURLY, "}")
                    )
                )
            ))
        ));
    }

    @Test
    public void shouldParseTuplePattern() {
        AstNode pattern = parse("(a, _)").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                tupleArgument(
                    terminal(OPEN_PAREN, "("),
                    asList(
                        tupleFieldMatch(captureArgument(terminal(ID, "a"))),
                        terminal(COMMA, ","),
                        tupleFieldMatch(ignoreArgument(terminal(UNDERSCORE, "_")))
                    ),
                    terminal(CLOSE_PAREN, ")")
                )
            ))
        ));
    }

    @Test
    public void shouldParseParenthesizedPattern() {
        AstNode pattern = parse("(a)").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                parenthesized(
                    terminal(OPEN_PAREN, "("),
                    captureArgument(terminal(ID, "a")),
                    terminal(CLOSE_PAREN, ")")
                )
            ))
        ));
    }

    @Test
    public void shouldParsePatternWithNoParameters() {
        AstNode pattern = parse("three = 2 + 1").parsePattern();
        assertThat(pattern, is(
            pattern(
                patternArguments(asList(
                    captureArgument(terminal(ID, "three"))
                )),
                terminal(EQUALS, "="),
                expression(asList(
                    primary(literal(integer(terminal(INT, 2)))),
                    primary(reference(qualified(terminal(ID, "+")))),
                    primary(literal(integer(terminal(INT, 1))))
                ))
            )
        ));
    }

    @Test
    public void shouldParseUnshuffledPattern() {
        AstNode pattern = parse("(Node _ left right)").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                parenthesized(
                    terminal(OPEN_PAREN, "("),
                    unshuffledArgument(asList(
                        constantArgument(qualified(terminal(ID, "Node"))),
                        ignoreArgument(terminal(UNDERSCORE, "_")),
                        captureArgument(terminal(ID, "left")),
                        captureArgument(terminal(ID, "right"))
                    )),
                    terminal(CLOSE_PAREN, ")")
                )
            ))
        ));
    }

    @Test
    public void shouldParseUnshuffledPatternInTuple() {
        AstNode pattern = parse("(_, Node _ left right)").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                tupleArgument(
                    terminal(OPEN_PAREN, "("),
                    asList(
                        tupleFieldMatch(ignoreArgument(terminal(UNDERSCORE, "_"))),
                        terminal(COMMA, ","),
                        tupleFieldMatch(unshuffledArgument(asList(
                            constantArgument(qualified(terminal(ID, "Node"))),
                            ignoreArgument(terminal(UNDERSCORE, "_")),
                            captureArgument(terminal(ID, "left")),
                            captureArgument(terminal(ID, "right"))
                        )))
                    ),
                    terminal(CLOSE_PAREN, ")")
                )
            ))
        ));
    }

    @Test
    public void shouldParseUnshuffledPatternInConstructorField() {
        AstNode pattern = parse("Node { left = Node _ l r, right }").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                constructorArgument(
                    qualified(terminal(ID, "Node")),
                    namedFieldMatches(
                        terminal(OPEN_CURLY, "{"),
                        asList(
                            namedFieldMatch(terminal(ID, "left"), terminal(EQUALS, "="), unshuffledArgument(asList(
                                constantArgument(qualified(terminal(ID, "Node"))),
                                ignoreArgument(terminal(UNDERSCORE, "_")),
                                captureArgument(terminal(ID, "l")),
                                captureArgument(terminal(ID, "r"))
                            ))),
                            terminal(COMMA, ","),
                            namedFieldImplicitCapture(terminal(ID, "right"))
                        ),
                        terminal(CLOSE_CURLY, "}")
                    )
                )
            ))
        ));
    }

    @Test
    public void shouldParseLeftInfixOperatorDefinition() {
        AstNode operator = parse("left infix 7 (+)").parseOperatorDefinition();
        assertThat(operator, is(
            operatorDefinition(
                operatorFixity(infixOperator(terminal(LEFT, "left"), terminal(INFIX, "infix"))),
                terminal(INT, 7),
                asList(
                    operator(parenthesized(
                        terminal(OPEN_PAREN, "("),
                        terminal(ID, "+"),
                        terminal(CLOSE_PAREN, ")")
                    ))
                )
            )
        ));
    }

    @Test
    public void shouldParseRightInfixOperatorDefinition() {
        AstNode operator = parse("right infix 0 ($)").parseOperatorDefinition();
        assertThat(operator, is(
            operatorDefinition(
                operatorFixity(infixOperator(terminal(RIGHT, "right"), terminal(INFIX, "infix"))),
                terminal(INT, 0),
                asList(
                    operator(parenthesized(
                        terminal(OPEN_PAREN, "("),
                        terminal(ID, "$"),
                        terminal(CLOSE_PAREN, ")")
                    ))
                )
            )
        ));
    }

    @Test
    public void shouldParsePrefixOperatorDefinition() {
        AstNode operator = parse("prefix 10 (-)").parseOperatorDefinition();
        assertThat(operator, is(
            operatorDefinition(
                operatorFixity(prefixOperator(terminal(PREFIX, "prefix"))),
                terminal(INT, 10),
                asList(
                    operator(parenthesized(
                        terminal(OPEN_PAREN, "("),
                        terminal(ID, "-"),
                        terminal(CLOSE_PAREN, ")")
                    ))
                )
            )
        ));
    }

    @Test
    public void shouldParseMultipleOperatorDefinitions() {
        AstNode operator = parse("left infix 7 (+), sub").parseOperatorDefinition();
        assertThat(operator, is(
            operatorDefinition(
                operatorFixity(infixOperator(terminal(LEFT, "left"), terminal(INFIX, "infix"))),
                terminal(INT, 7),
                asList(
                    operator(parenthesized(
                        terminal(OPEN_PAREN, "("),
                        terminal(ID, "+"),
                        terminal(CLOSE_PAREN, ")")
                    )),
                    terminal(COMMA, ","),
                    operator(terminal(ID, "sub"))
                )
            )
        ));
    }

    @Test
    public void shouldParsePatternSignature() {
        AstNode signature = parse("($) :: (a -> b) -> a -> b").parsePatternSignature();
        assertThat(signature, is(
            patternSignature(
                asList(
                    patternName(parenthesized(
                        terminal(OPEN_PAREN, "("),
                        terminal(ID, "$"),
                        terminal(CLOSE_PAREN, ")")
                    ))
                ),
                terminal(DOUBLE_COLON, "::"),
                typeSignature(
                    functionType(
                        parenthesized(
                            terminal(OPEN_PAREN, "("),
                            functionType(
                                variableType(terminal(ID, "a")),
                                terminal(ARROW, "->"),
                                variableType(terminal(ID, "b"))
                            ),
                            terminal(CLOSE_PAREN, ")")
                        ),
                        terminal(ARROW, "->"),
                        functionType(
                            variableType(terminal(ID, "a")),
                            terminal(ARROW, "->"),
                            variableType(terminal(ID, "b"))
                        )
                    )
                )
            )
        ));
    }

    @Test
    public void shouldParseMultiplePatternSignature() {
        AstNode signature = parse("(==), (/=) :: a -> a -> Bool").parsePatternSignature();
        assertThat(signature, is(
            patternSignature(
                asList(
                    patternName(parenthesized(
                        terminal(OPEN_PAREN, "("),
                        terminal(ID, "=="),
                        terminal(CLOSE_PAREN, ")")
                    )),
                    terminal(COMMA, ","),
                    patternName(parenthesized(
                        terminal(OPEN_PAREN, "("),
                        terminal(ID, "/="),
                        terminal(CLOSE_PAREN, ")")
                    ))
                ),
                terminal(DOUBLE_COLON, "::"),
                typeSignature(
                    functionType(
                        variableType(terminal(ID, "a")),
                        terminal(ARROW, "->"),
                        functionType(
                            variableType(terminal(ID, "a")),
                            terminal(ARROW, "->"),
                            sumType(qualified(terminal(ID, "Bool")))
                        )
                    )
                )
            )
        ));
    }

    @Test
    public void shouldParseParameterizedSumInTypeSignature() {
        AstNode signature = parse("List a -> List (a -> b)").parseTypeSignature();
        assertThat(signature, is(
            typeSignature(
                functionType(
                    sumType(qualified(terminal(ID, "List")), asList(
                        variableType(terminal(ID, "a"))
                    )),
                    terminal(ARROW, "->"),
                    sumType(qualified(terminal(ID, "List")), asList(
                        parenthesized(
                            terminal(OPEN_PAREN, "("),
                            functionType(
                                variableType(terminal(ID, "a")),
                                terminal(ARROW, "->"),
                                variableType(terminal(ID, "b"))
                            ),
                            terminal(CLOSE_PAREN, ")")
                        )
                    ))
                )
            )
        ));
    }

    @Test
    public void shouldParseMultiParameterizedSumInTypeSignature() {
        AstNode signature = parse("Map a b -> a -> b").parseTypeSignature();
        assertThat(signature, is(
            typeSignature(
                functionType(
                    sumType(qualified(terminal(ID, "Map")), asList(
                        variableType(terminal(ID, "a")),
                        variableType(terminal(ID, "b"))
                    )),
                    terminal(ARROW, "->"),
                    functionType(
                        variableType(terminal(ID, "a")),
                        terminal(ARROW, "->"),
                        variableType(terminal(ID, "b"))
                    )
                )
            )
        ));
    }

    @Test
    public void shouldParseParameterizedVariableInTypeSignature() {
        AstNode signature = parse("m a -> (a -> m b) -> m b").parseTypeSignature();
        assertThat(signature, is(
            typeSignature(
                functionType(
                    constructorType(terminal(ID, "m"), asList(
                        variableType(terminal(ID, "a"))
                    )),
                    terminal(ARROW, "->"),
                    functionType(
                        parenthesized(
                            terminal(OPEN_PAREN, "("),
                            functionType(
                                variableType(terminal(ID, "a")),
                                terminal(ARROW, "->"),
                                constructorType(terminal(ID, "m"), asList(
                                    variableType(terminal(ID, "b"))
                                ))
                            ),
                            terminal(CLOSE_PAREN, ")")
                        ),
                        terminal(ARROW, "->"),
                        constructorType(terminal(ID, "m"), asList(
                            variableType(terminal(ID, "b"))
                        ))
                    )
                )
            )
        ));
    }

    @Test
    public void shouldParseTypeSignatureWithContext() {
        AstNode signature = parse("(Eq a) => a -> a -> Bool").parseTypeSignature();
        assertThat(signature, is(
            typeSignature(
                contextType(
                    context(
                        terminal(OPEN_PAREN, "("),
                        asList(
                            typeContext(qualified(terminal(ID, "Eq")), variableType(terminal(ID, "a")))
                        ),
                        terminal(CLOSE_PAREN, ")"),
                        terminal(DOUBLE_ARROW, "=>")
                    ),
                    functionType(
                        variableType(terminal(ID, "a")),
                        terminal(ARROW, "->"),
                        functionType(
                            variableType(terminal(ID, "a")),
                            terminal(ARROW, "->"),
                            sumType(qualified(terminal(ID, "Bool")))
                        )
                    )
                )
            )
        ));
    }

    @Test
    public void shouldParseTupleInTypeSignature() {
        AstNode signature = parse("(a, b) -> b").parseTypeSignature();
        assertThat(signature, is(
            typeSignature(
                functionType(
                    tupleType(
                        terminal(OPEN_PAREN, "("),
                        asList(
                            tupleTypeField(variableType(terminal(ID, "a"))),
                            terminal(COMMA, ","),
                            tupleTypeField(variableType(terminal(ID, "b")))
                        ),
                        terminal(CLOSE_PAREN, ")")
                    ),
                    terminal(ARROW, "->"),
                    variableType(terminal(ID, "b"))
                )
            )
        ));
    }

    @Test
    public void shouldParseDataWithConstants() {
        AstNode data = parse("data Color = Red | Green | Blue").parseDataType();
        assertThat(data, is(
            dataType(
                terminal(DATA, "data"),
                terminal(ID, "Color"),
                terminal(EQUALS, "="),
                asList(
                    dataConstant(terminal(ID, "Red")),
                    terminal(PIPE, "|"),
                    dataConstant(terminal(ID, "Green")),
                    terminal(PIPE, "|"),
                    dataConstant(terminal(ID, "Blue"))
                )
            )
        ));
    }

    @Test
    public void shouldParseDataWithTupleConstructors() {
        AstNode data = parse("data IntTree = Nil | Node Int IntTree IntTree").parseDataType();
        assertThat(data, is(
            dataType(
                terminal(DATA, "data"),
                terminal(ID, "IntTree"),
                terminal(EQUALS, "="),
                asList(
                    dataConstant(terminal(ID, "Nil")),
                    terminal(PIPE, "|"),
                    dataTuple(terminal(ID, "Node"), asList(
                        dataTupleField(sumType(qualified(terminal(ID, "Int")))),
                        dataTupleField(sumType(qualified(terminal(ID, "IntTree")))),
                        dataTupleField(sumType(qualified(terminal(ID, "IntTree"))))
                    ))
                )
            )
        ));
    }

    @Test
    public void shouldParseDataWithNamedFieldConstructors() {
        AstNode data = parse(
            "data IntTree = Nil",
            "             | Node { value :: Int,",
            "                      left :: IntTree,",
            "                      right :: IntTree }"
        ).parseDataType();
        assertThat(data, is(
            dataType(
                terminal(DATA, "data"),
                terminal(ID, "IntTree"),
                terminal(EQUALS, "="),
                asList(
                    dataConstant(terminal(ID, "Nil")),
                    terminal(PIPE, "|"),
                    dataRecord(
                        terminal(ID, "Node"),
                        terminal(OPEN_CURLY, "{"),
                        asList(
                            dataRecordField(terminal(ID, "value"), terminal(DOUBLE_COLON, "::"), sumType(qualified(terminal(ID, "Int")))),
                            terminal(COMMA, ","),
                            dataRecordField(terminal(ID, "left"), terminal(DOUBLE_COLON, "::"), sumType(qualified(terminal(ID, "IntTree")))),
                            terminal(COMMA, ","),
                            dataRecordField(terminal(ID, "right"), terminal(DOUBLE_COLON, "::"), sumType(qualified(terminal(ID, "IntTree"))))
                        ),
                        terminal(CLOSE_CURLY, "}")
                    )
                )
            )
        ));
    }

    @Test
    public void shouldParseGenericDataType() {
        AstNode data = parse("data Map a b = Nil | Node a b (Map a b) (Map a b)").parseDataType();
        assertThat(data, is(
            dataType(
                terminal(DATA, "data"),
                terminal(ID, "Map"),
                asList(
                    variableType(terminal(ID, "a")),
                    variableType(terminal(ID, "b"))
                ),
                terminal(EQUALS, "="),
                asList(
                    dataConstant(terminal(ID, "Nil")),
                    terminal(PIPE, "|"),
                    dataTuple(terminal(ID, "Node"), asList(
                        dataTupleField(variableType(terminal(ID, "a"))),
                        dataTupleField(variableType(terminal(ID, "b"))),
                        dataTupleField(parenthesized(
                            terminal(OPEN_PAREN, "("),
                            sumType(qualified(terminal(ID, "Map")), asList(
                                variableType(terminal(ID, "a")),
                                variableType(terminal(ID, "b"))
                            )),
                            terminal(CLOSE_PAREN, ")")
                        )),
                        dataTupleField(parenthesized(
                            terminal(OPEN_PAREN, "("),
                            sumType(qualified(terminal(ID, "Map")), asList(
                                variableType(terminal(ID, "a")),
                                variableType(terminal(ID, "b"))
                            )),
                            terminal(CLOSE_PAREN, ")")
                        ))
                    ))
                )
            )
        ));
    }

    @Test
    public void shouldParseClassDefinition() {
        AstNode classDef = parse(
            "class Eq a where",
            "    (==), (/=) :: a -> a -> Bool",
            "    a == b = a /= b",
            "    a /= b = a == b"
        ).parseClassDefinition();
        assertThat(classDef, is(
            classDefinition(
                terminal(CLASS, "class"),
                terminal(ID, "Eq"),
                asList(classArgument(terminal(ID, "a"))),
                terminal(WHERE, "where"),
                classMembers(
                    terminal(OPEN_CURLY, "{"),
                    asList(
                        patternSignature(
                            asList(
                                patternName(parenthesized(
                                    terminal(OPEN_PAREN, "("),
                                    terminal(ID, "=="),
                                    terminal(CLOSE_PAREN, ")")
                                )),
                                terminal(COMMA, ","),
                                patternName(parenthesized(
                                    terminal(OPEN_PAREN, "("),
                                    terminal(ID, "/="),
                                    terminal(CLOSE_PAREN, ")")
                                ))
                            ),
                            terminal(DOUBLE_COLON, "::"),
                            typeSignature(
                                functionType(
                                    variableType(terminal(ID, "a")),
                                    terminal(ARROW, "->"),
                                    functionType(
                                        variableType(terminal(ID, "a")),
                                        terminal(ARROW, "->"),
                                        sumType(qualified(terminal(ID, "Bool")))
                                    )
                                )
                            )
                        ),
                        terminal(SEMICOLON, ";"),
                        pattern(
                            patternArguments(asList(
                                captureArgument(terminal(ID, "a")),
                                captureArgument(terminal(ID, "==")),
                                captureArgument(terminal(ID, "b"))
                            )),
                            terminal(EQUALS, "="),
                            expression(asList(
                                primary(reference(qualified(terminal(ID, "a")))),
                                primary(reference(qualified(terminal(ID, "/=")))),
                                primary(reference(qualified(terminal(ID, "b"))))
                            ))
                        ),
                        terminal(SEMICOLON, ";"),
                        pattern(
                            patternArguments(asList(
                                captureArgument(terminal(ID, "a")),
                                captureArgument(terminal(ID, "/=")),
                                captureArgument(terminal(ID, "b"))
                            )),
                            terminal(EQUALS, "="),
                            expression(asList(
                                primary(reference(qualified(terminal(ID, "a")))),
                                primary(reference(qualified(terminal(ID, "==")))),
                                primary(reference(qualified(terminal(ID, "b"))))
                            ))
                        ),
                        terminal(SEMICOLON, ";"),
                        terminal(SEMICOLON, ";")
                    ),
                    terminal(CLOSE_CURLY, "}")
                )
            )
        ));
    }

    @Test
    public void shouldParseClassDefinitionWithContext() {
        AstNode classDef = parse(
            "class (Eq a) => Num a where",
            "    (+), (-) :: a -> a -> a"
        ).parseClassDefinition();
        assertThat(classDef, is(
            classDefinition(
                terminal(CLASS, "class"),
                context(
                    terminal(OPEN_PAREN, "("),
                    asList(typeContext(qualified(terminal(ID, "Eq")), variableType(terminal(ID, "a")))),
                    terminal(CLOSE_PAREN, ")"),
                    terminal(DOUBLE_ARROW, "=>")
                ),
                terminal(ID, "Num"),
                asList(classArgument(terminal(ID, "a"))),
                terminal(WHERE, "where"),
                classMembers(
                    terminal(OPEN_CURLY, "{"),
                    asList(
                        patternSignature(
                            asList(
                                patternName(parenthesized(
                                    terminal(OPEN_PAREN, "("),
                                    terminal(ID, "+"),
                                    terminal(CLOSE_PAREN, ")")
                                )),
                                terminal(COMMA, ","),
                                patternName(parenthesized(
                                    terminal(OPEN_PAREN, "("),
                                    terminal(ID, "-"),
                                    terminal(CLOSE_PAREN, ")")
                                ))
                            ),
                            terminal(DOUBLE_COLON, "::"),
                            typeSignature(
                                functionType(
                                    variableType(terminal(ID, "a")),
                                    terminal(ARROW, "->"),
                                    functionType(
                                        variableType(terminal(ID, "a")),
                                        terminal(ARROW, "->"),
                                        variableType(terminal(ID, "a"))
                                    )
                                )
                            )
                        ),
                        terminal(SEMICOLON, ";"),
                        terminal(SEMICOLON, ";")
                    ),
                    terminal(CLOSE_CURLY, "}")
                )
            )
        ));
    }

    @Test
    public void shouldParseInstanceDefinition() {
        AstNode instanceDef = parse(
            "instance Eq Int where",
            "    (==) = intEq?"
        ).parseInstanceDefinition();
        assertThat(instanceDef, is(
            instanceDefinition(
                terminal(INSTANCE, "instance"),
                terminal(ID, "Eq"),
                asList(sumType(qualified(terminal(ID, "Int")))),
                terminal(WHERE, "where"),
                instanceMembers(
                    terminal(OPEN_CURLY, "{"),
                    asList(
                        pattern(
                            patternArguments(asList(
                                parenthesized(
                                    terminal(OPEN_PAREN, "("),
                                    captureArgument(terminal(ID, "==")),
                                    terminal(CLOSE_PAREN, ")")
                                )
                            )),
                            terminal(EQUALS, "="),
                            expression(asList(
                                primary(reference(qualified(terminal(ID, "intEq?"))))
                            ))
                        ),
                        terminal(SEMICOLON, ";"),
                        terminal(SEMICOLON, ";")
                    ),
                    terminal(CLOSE_CURLY, "}")
                )
            )
        ));
    }

    @Test
    public void shouldParseEmptyModules() {
        AstNode modules = parse("").parseModules();
        assertThat(((ModulesNode) modules).getModules(), is(empty()));
    }

    @Test
    public void shouldParseMultipleModules() {
        AstNode modules = parse(
            "module a.b.c",
            "module d.e.f"
        ).parseModules();
        assertThat(((ModulesNode) modules).getModules(), contains(
            module(
                terminal(MODULE, "module"),
                qualified(asList(terminal(ID, "a"), terminal(DOT, "."), terminal(ID, "b"), terminal(DOT, "."), terminal(ID, "c"))),
                terminal(SEMICOLON, ";"),
                importScope(
                    importStatements(emptyList()),
                    moduleMembers(emptyList())
                )
            ),
            module(
                terminal(MODULE, "module"),
                qualified(asList(terminal(ID, "d"), terminal(DOT, "."), terminal(ID, "e"), terminal(DOT, "."), terminal(ID, "f"))),
                terminal(SEMICOLON, ";"),
                importScope(
                    importStatements(emptyList()),
                    moduleMembers(emptyList())
                )
            )
        ));
    }

    @Test
    public void shouldParseIntegerLiteral() {
        AstNode literal = parse("3").parseLiteral();
        assertThat(literal, is(literal(integer(terminal(INT, 3)))));
    }

    @Test
    public void shouldParseDoubleLiteral() {
        AstNode literal = parse("3.32").parseLiteral();
        assertThat(literal, is(literal(double_(terminal(DOUBLE, 3.32)))));
    }

    @Test
    public void shouldParseStringLiteral() {
        AstNode literal = parse("\"hello!\"").parseLiteral();
        assertThat(literal, is(literal(string(terminal(STRING, "hello!")))));
    }

    @Test
    public void shouldParseBoolLiteral() {
        AstNode literal = parse("True").parseLiteral();
        assertThat(literal, is(literal(bool(terminal(BOOL, true)))));
    }

    @Test
    public void shouldParseCharLiteral() {
        AstNode literal = parse("'a'").parseLiteral();
        assertThat(literal, is(literal(char_(terminal(CHAR, 'a')))));
    }

    @Test
    public void shouldParseTupleLiteral() {
        AstNode tuple = parse("(1, 2, 3)").parseExpression();
        assertThat(tuple, is(
            expression(asList(
                primary(
                    tupleLiteral(
                        terminal(OPEN_PAREN, "("),
                        asList(
                            expression(asList(primary(literal(integer(terminal(INT, 1)))))),
                            terminal(COMMA, ","),
                            expression(asList(primary(literal(integer(terminal(INT, 2)))))),
                            terminal(COMMA, ","),
                            expression(asList(primary(literal(integer(terminal(INT, 3))))))
                        ),
                        terminal(CLOSE_PAREN, ")")
                    )
                )
            ))
        ));
    }

    @Test
    public void shouldNotParseTupleLiteralWithMoreThan12Members() {
        exception.expect(ParseException.class);
        exception.expectMessage("Tuples cannot exceed 12 members in size [test://shouldNotParseTupleLiteralWithMoreThan12Members (1, 1), (1, 44)]");
        parse("(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)").parseExpression();
    }

    @Test
    public void shouldParseDoNotation() {
        AstNode pattern = parse(
            "printed = do",
            "    println \"Hello Clinic!\"",
            "    println \"Debilitating coffee addiction\""
        ).parsePattern();
        assertThat(pattern, is(
            pattern(
                patternArguments(asList(
                    captureArgument(terminal(ID, "printed"))
                )),
                terminal(EQUALS, "="),
                expression(asList(
                    primary(
                        doStatements(
                            terminal(DO, "do"),
                            terminal(OPEN_CURLY, "{"),
                            asList(
                                doStatement(
                                    expression(asList(
                                        primary(reference(qualified(terminal(ID, "println")))),
                                        primary(literal(string(terminal(STRING, "Hello Clinic!"))))
                                    )),
                                    terminal(SEMICOLON, ";")
                                ),
                                doStatement(
                                    expression(asList(
                                        primary(reference(qualified(terminal(ID, "println")))),
                                        primary(literal(string(terminal(STRING, "Debilitating coffee addiction"))))
                                    )),
                                    terminal(SEMICOLON, ";")
                                ),
                                terminal(SEMICOLON, ";")
                            ),
                            terminal(CLOSE_CURLY, "}")
                        )
                    )
                ))
            )
        ));
    }

    @Test
    public void shouldParseDoNotationWithDrawFrom() {
        AstNode pattern = parse(
            "pingpong = do",
            "    ping <- readln",
            "    println (\"ponging back! \" ++ ping)"
        ).parsePattern();
        assertThat(pattern, is(
            pattern(
                patternArguments(asList(
                    captureArgument(terminal(ID, "pingpong"))
                )),
                terminal(EQUALS, "="),
                expression(asList(
                    primary(
                        doStatements(
                            terminal(DO, "do"),
                            terminal(OPEN_CURLY, "{"),
                            asList(
                                drawFromStatement(
                                    patternArguments(asList(
                                        captureArgument(terminal(ID, "ping"))
                                    )),
                                    terminal(BACKWARDS_ARROW, "<-"),
                                    expression(asList(primary(reference(qualified(terminal(ID, "readln")))))),
                                    terminal(SEMICOLON, ";")
                                ),
                                doStatement(
                                    expression(asList(
                                        primary(reference(qualified(terminal(ID, "println")))),
                                        primary(parenthesized(
                                            terminal(OPEN_PAREN, "("),
                                            expression(asList(
                                                primary(literal(string(terminal(STRING, "ponging back! ")))),
                                                primary(reference(qualified(terminal(ID, "++")))),
                                                primary(reference(qualified(terminal(ID, "ping"))))
                                            )),
                                            terminal(CLOSE_PAREN, ")")
                                        ))
                                    )),
                                    terminal(SEMICOLON, ";")
                                ),
                                terminal(SEMICOLON, ";")
                            ),
                            terminal(CLOSE_CURLY, "}")
                        )
                    )
                ))
            )
        ));
    }

    @Test
    public void shouldParseListLiteral() {
        AstNode expression = parse("[1, 2, 3]").parseExpression();
        assertThat(expression, is(
            expression(asList(
                primary(listLiteral(
                    terminal(OPEN_SQUARE, "["),
                    asList(
                        expression(asList(primary(literal(integer(terminal(INT, 1)))))),
                        terminal(COMMA, ","),
                        expression(asList(primary(literal(integer(terminal(INT, 2)))))),
                        terminal(COMMA, ","),
                        expression(asList(primary(literal(integer(terminal(INT, 3))))))
                    ),
                    terminal(CLOSE_SQUARE, "]")
                ))
            ))
        ));
    }

    @Test
    public void shouldParseEmptyList() {
        AstNode expression = parse("[]").parseExpression();
        assertThat(expression, is(
            expression(asList(primary(reference(qualified(terminal(ID, "[]"))))))
        ));
    }

    @Test
    public void shouldParseListWithTrailingComma() {
        AstNode expression = parse("[1, 2, 3,]").parseExpression();
        assertThat(expression, is(
            expression(asList(
                primary(listLiteral(
                    terminal(OPEN_SQUARE, "["),
                    asList(
                        expression(asList(primary(literal(integer(terminal(INT, 1)))))),
                        terminal(COMMA, ","),
                        expression(asList(primary(literal(integer(terminal(INT, 2)))))),
                        terminal(COMMA, ","),
                        expression(asList(primary(literal(integer(terminal(INT, 3)))))),
                        terminal(COMMA, ",")
                    ),
                    terminal(CLOSE_SQUARE, "]")
                ))
            ))
        ));
    }

    @Test
    public void shouldParseListTypeSignature() {
        AstNode signature = parse("[a] -> a").parseTypeSignature();
        assertThat(signature, is(
            typeSignature(
                functionType(
                    listType(
                        terminal(OPEN_SQUARE, "["),
                        variableType(terminal(ID, "a")),
                        terminal(CLOSE_SQUARE, "]")
                    ),
                    terminal(ARROW, "->"),
                    variableType(terminal(ID, "a"))
                )
            )
        ));
    }

    @Test
    public void shouldParseListDestructuringPattern() {
        AstNode pattern = parse("(_:xs)").parsePatternArguments();
        assertThat(pattern, is(
            patternArguments(asList(
                parenthesized(
                    terminal(OPEN_PAREN, "("),
                    unshuffledArgument(asList(
                        ignoreArgument(terminal(UNDERSCORE, "_")),
                        constantArgument(qualified(terminal(ID, ":"))),
                        captureArgument(terminal(ID, "xs"))
                    )),
                    terminal(CLOSE_PAREN, ")")
                )
            ))
        ));
    }

    @Test
    public void shouldParseConditional() {
        AstNode conditional = parse("if True then this value else that value").parseExpression();
        assertThat(conditional, is(
            expression(asList(
                primary(
                    conditional(
                        terminal(IF, "if"),
                        expression(asList(primary(literal(bool(terminal(BOOL, true)))))),
                        terminal(THEN, "then"),
                        expression(asList(
                            primary(reference(qualified(terminal(ID, "this")))),
                            primary(reference(qualified(terminal(ID, "value"))))
                        )),
                        terminal(ELSE, "else"),
                        expression(asList(
                            primary(reference(qualified(terminal(ID, "that")))),
                            primary(reference(qualified(terminal(ID, "value"))))
                        ))
                    )
                )
            ))
        ));
    }

    @Test
    public void shouldParseChainedConditional() {
        AstNode conditional = parse(
            "value = if one condition",
            "        then this value",
            "        else if two condition",
            "        then that value",
            "        else something entirely different"
        ).parsePattern();
        assertThat(conditional, is(
            pattern(
                patternArguments(asList(captureArgument(terminal(ID, "value")))),
                terminal(EQUALS, "="),
                expression(asList(
                    primary(
                        conditional(
                            terminal(IF, "if"),
                            expression(asList(
                                primary(reference(qualified(terminal(ID, "one")))),
                                primary(reference(qualified(terminal(ID, "condition"))))
                            )),
                            terminal(THEN, "then"),
                            expression(asList(
                                primary(reference(qualified(terminal(ID, "this")))),
                                primary(reference(qualified(terminal(ID, "value"))))
                            )),
                            terminal(ELSE, "else"),
                            expression(asList(primary(conditional(
                                terminal(IF, "if"),
                                expression(asList(
                                    primary(reference(qualified(terminal(ID, "two")))),
                                    primary(reference(qualified(terminal(ID, "condition"))))
                                )),
                                terminal(THEN, "then"),
                                expression(asList(
                                    primary(reference(qualified(terminal(ID, "that")))),
                                    primary(reference(qualified(terminal(ID, "value"))))
                                )),
                                terminal(ELSE, "else"),
                                expression(asList(
                                    primary(reference(qualified(terminal(ID, "something")))),
                                    primary(reference(qualified(terminal(ID, "entirely")))),
                                    primary(reference(qualified(terminal(ID, "different"))))
                                ))
                            ))))
                        )
                    )
                ))
            )
        ));
    }

    @Test
    public void shouldParseFunctionLiteral() {
        AstNode function = parse("\\a -> a").parseExpression();
        assertThat(function, is(
            expression(asList(
                primary(
                    patternLiteral(
                        terminal(BACKSLASH, "\\"),
                        patternArguments(asList(
                            captureArgument(terminal(ID, "a"))
                        )),
                        terminal(ARROW, "->"),
                        expression(asList(primary(reference(qualified(terminal(ID, "a"))))))
                    )
                )
            ))
        ));
    }

    @Test
    public void shouldParseFunctionLiteralWithPattern() {
        AstNode function = parse("\\(_, b) -> b").parseExpression();
        assertThat(function, is(
            expression(asList(
                primary(
                    patternLiteral(
                        terminal(BACKSLASH, "\\"),
                        patternArguments(asList(
                            tupleArgument(
                                terminal(OPEN_PAREN, "("),
                                asList(
                                    tupleFieldMatch(ignoreArgument(terminal(UNDERSCORE, "_"))),
                                    terminal(COMMA, ","),
                                    tupleFieldMatch(captureArgument(terminal(ID, "b")))
                                ),
                                terminal(CLOSE_PAREN, ")")
                            )
                        )),
                        terminal(ARROW, "->"),
                        expression(asList(primary(reference(qualified(terminal(ID, "b"))))))
                    )
                )
            ))
        ));
    }

    @Test
    public void shouldNotParseEqualsMatchInFunctionLiteral() {
        exception.expect(ParseException.class);
        exception.expectMessage("Unexpected INT [test://shouldNotParseEqualsMatchInFunctionLiteral (1, 2), (1, 3)]");
        parse("\\1 b -> b").parseExpression();
    }

    @Test
    public void shouldParseOperatorDefinitionAsModuleMember() {
        AstNode moduleMember = parse("right infix 0 ($)\n").parseModuleMember();
        assertThat(moduleMember, is(
            moduleMember(
                operatorDefinition(
                    operatorFixity(infixOperator(terminal(RIGHT, "right"), terminal(INFIX, "infix"))),
                    terminal(INT, 0),
                    asList(
                        operator(parenthesized(
                            terminal(OPEN_PAREN, "("),
                            terminal(ID, "$"),
                            terminal(CLOSE_PAREN, ")")
                        ))
                    )
                ),
                terminal(SEMICOLON, ";")
            )
        ));
    }

    @Test
    public void shouldParseDataDefinitionAsModuleMember() {
        AstNode moduleMember = parse("data Color = Red | Green | Blue").parseModuleMember();
        assertThat(moduleMember, is(
            moduleMember(
                dataType(
                    terminal(DATA, "data"),
                    terminal(ID, "Color"),
                    terminal(EQUALS, "="),
                    asList(
                        dataConstant(terminal(ID, "Red")),
                        terminal(PIPE, "|"),
                        dataConstant(terminal(ID, "Green")),
                        terminal(PIPE, "|"),
                        dataConstant(terminal(ID, "Blue"))
                    )
                ),
                terminal(SEMICOLON, ";")
            )
        ));
    }

    @Test
    public void shouldParsePatternSignatureAsModuleMember() {
        AstNode moduleMember = parse("($) :: (a -> b) -> a -> b").parseModuleMember();
        assertThat(moduleMember, is(
            moduleMember(
                patternSignature(
                    asList(
                        patternName(parenthesized(
                            terminal(OPEN_PAREN, "("),
                            terminal(ID, "$"),
                            terminal(CLOSE_PAREN, ")")
                        ))
                    ),
                    terminal(DOUBLE_COLON, "::"),
                    typeSignature(
                        functionType(
                            parenthesized(
                                terminal(OPEN_PAREN, "("),
                                functionType(
                                    variableType(terminal(ID, "a")),
                                    terminal(ARROW, "->"),
                                    variableType(terminal(ID, "b"))
                                ),
                                terminal(CLOSE_PAREN, ")")
                            ),
                            terminal(ARROW, "->"),
                            functionType(
                                variableType(terminal(ID, "a")),
                                terminal(ARROW, "->"),
                                variableType(terminal(ID, "b"))
                            )
                        )
                    )
                ),
                terminal(SEMICOLON, ";")
            )
        ));
    }

    @Test
    public void shouldParseInstanceDefinitionAsModuleMember() {
        AstNode moduleMember = parse(
            "instance Eq Int where",
            "    (==) = intEq?"
        ).parseModuleMember();
        assertThat(moduleMember, is(
            moduleMember(
                instanceDefinition(
                    terminal(INSTANCE, "instance"),
                    terminal(ID, "Eq"),
                    asList(sumType(qualified(terminal(ID, "Int")))),
                    terminal(WHERE, "where"),
                    instanceMembers(
                        terminal(OPEN_CURLY, "{"),
                        asList(
                            pattern(
                                patternArguments(asList(
                                    parenthesized(
                                        terminal(OPEN_PAREN, "("),
                                        captureArgument(terminal(ID, "==")),
                                        terminal(CLOSE_PAREN, ")")
                                    )
                                )),
                                terminal(EQUALS, "="),
                                expression(asList(
                                    primary(reference(qualified(terminal(ID, "intEq?"))))
                                ))
                            ),
                            terminal(SEMICOLON, ";"),
                            terminal(SEMICOLON, ";")
                        ),
                        terminal(CLOSE_CURLY, "}")
                    )
                ),
                terminal(SEMICOLON, ";")
            )
        ));
    }

    @Test
    public void shouldParseClassDefinitionAsModuleMember() {
        AstNode moduleMember = parse(
            "class Eq a where",
            "    (==) :: a -> a -> Bool"
        ).parseModuleMember();
        assertThat(moduleMember, is(
            moduleMember(
                classDefinition(
                    terminal(CLASS, "class"),
                    terminal(ID, "Eq"),
                    asList(classArgument(terminal(ID, "a"))),
                    terminal(WHERE, "where"),
                    classMembers(
                        terminal(OPEN_CURLY, "{"),
                        asList(
                            patternSignature(
                                asList(
                                    patternName(parenthesized(
                                        terminal(OPEN_PAREN, "("),
                                        terminal(ID, "=="),
                                        terminal(CLOSE_PAREN, ")")
                                    ))
                                ),
                                terminal(DOUBLE_COLON, "::"),
                                typeSignature(
                                    functionType(
                                        variableType(terminal(ID, "a")),
                                        terminal(ARROW, "->"),
                                        functionType(
                                            variableType(terminal(ID, "a")),
                                            terminal(ARROW, "->"),
                                            sumType(qualified(terminal(ID, "Bool")))
                                        )
                                    )
                                )
                            ),
                            terminal(SEMICOLON, ";"),
                            terminal(SEMICOLON, ";")
                        ),
                        terminal(CLOSE_CURLY, "}")
                    )
                ),
                terminal(SEMICOLON, ";")
            )
        ));
    }

    @Test
    public void shouldParseRecordInitializer() {
        AstNode initializer = parse(
            "Entry { key = \"bananas\",",
            "        value = \"oranges\", }"
        ).parseExpression();
        assertThat(initializer, is(
            expression(asList(
                primary(initializer(
                    reference(qualified(terminal(ID, "Entry"))),
                    terminal(OPEN_CURLY, "{"),
                    asList(
                        initializerField(terminal(ID, "key"), terminal(EQUALS, "="), expression(asList(primary(literal(string(terminal(STRING, "bananas"))))))),
                        terminal(COMMA, ","),
                        initializerField(terminal(ID, "value"), terminal(EQUALS, "="), expression(asList(primary(literal(string(terminal(STRING, "oranges"))))))),
                        terminal(COMMA, ",")
                    ),
                    terminal(CLOSE_CURLY, "}")))
            ))
        ));
    }

    @Test
    public void shouldParseDotFunction() {
        AstNode dotFunction = parse("f . x").parseExpression();
        assertThat(dotFunction, is(
            expression(asList(
                primary(reference(qualified(terminal(ID, "f")))),
                primary(reference(qualified(terminal(ID, ".")))),
                primary(reference(qualified(terminal(ID, "x"))))
            ))
        ));
    }

    private AstParser parse(String... data) {
        return new AstParser(Scanner.forString(URI.create("test://" + testName.getMethodName()), String.join("\n", data)));
    }
}
