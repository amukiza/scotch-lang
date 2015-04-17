package scotch.compiler.parser;

import static java.lang.Character.isLowerCase;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static scotch.compiler.scanner.Token.TokenKind.ARROW;
import static scotch.compiler.scanner.Token.TokenKind.IS;
import static scotch.compiler.scanner.Token.TokenKind.LAMBDA_SLASH;
import static scotch.compiler.scanner.Token.TokenKind.DRAW_FROM;
import static scotch.compiler.scanner.Token.TokenKind.BOOL;
import static scotch.compiler.scanner.Token.TokenKind.CHAR;
import static scotch.compiler.scanner.Token.TokenKind.COMMA;
import static scotch.compiler.scanner.Token.TokenKind.DEFAULT_OPERATOR;
import static scotch.compiler.scanner.Token.TokenKind.DOT;
import static scotch.compiler.scanner.Token.TokenKind.CONTEXT_ARROW;
import static scotch.compiler.scanner.Token.TokenKind.HAS_TYPE;
import static scotch.compiler.scanner.Token.TokenKind.DOUBLE;
import static scotch.compiler.scanner.Token.TokenKind.EOF;
import static scotch.compiler.scanner.Token.TokenKind.ID;
import static scotch.compiler.scanner.Token.TokenKind.INT;
import static scotch.compiler.scanner.Token.TokenKind.DO;
import static scotch.compiler.scanner.Token.TokenKind.ELSE;
import static scotch.compiler.scanner.Token.TokenKind.IF;
import static scotch.compiler.scanner.Token.TokenKind.THEN;
import static scotch.compiler.scanner.Token.TokenKind.WHERE;
import static scotch.compiler.scanner.Token.TokenKind.OPEN_CURLY;
import static scotch.compiler.scanner.Token.TokenKind.OPEN_PAREN;
import static scotch.compiler.scanner.Token.TokenKind.OPEN_SQUARE;
import static scotch.compiler.scanner.Token.TokenKind.ALTERNATIVE;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_CURLY;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_PAREN;
import static scotch.compiler.scanner.Token.TokenKind.CLOSE_SQUARE;
import static scotch.compiler.scanner.Token.TokenKind.SEMICOLON;
import static scotch.compiler.scanner.Token.TokenKind.STRING;
import static scotch.compiler.syntax.definition.DefinitionEntry.entry;
import static scotch.compiler.syntax.definition.DefinitionGraph.createGraph;
import static scotch.compiler.syntax.pattern.ComplexMatchBuilder.complexMatchBuilder;
import static scotch.compiler.text.TextUtil.repeat;
import static scotch.compiler.util.Pair.pair;
import static scotch.symbol.Symbol.qualified;
import static scotch.symbol.Symbol.splitQualified;
import static scotch.symbol.Symbol.symbol;
import static scotch.symbol.Symbol.unqualified;
import static scotch.symbol.Value.Fixity.LEFT_INFIX;
import static scotch.symbol.Value.Fixity.PREFIX;
import static scotch.symbol.Value.Fixity.RIGHT_INFIX;
import static scotch.symbol.type.Types.fn;
import static scotch.symbol.type.Types.sum;
import static scotch.symbol.type.Types.var;
import static scotch.util.StringUtil.quote;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import scotch.compiler.scanner.Scanner;
import scotch.compiler.scanner.Token;
import scotch.compiler.scanner.Token.TokenKind;
import scotch.compiler.syntax.builder.BuilderUtil;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.definition.ClassDefinition.ClassDefinitionBuilder;
import scotch.compiler.syntax.definition.DataConstructorDefinition;
import scotch.compiler.syntax.definition.DataFieldDefinition;
import scotch.compiler.syntax.definition.DataTypeDefinition;
import scotch.compiler.syntax.definition.Definition;
import scotch.compiler.syntax.definition.DefinitionEntry;
import scotch.compiler.syntax.definition.DefinitionGraph;
import scotch.compiler.syntax.definition.Import;
import scotch.compiler.syntax.definition.ModuleDefinition;
import scotch.compiler.syntax.definition.ModuleImport;
import scotch.compiler.syntax.definition.OperatorDefinition;
import scotch.compiler.syntax.definition.RootDefinition;
import scotch.compiler.syntax.definition.ScopeDefinition;
import scotch.compiler.syntax.definition.UnshuffledDefinition;
import scotch.compiler.syntax.definition.ValueDefinition;
import scotch.compiler.syntax.definition.ValueSignature;
import scotch.compiler.syntax.pattern.CaptureMatch;
import scotch.compiler.syntax.pattern.EqualMatch;
import scotch.compiler.syntax.pattern.IgnorePattern;
import scotch.compiler.syntax.pattern.PatternCase;
import scotch.compiler.syntax.pattern.PatternMatch;
import scotch.compiler.syntax.pattern.StructField;
import scotch.compiler.syntax.pattern.StructMatch;
import scotch.compiler.syntax.pattern.UnshuffledStructMatch;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.Argument;
import scotch.compiler.syntax.value.Conditional;
import scotch.compiler.syntax.value.ConstantReference;
import scotch.compiler.syntax.value.DataConstructor;
import scotch.compiler.syntax.value.DefaultOperator;
import scotch.compiler.syntax.value.FunctionValue;
import scotch.compiler.syntax.value.Identifier;
import scotch.compiler.syntax.value.Initializer;
import scotch.compiler.syntax.value.InitializerField;
import scotch.compiler.syntax.value.Literal;
import scotch.compiler.syntax.value.PatternMatcher;
import scotch.compiler.syntax.value.UnshuffledValue;
import scotch.compiler.syntax.value.Value;
import scotch.compiler.text.NamedSourcePoint;
import scotch.compiler.text.SourceLocation;
import scotch.compiler.util.Pair;
import scotch.symbol.Symbol;
import scotch.symbol.SymbolResolver;
import scotch.symbol.Value.Fixity;
import scotch.symbol.type.SumType;
import scotch.symbol.type.Type;
import scotch.symbol.util.DefaultSymbolGenerator;
import scotch.symbol.util.SymbolGenerator;
import scotch.util.StringUtil;

public class InputParser {

    private static final List<TokenKind> literals = asList(STRING, INT, CHAR, DOUBLE, BOOL);
    private final LookAheadScanner        scanner;
    private final List<DefinitionEntry>   definitions;
    private final Deque<NamedSourcePoint> positions;
    private final SymbolGenerator         symbolGenerator;
    private final Deque<Scope>            scopes;
    private final Deque<List<String>>     memberNames;
    private       String                  currentModule;

    public InputParser(SymbolResolver resolver, Scanner scanner) {
        this.scanner = new LookAheadScanner(scanner);
        this.definitions = new ArrayList<>();
        this.positions = new ArrayDeque<>();
        this.symbolGenerator = new DefaultSymbolGenerator();
        this.scopes = new ArrayDeque<>(asList(Scope.scope(symbolGenerator, resolver)));
        this.memberNames = new ArrayDeque<>(asList(ImmutableList.of()));
    }

    public DefinitionGraph parse() {
        parseRoot();
        return createGraph(definitions)
            .withSequence(symbolGenerator)
            .build();
    }

    private BindExpressionBuilder bindExpression() {
        return new BindExpressionBuilder();
    }

    private DefinitionReference collect(Definition definition) {
        definitions.add(entry(scope(), definition));
        return definition.getReference();
    }

    private DefinitionReference createConstructor(DataConstructorDefinition constructor, SumType type) {
        return scoped(() -> definition(ValueDefinition.builder(),
            value -> {
                Value body = createConstructorBody(constructor, type);
                value.withSymbol(constructor.getSymbol()).withBody(body);
            }
        ));
    }

    private Value createConstructorBody(DataConstructorDefinition constructor, SumType type) {
        if (constructor.isNiladic()) {
            return node(ConstantReference.builder(),
                constant -> constant
                    .withDataType(constructor.getDataType())
                    .withSymbol(constructor.getSymbol())
                    .withConstantField(constructor.getConstantField())
                    .withType(type));
        } else {
            return scoped(() -> node(
                FunctionValue.builder(),
                function -> definition(
                    ScopeDefinition.builder(),
                    scope -> {
                        Symbol symbol = reserveSymbol();
                        scope.withSymbol(symbol);
                        function
                            .withSymbol(symbol)
                            .withArguments(constructor.getFields().stream()
                                .map(DataFieldDefinition::toArgument)
                                .collect(toList()))
                            .withBody(node(DataConstructor.builder(),
                                ctor -> ctor
                                    .withSymbol(constructor.getSymbol())
                                    .withType(type)
                                    .withArguments(constructor.getFields().stream()
                                        .map(DataFieldDefinition::toValue)
                                        .collect(toList()))));
                    })));
        }
    }

    private <D extends Definition, B extends SyntaxBuilder<D>> DefinitionReference definition(B supplier, Consumer<B> consumer) {
        return collect(node(supplier, consumer));
    }

    private boolean expects(TokenKind kind) {
        return expectsAt(0, kind);
    }

    private boolean expectsAt(int offset, TokenKind kind) {
        return scanner.peekAt(offset).is(kind);
    }

    private boolean expectsClassDefinition() {
        return expectsWord("class");
    }

    private boolean expectsDataDefinition() {
        return expectsWord("data");
    }

    private boolean expectsDefinitions() {
        return !expectsModule() && !expects(EOF);
    }

    private boolean expectsIgnoreMatch() {
        return expectsWord("_");
    }

    private boolean expectsImport() {
        return expectsWord("import");
    }

    private boolean expectsLiteral() {
        return literals.stream().anyMatch(this::expects);
    }

    private boolean expectsMatch() {
        return expectsWord()
            || expectsLiteral()
            || expects(OPEN_PAREN);
    }

    private boolean expectsModule() {
        return expectsWord("module");
    }

    private boolean expectsOperatorDefinition() {
        return expectsWord("prefix")
            || ((expectsWord("left") || expectsWord("right")) && expectsWordAt(1, "infix"));
    }

    private boolean expectsValueSignatures() {
        int offset = 0;
        while (!expectsAt(offset, SEMICOLON) && !expectsAt(offset, OPEN_CURLY)) {
            if (expectsAt(offset, ID)) {
                if (expectsAt(offset + 1, COMMA)) {
                    offset += 2;
                } else if (expectsAt(offset + 1, HAS_TYPE)) {
                    return true;
                } else {
                    break;
                }
            } else if (expectsAt(offset, OPEN_PAREN) && expectsAt(offset + 1, ID) && expectsAt(offset + 2, CLOSE_PAREN)) {
                if (expectsAt(offset + 3, COMMA)) {
                    offset += 4;
                } else if (expectsAt(offset + 3, HAS_TYPE)) {
                    return true;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return false;
    }

    private boolean expectsWord() {
        return expectsWordAt(0);
    }

    private boolean expectsWord(String value) {
        return expectsWordAt(0, value);
    }

    private boolean expectsWordAt(int offset) {
        return expectsAt(offset, ID);
    }

    private boolean expectsWordAt(int offset, String value) {
        return expectsAt(offset, ID) && Objects.equals(scanner.peekAt(offset).getValue(), value);
    }

    private SourceLocation getSourceLocation() {
        return unmarkPosition().to(scanner.getPreviousPosition());
    }

    private void markPosition() {
        positions.push(scanner.getPosition());
    }

    private void named(Symbol symbol, Consumer<Symbol> function) {
        memberNames.push(symbol.getMemberNames());
        try {
            function.accept(symbol);
        } finally {
            memberNames.pop();
        }
    }

    private Token nextToken() {
        return scanner.nextToken();
    }

    private <N, B extends SyntaxBuilder<N>> N node(B builder, Consumer<B> consumer) {
        markPosition();
        try {
            consumer.accept(builder);
        } catch (RuntimeException exception) {
            unmarkPosition();
            throw exception;
        }
        return builder.withSourceLocation(getSourceLocation()).build();
    }

    private DataFieldDefinition parseAnonymousField(int offset, Map<String, Type> constraints) {
        if (expects(OPEN_PAREN)) {
            nextToken();
            DataFieldDefinition definition = parseAnonymousField_(offset, constraints);
            require(CLOSE_PAREN);
            return definition;
        } else {
            return parseAnonymousField_(offset, constraints);
        }
    }

    private DataFieldDefinition parseAnonymousField_(int offset, Map<String, Type> constraints) {
        return node(DataFieldDefinition.builder(),
            builder -> builder
                .withOrdinal(offset)
                .withName("_" + offset)
                .withType(parseType(constraints)));
    }

    private List<DataFieldDefinition> parseAnonymousFields(Map<String, Type> constraints) {
        List<DataFieldDefinition> fields = new ArrayList<>();
        int offset = 0;
        while (expectsWord() || expects(OPEN_PAREN)) {
            fields.add(parseAnonymousField(offset++, constraints));
        }
        return fields;
    }

    private Argument parseArgument() {
        return node(Argument.builder(), builder -> builder
            .withName(requireWord())
            .withType(reserveType()));
    }

    private CaptureMatch parseCaptureMatch() {
        return node(CaptureMatch.builder(), builder -> builder.withIdentifier(parseWordReference()));
    }

    private List<Type> parseClassArguments() {
        List<Type> arguments = new ArrayList<>();
        do {
            arguments.add(var(requireWord()));
        } while (expectsWord());
        return arguments;
    }

    private DefinitionReference parseClassDefinition() {
        return definition(new ClassDefinitionBuilder(), builder -> {
            requireWord("class");
            builder.withSymbol(qualify(requireWord()))
                .withArguments(parseClassArguments())
                .withMembers(parseClassMembers());
        });
    }

    private List<DefinitionReference> parseClassMembers() {
        require(WHERE);
        require(OPEN_CURLY);
        List<DefinitionReference> members = new ArrayList<>();
        while (!expects(CLOSE_CURLY)) {
            if (expectsValueSignatures()) {
                parseValueSignatures().forEach(members::add);
            } else {
                members.add(parseValueDefinition());
            }
            requireTerminator();
        }
        require(CLOSE_CURLY);
        return members;
    }

    private PatternMatch parseComplexMatch() {
        return node(complexMatchBuilder(symbolGenerator), match -> {
            require(OPEN_PAREN);
            match.withPatternMatch(parseUnshuffledMatch());
            if (expects(COMMA)) {
                match.tuplize();
                while (expects(COMMA)) {
                    nextToken();
                    match.withPatternMatch(parseUnshuffledMatch());
                }
            }
            require(CLOSE_PAREN);
        });
    }

    private Value parseConditional() {
        return node(Conditional.builder(), conditional -> {
            require(IF);
            conditional.withCondition(parseExpression().collapse());
            require(THEN);
            conditional.withWhenTrue(parseExpression().collapse());
            require(ELSE);
            conditional.withWhenFalse(parseExpression().collapse());
            conditional.withType(reserveType());
        });
    }

    private void parseConstraint(Map<String, List<Symbol>> constraints) {
        Symbol typeClass = symbol(parseQualifiedName());
        String variable = requireWord();
        constraints.computeIfAbsent(variable, k -> new ArrayList<>()).add(typeClass);
    }

    private Map<String, Type> parseConstraints() {
        Map<String, List<Symbol>> constraints = new HashMap<>();
        require(OPEN_PAREN);
        parseConstraint(constraints);
        while (expects(COMMA)) {
            nextToken();
            parseConstraint(constraints);
        }
        require(CLOSE_PAREN);
        require(CONTEXT_ARROW);
        return constraints.entrySet().stream()
            .collect(toMap(Entry::getKey, entry -> var(entry.getKey(), entry.getValue())));
    }

    private DataConstructorDefinition parseDataConstructor(AtomicInteger ordinal, Symbol dataType, Map<String, Type> constraints) {
        return node(DataConstructorDefinition.builder(), builder -> {
            if (expects(OPEN_CURLY)) {
                builder
                    .withOrdinal(ordinal.getAndIncrement())
                    .withSymbol(dataType)
                    .withDataType(dataType);
                parseDataFields(builder, constraints);
            } else {
                builder
                    .withOrdinal(ordinal.getAndIncrement())
                    .withSymbol(qualify(requireWord()))
                    .withDataType(dataType);
                if (expects(OPEN_CURLY)) {
                    parseDataFields(builder, constraints);
                } else {
                    builder.withFields(parseAnonymousFields(constraints));
                }
            }
        });
    }

    private void parseDataFields(DataConstructorDefinition.Builder builder, Map<String, Type> constraints) {
        require(OPEN_CURLY);
        AtomicInteger ordinal = new AtomicInteger();
        builder.addField(parseNamedField(ordinal, constraints));
        while (expects(COMMA) && expectsWordAt(1)) {
            nextToken();
            builder.addField(parseNamedField(ordinal, constraints));
        }
        if (expects(COMMA)) {
            nextToken();
        }
        require(CLOSE_CURLY);
    }

    private List<DefinitionReference> parseDataType() {
        List<DataConstructorDefinition> constructors = new ArrayList<>();
        List<DefinitionReference> definitions = new ArrayList<>();
        definitions.add(0, definition(DataTypeDefinition.builder(), builder -> {
            requireWord("data");
            Map<String, Type> constraints = parseSignatureConstraints();
            List<Type> parameters = new ArrayList<>();
            Symbol symbol = qualify(requireWord());
            builder.withSymbol(symbol);
            while (!expects(IS) && !expects(OPEN_CURLY)) {
                Type parameter = parseType(constraints);
                builder.addParameter(parameter);
                parameters.add(parameter);
            }
            AtomicInteger ordinal = new AtomicInteger();
            if (expects(OPEN_CURLY)) {
                constructors.add(parseDataConstructor(ordinal, symbol, constraints));
            } else {
                require(IS);
                constructors.add(parseDataConstructor(ordinal, symbol, constraints));
                while (expects(ALTERNATIVE)) {
                    nextToken();
                    constructors.add(parseDataConstructor(ordinal, symbol, constraints));
                }
            }
            constructors.forEach(builder::addConstructor);
            constructors.stream()
                .map(constructor -> createConstructor(constructor, sum(symbol, parameters)))
                .forEach(definitions::add);
        }));
        return definitions;
    }

    private DefaultOperator parseDefaultOperator() {
        return node(DefaultOperator.builder(), builder -> builder
            .withSymbol(unqualified(require(DEFAULT_OPERATOR).getValueAs(String.class)))
            .withType(reserveType()));
    }

    private List<DefinitionReference> parseDefinitions() {
        List<DefinitionReference> definitions = new ArrayList<>();
        if (expectsClassDefinition()) {
            definitions.add(parseClassDefinition());
        } else if (expectsOperatorDefinition()) {
            definitions.addAll(parseOperatorDefinition());
        } else if (expectsDataDefinition()) {
            definitions.addAll(parseDataType());
        } else if (expectsValueSignatures()) {
            definitions.addAll(parseValueSignatures());
        } else {
            definitions.add(parseValueDefinition());
        }
        return definitions;
    }

    private void parseDoExpression(List<DoExpression> expressions) {
        if (expectsWord() && expectsAt(1, DRAW_FROM)) {
            expressions.add(scoped(() -> node(bindExpression(),
                bind -> definition(ScopeDefinition.builder(), scope -> {
                    Symbol symbol = reserveSymbol();
                    scope.withSymbol(symbol);
                    bind.withSymbol(symbol);
                    bind.withArgument(parseArgument());
                    require(DRAW_FROM);
                    bind.withValue(parseExpression());
                    requireTerminator();
                    if (!expects(CLOSE_CURLY)) {
                        parseDoExpression(expressions);
                    }
                })
            )));
        } else {
            DoExpression expression = new ThenExpression(parseExpression());
            requireTerminator();
            if (!expects(CLOSE_CURLY)) {
                parseDoExpression(expressions);
            }
            expressions.add(expression);
        }
    }

    private Value parseDoStatement() {
        require(DO);
        require(OPEN_CURLY);
        List<DoExpression> expressions = new ArrayList<>();
        parseDoExpression(expressions);
        require(CLOSE_CURLY);
        List<Value> values = expressions.remove(0).toValue();
        while (!expressions.isEmpty()) {
            expressions.remove(0).toValue(values);
        }
        UnshuffledValue.Builder builder = UnshuffledValue.builder();
        values.forEach(builder::withMember);
        builder.withSourceLocation(SourceLocation.extent(values.stream()
            .map(Value::getSourceLocation)
            .collect(toList())));
        return builder.build();
    }

    private ParseException parseException(String message, SourceLocation sourceLocation) {
        throw new ParseException(message, sourceLocation);
    }

    private Value parseExpression() {
        return node(UnshuffledValue.builder(), builder -> {
            builder.withMember(parseRequiredPrimary());
            Optional<Value> argument = parseOptionalPrimary();
            while (argument.isPresent()) {
                builder.withMember(argument.get());
                argument = parseOptionalPrimary();
            }
        });
    }

    private InitializerField parseField() {
        return node(InitializerField.builder(), builder -> {
            builder.withName(requireWord());
            require(IS);
            builder.withValue(parseExpression().collapse());
        });
    }

    private StructField parseFieldMatch() {
        return node(StructField.builder(), builder -> {
            builder.withFieldName(requireWord());
            builder.withType(reserveType());
            if (expects(IS)) {
                nextToken();
                builder.withPatternMatch(parseRequiredMatch());
            } else {
                builder.withImplicitCapture();
            }
        });
    }

    private List<InitializerField> parseFields() {
        List<InitializerField> fields = new ArrayList<>();
        require(OPEN_CURLY);
        if (expectsWord()) {
            fields.add(parseField());
            while (expects(COMMA) && expectsWordAt(1)) {
                nextToken();
                fields.add(parseField());
            }
            if (expects(COMMA)) {
                nextToken();
            }
        }
        require(CLOSE_CURLY);
        return fields;
    }

    private IgnorePattern parseIgnoreMatch() {
        return node(IgnorePattern.builder(), builder -> {
            requireWord("_");
            builder.withType(reserveType());
        });
    }

    private Import parseImport() {
        return node(ModuleImport.builder(), builder -> {
            requireWord("import");
            builder.withModuleName(parseQualifiedName());
        });
    }

    private List<Import> parseImports() {
        List<Import> imports = new ArrayList<>();
        while (expectsImport()) {
            imports.add(parseImport());
            requireTerminator();
        }
        return imports;
    }

    private Value parseInitializer_(Value value) {
        if (expects(OPEN_CURLY)) {
            return node(Initializer.builder(), builder -> {
                builder.withType(reserveType());
                builder.withValue(value);
                parseFields().forEach(builder::addField);
            });
        } else {
            return value;
        }
    }

    private Value parseList() {
        return node(ListBuilder.builder(symbolGenerator), builder -> {
            require(OPEN_SQUARE);
            if (!expects(CLOSE_SQUARE)) {
                builder.addValue(parseExpression());
                while (expects(COMMA)) {
                    nextToken();
                    if (expects(CLOSE_SQUARE)) {
                        break;
                    } else {
                        builder.addValue(parseExpression());
                    }
                }
            }
            require(CLOSE_SQUARE);
        });
    }

    private Value parseLiteral() {
        return node(Literal.builder(), builder -> {
            if (expects(STRING)) {
                builder.withValue(requireString());
            } else if (expects(INT)) {
                builder.withValue(requireInt());
            } else if (expects(CHAR)) {
                builder.withValue(requireChar());
            } else if (expects(DOUBLE)) {
                builder.withValue(requireDouble());
            } else if (expects(BOOL)) {
                builder.withValue(requireBool());
            } else {
                throw unexpected(literals);
            }
        });
    }

    private Optional<PatternMatch> parseLiteralMatch(boolean required) {
        PatternMatch match = null;
        if (expectsIgnoreMatch()) {
            match = parseIgnoreMatch();
        } else if (expectsWord()) {
            match = parseCaptureMatch();
        } else if (required) {
            throw unexpected(ID);
        }
        return Optional.ofNullable(match);
    }

    private Optional<PatternMatch> parseMatch(boolean required) {
        PatternMatch match = null;
        if (expectsIgnoreMatch()) {
            match = parseIgnoreMatch();
        } else if (expectsWord()) {
            if (expectsAt(1, OPEN_CURLY)) {
                match = parseStructMatch();
            } else if (symbol(peekAt(0).getValueAs(String.class)).isConstructorName()) {
                match = node(EqualMatch.builder(), builder -> builder.withValue(parseWordReference()));
            } else {
                match = parseCaptureMatch();
            }
        } else if (expectsLiteral()) {
            match = node(EqualMatch.builder(), builder -> builder.withValue(parseLiteral()));
        } else if (expects(OPEN_PAREN)) {
            match = parseComplexMatch();
        } else if (required) {
            throw unexpected(ID);
        }
        return Optional.ofNullable(match);
    }

    @SuppressWarnings("unchecked")
    private DefinitionReference parseModule() {
        requireWord("module");
        currentModule = parseQualifiedName();
        requireTerminator();
        List<Import> imports = parseImports();
        return scoped(imports, () -> definition(
            ModuleDefinition.builder(),
            builder -> builder
                .withSymbol(currentModule)
                .withImports(imports)
                .withDefinitions(parseModuleDefinitions())
        ));
    }

    private List<DefinitionReference> parseModuleDefinitions() {
        List<DefinitionReference> definitions = new ArrayList<>();
        while (expectsDefinitions()) {
            definitions.addAll(parseDefinitions());
            requireTerminator();
        }
        return definitions;
    }

    private DataFieldDefinition parseNamedField(AtomicInteger ordinal, Map<String, Type> constraints) {
        if (expects(OPEN_PAREN)) {
            nextToken();
            DataFieldDefinition field = parseNamedField_(ordinal, constraints);
            require(CLOSE_PAREN);
            return field;
        } else {
            return parseNamedField_(ordinal, constraints);
        }
    }

    private DataFieldDefinition parseNamedField_(AtomicInteger ordinal, Map<String, Type> constraints) {
        return node(DataFieldDefinition.builder(), builder -> {
            builder
                .withOrdinal(ordinal.getAndIncrement())
                .withName(requireWord());
            require(HAS_TYPE);
            builder.withType(parseType(constraints));
        });
    }

    private List<DefinitionReference> parseOperatorDefinition() {
        Fixity fixity = parseOperatorFixity();
        int precedence = parseOperatorPrecedence();
        return parseSymbols().stream()
            .map(pair -> pair.into(
                (symbol, sourceLocation) -> OperatorDefinition.builder()
                    .withSourceLocation(sourceLocation)
                    .withSymbol(symbol)
                    .withFixity(fixity)
                    .withPrecedence(precedence)
                    .build()))
            .map(this::collect)
            .collect(toList());
    }

    private Fixity parseOperatorFixity() {
        if (expectsWord("right") && expectsWordAt(1, "infix")) {
            nextToken();
            nextToken();
            return RIGHT_INFIX;
        } else if (expectsWord("left") && expectsWordAt(1, "infix")) {
            nextToken();
            nextToken();
            return LEFT_INFIX;
        } else if (expectsWord("prefix")) {
            nextToken();
            return PREFIX;
        } else {
            throw unexpected(ID, "left infix", "right infix", "prefix");
        }
    }

    private int parseOperatorPrecedence() {
        int precedence = requireInt();
        if (precedence > 20) {
            throw parseException("Can't have operator precedence higher than 20", getSourceLocation());
        } else {
            return precedence;
        }
    }

    private Optional<PatternMatch> parseOptionalLiteralMatch() {
        return parseLiteralMatch(false);
    }

    private Optional<PatternMatch> parseOptionalMatch() {
        return parseMatch(false);
    }

    private Optional<Value> parseOptionalPrimary() {
        return parsePrimary(false);
    }

    private PatternMatcher parsePatternLiteral() {
        return scoped(() -> node(PatternMatcher.builder(),
            patternMatcher -> definition(ScopeDefinition.builder(),
                matcherScope -> named(reserveSymbol(),
                    matcherSymbol -> {
                        matcherScope.withSymbol(matcherSymbol);
                        patternMatcher.withSymbol(matcherSymbol);
                        patternMatcher.withType(reserveType());
                        patternMatcher.withPatterns(asList(scoped(() -> node(PatternCase.builder(),
                            patternCaseBuilder -> definition(ScopeDefinition.builder(),
                                patternCaseScope -> named(reserveSymbol(), patternSymbol -> {
                                    patternCaseScope.withSymbol(patternSymbol);
                                    patternCaseBuilder.withSymbol(patternSymbol);
                                    require(LAMBDA_SLASH);
                                    AtomicInteger counter = new AtomicInteger();
                                    List<PatternMatch> matches = parsePatternLiteralMatches();
                                    List<Argument> arguments = matches.stream()
                                        .map(match -> Argument.builder()
                                            .withName("#" + counter.getAndIncrement())
                                            .withType(reserveType())
                                            .withSourceLocation(match.getSourceLocation())
                                            .build())
                                        .collect(toList());
                                    patternCaseBuilder.withMatches(matches);
                                    patternMatcher.withArguments(arguments);
                                    require(ARROW);
                                    patternCaseBuilder.withBody(parseExpression());
                                })
                            )
                        ))));
                    }
                )
            )
        ));
    }

    private List<PatternMatch> parsePatternLiteralMatches() {
        List<PatternMatch> matches = new ArrayList<>();
        matches.add(parseRequiredLiteralMatch());
        Optional<PatternMatch> match = parseOptionalLiteralMatch();
        while (match.isPresent()) {
            matches.add(match.get());
            match = parseOptionalLiteralMatch();
        }
        return matches;
    }

    private List<PatternMatch> parsePatternMatches() {
        List<PatternMatch> matches = new ArrayList<>();
        matches.add(parseRequiredMatch());
        Optional<PatternMatch> match = parseOptionalMatch();
        while (match.isPresent()) {
            matches.add(match.get());
            match = parseOptionalMatch();
        }
        return matches;
    }

    private Optional<Value> parsePrimary(boolean required) {
        Value value = null;
        if (expectsWord()) {
            value = parseWordReference();
        } else if (expects(DEFAULT_OPERATOR)) {
            value = parseDefaultOperator();
        } else if (expectsLiteral()) {
            value = parseLiteral();
        } else if (expects(OPEN_PAREN)) {
            value = parseTupleOrParenthetical();
        } else if (expects(OPEN_SQUARE)) {
            value = parseList();
        } else if (expects(LAMBDA_SLASH)) {
            value = parsePatternLiteral();
        } else if (expects(DO)) {
            value = parseDoStatement();
        } else if (expects(IF)) {
            value = parseConditional();
        } else if (required) {
            throw unexpected(ImmutableList.<TokenKind>builder().add(ID, OPEN_PAREN).addAll(literals).build());
        }
        return Optional.ofNullable(value).map(this::parseInitializer_);
    }

    private String parseQualifiedName() {
        List<String> parts = new ArrayList<>();
        parts.add(requireWord());
        while (expects(DOT)) {
            nextToken();
            parts.add(requireWord());
        }
        return join(".", parts);
    }

    private PatternMatch parseRequiredLiteralMatch() {
        return parseLiteralMatch(true).get();
    }

    private PatternMatch parseRequiredMatch() {
        return parseMatch(true).get();
    }

    private Value parseRequiredPrimary() {
        return parsePrimary(true).get();
    }

    private void parseRoot() {
        definition(RootDefinition.builder(), builder -> {
            if (!expects(EOF)) {
                builder.withModule(parseModule());
                while (expectsModule()) {
                    builder.withModule(parseModule());
                }
            }
            require(EOF);
        });
    }

    private Map<String, Type> parseSignatureConstraints() {
        if (expects(OPEN_PAREN)) {
            int offset = 0;
            while (!peekAt(offset).is(SEMICOLON)) {
                if (peekAt(offset).is(CLOSE_PAREN) && peekAt(offset + 1).is(CONTEXT_ARROW)) {
                    return parseConstraints();
                } else {
                    offset++;
                }
            }
        }
        return emptyMap();
    }

    private PatternMatch parseStructMatch() {
        return node(StructMatch.builder(), builder -> {
            builder.withConstructor(symbol(requireWord()));
            builder.withType(reserveType());
            require(OPEN_CURLY);
            builder.withField(parseFieldMatch());
            while (expects(COMMA) && expectsWordAt(1)) {
                nextToken();
                builder.withField(parseFieldMatch());
            }
            if (expects(COMMA)) {
                nextToken();
            }
            require(CLOSE_CURLY);
        });
    }

    private Symbol parseSymbol() {
        if (expects(ID)) {
            return qualify(requireMemberName());
        } else if (expects(OPEN_PAREN)) {
            nextToken();
            List<String> memberName = requireMemberName();
            require(CLOSE_PAREN);
            return qualify(memberName);
        } else {
            throw unexpected(asList(ID, OPEN_PAREN));
        }
    }

    private List<Pair<Symbol, SourceLocation>> parseSymbols() {
        List<Pair<Symbol, SourceLocation>> symbols = new ArrayList<>();
        markPosition();
        symbols.add(pair(parseSymbol(), getSourceLocation()));
        while (expects(COMMA)) {
            nextToken();
            markPosition();
            symbols.add(pair(parseSymbol(), getSourceLocation()));
        }
        return symbols;
    }

    private Value parseTupleOrParenthetical() {
        return node(TupleBuilder.builder(), builder -> {
            require(OPEN_PAREN);
            builder.withMember(parseExpression());
            while (expects(COMMA)) {
                nextToken();
                builder.withMember(parseExpression());
            }
            require(CLOSE_PAREN);
            if (builder.hasTooManyMembers()) {
                throw parseException("Tuple can't have more than " + builder.maxMembers() + " members", scanner.peekAt(0).getSourceLocation());
            } else if (builder.isTuple()) {
                builder.withType(reserveType());
                builder.withTupleType(reserveType());
            }
        });
    }

    private Type parseType(Map<String, Type> constraints) {
        return parseType_(
            constraints,
            tryRequire(OPEN_PAREN)
                .map(token -> {
                    List<Type> members = new ArrayList<>();
                    members.add(parseType(constraints));
                    while (expects(COMMA)) {
                        nextToken();
                        members.add(parseType(constraints));
                    }
                    require(CLOSE_PAREN);
                    Type type;
                    if (members.size() == 1) {
                        type = members.get(0);
                    } else {
                        type = sum("scotch.data.tuple.(" + repeat(",", members.size() - 1) + ")", members);
                    }
                    return type;
                })
                .orElseGet(() -> parseTypePrimary(constraints)));
    }

    private Type parseTypePrimary(Map<String, Type> constraints) {
        if (expects(OPEN_SQUARE)) {
            require(OPEN_SQUARE);
            Type type = sum("scotch.data.list.[]", asList(parseTypePrimary(constraints)));
            require(CLOSE_SQUARE);
            return type;
        } else {
            return splitQualified(parseQualifiedName()).into((optionalModuleName, memberName) -> {
                try {
                    markPosition();
                    if (isLowerCase(memberName.charAt(0))) {
                        if (optionalModuleName.isPresent()) {
                            throw parseException("Type name must be uppercase", peekSourceLocation());
                        } else {
                            return constraints.getOrDefault(memberName, var(memberName));
                        }
                    } else {
                        return parseTypePrimary_(optionalModuleName, memberName, constraints);
                    }
                } finally {
                    positions.pop();
                }
            });
        }
    }

    private Type parseTypePrimary_(Optional<String> optionalModuleName, String memberName, Map<String, Type> constraints) {
        List<Type> parameters = new ArrayList<>();
        while (expectsWord() || expects(OPEN_PAREN)) {
            parameters.add(parseType(constraints));
        }
        return optionalModuleName
            .map(moduleName -> (Type) sum(qualified(moduleName, memberName), parameters))
            .orElseGet(() -> sum(unqualified(memberName), parameters));
    }

    private Type parseType_(Map<String, Type> constraints, Type type) {
        if (expects(ARROW)) {
            nextToken();
            return fn(type, parseType(constraints));
        } else {
            return type;
        }
    }

    private PatternMatch parseUnshuffledMatch() {
        return node(UnshuffledStructMatch.builder(), builder -> {
            builder.withType(reserveType());
            builder.withPatternMatch(parseRequiredMatch());
            while (expectsMatch()) {
                builder.withPatternMatch(parseRequiredMatch());
            }
        });
    }

    private DefinitionReference parseValueDefinition() {
        if (expectsAt(1, IS)) {
            return scoped(() -> definition(ValueDefinition.builder(),
                builder -> named(qualify(requireMemberName()),
                    symbol -> {
                        builder.withSymbol(symbol);
                        require(IS);
                        builder.withBody(parseExpression());
                    })));
        } else {
            return scoped(() -> definition(UnshuffledDefinition.builder(), builder -> {
                builder.withMatches(parsePatternMatches());
                require(IS);
                builder.withSymbol(reserveSymbol())
                    .withBody(parseExpression());
            }));
        }
    }

    private Type parseValueSignature() {
        require(HAS_TYPE);
        return parseType(parseSignatureConstraints());
    }

    private List<DefinitionReference> parseValueSignatures() {
        List<Pair<Symbol, SourceLocation>> symbolList = parseSymbols();
        Type type = parseValueSignature();
        return symbolList.stream()
            .map(pair -> pair.into(
                (symbol, sourceLocation) -> ValueSignature.builder()
                    .withSymbol(symbol)
                    .withType(type)
                    .withSourceLocation(sourceLocation)
                    .build()))
            .map(this::collect)
            .collect(toList());
    }

    private Identifier parseWordReference() {
        return node(Identifier.builder(), builder -> builder
                .withSymbol(unqualified(requireWord()))
                .withType(reserveType())
        );
    }

    private Token peekAt(int offset) {
        return scanner.peekAt(offset);
    }

    private SourceLocation peekSourceLocation() {
        return positions.peek().to(scanner.getPosition());
    }

    private Symbol qualify(String memberName) {
        return qualified(currentModule, memberName);
    }

    private Symbol qualify(List<String> memberName) {
        return qualified(currentModule, memberName);
    }

    private Token require(TokenKind kind) {
        return requireAt(0, kind);
    }

    private Token requireAt(int offset, TokenKind kind) {
        if (expectsAt(offset, kind)) {
            Token token = peekAt(offset);
            nextToken();
            return token;
        } else {
            throw unexpected(kind);
        }
    }

    private Boolean requireBool() {
        return require(BOOL).getValueAs(Boolean.class);
    }

    private Character requireChar() {
        return require(CHAR).getValueAs(Character.class);
    }

    private Double requireDouble() {
        return require(DOUBLE).getValueAs(Double.class);
    }

    private int requireInt() {
        return require(INT).getValueAs(Integer.class);
    }

    private List<String> requireMemberName() {
        return ImmutableList.<String>builder()
            .addAll(memberNames.peek())
            .add(requireWord())
            .build();
    }

    private String requireString() {
        return require(STRING).getValueAs(String.class);
    }

    private void requireTerminator() {
        if (expects(SEMICOLON)) {
            while (expects(SEMICOLON)) {
                nextToken();
            }
        } else {
            throw unexpected(SEMICOLON);
        }
    }

    private void requireWord(String value) {
        requireWordAt(0, value);
    }

    private String requireWord() {
        return requireWordAt(0);
    }

    private String requireWordAt(int offset, String value) {
        if (expectsWordAt(offset, value)) {
            return requireWordAt(offset);
        } else {
            throw unexpected(ID, value);
        }
    }

    private String requireWordAt(int offset) {
        if (expectsAt(offset, ID)) {
            String word = peekAt(offset).getValueAs(String.class);
            nextToken();
            return word;
        } else {
            throw unexpected(ID);
        }
    }

    private Symbol reserveSymbol() {
        return scope().reserveSymbol(memberNames.peek());
    }

    private Type reserveType() {
        return scope().reserveType();
    }

    private Scope scope() {
        return scopes.peek();
    }

    private <T> T scoped(List<Import> imports, Supplier<T> supplier) {
        return scoped(Optional.of(imports), supplier);
    }

    private <T> T scoped(Supplier<T> supplier) {
        return scoped(Optional.empty(), supplier);
    }

    private <T> T scoped(Optional<List<Import>> optionalImports, Supplier<T> supplier) {
        scopes.push(optionalImports
            .map(imports -> scope().enterScope(currentModule, imports))
            .orElseGet(scope()::enterScope));
        try {
            return supplier.get();
        } finally {
            scopes.pop();
        }
    }

    private Optional<Token> tryRequire(TokenKind tokenKind) {
        if (expects(tokenKind)) {
            return Optional.of(nextToken());
        } else {
            return Optional.empty();
        }
    }

    private ParseException unexpected(TokenKind wantedKind) {
        return parseException(
            "Unexpected " + scanner.peekAt(0).getKind()
                + "; wanted " + wantedKind,
            scanner.peekAt(0).getSourceLocation()
        );
    }

    private ParseException unexpected(TokenKind wantedKind, Object wantedValue) {
        return parseException(
            "Unexpected " + scanner.peekAt(0).getKind() + " with value " + quote(scanner.peekAt(0).getValue())
                + "; wanted " + wantedKind + " with value " + quote(wantedValue),
            scanner.peekAt(0).getSourceLocation()
        );
    }

    private ParseException unexpected(TokenKind wantedKind, Object... wantedValues) {
        return parseException(
            "Unexpected " + scanner.peekAt(0).getKind() + " with value " + quote(scanner.peekAt(0).getValue())
                + "; wanted " + wantedKind + " with one value of"
                + " [" + join(", ", stream(wantedValues).map(StringUtil::quote).collect(toList())) + "]",
            scanner.peekAt(0).getSourceLocation()
        );
    }

    private ParseException unexpected(List<TokenKind> wantedKinds) {
        return parseException(
            "Unexpected " + scanner.peekAt(0).getKind()
                + "; wanted one of [" + join(", ", wantedKinds.stream().map(Object::toString).collect(toList())) + "]",
            scanner.peekAt(0).getSourceLocation()
        );
    }

    private NamedSourcePoint unmarkPosition() {
        return positions.pop();
    }

    @AllArgsConstructor
    private class BindExpression extends DoExpression {

        @NonNull private final SourceLocation sourceLocation;
        @NonNull private final Symbol         symbol;
        @NonNull private final Argument       argument;
        @NonNull private final Value          value;

        @Override
        public List<Value> toValue() {
            throw new ParseException("Unexpected bind in do-notation", value.getSourceLocation());
        }

        @Override
        public void toValue(List<Value> values) {
            List<Value> outerValues = new ArrayList<Value>() {{
                UnshuffledValue.Builder unshuffled = UnshuffledValue.builder();
                values.forEach(unshuffled::withMember);
                unshuffled.withSourceLocation(SourceLocation.extent(values.stream()
                    .map(Value::getSourceLocation)
                    .collect(toList())));
                add(value);
                add(Identifier.builder()
                    .withSymbol(qualified("scotch.control.monad", ">>="))
                    .withType(reserveType())
                    .withSourceLocation(value.getSourceLocation().getEndPoint())
                    .build());
                add(FunctionValue.builder()
                    .withSymbol(symbol)
                    .withArguments(asList(argument))
                    .withBody(unshuffled.build())
                    .withSourceLocation(sourceLocation)
                    .build());
            }};
            values.clear();
            values.addAll(outerValues);
        }
    }

    private final class BindExpressionBuilder implements SyntaxBuilder<BindExpression> {

        private Optional<SourceLocation> sourceLocation;
        private Optional<Symbol>         symbol;
        private Optional<Argument>       argument;
        private Optional<Value>          value;

        @Override
        public BindExpression build() {
            return new BindExpression(
                BuilderUtil.require(sourceLocation, "Source location"),
                BuilderUtil.require(symbol, "Symbol"),
                BuilderUtil.require(argument, "Argument"),
                BuilderUtil.require(value, "Value")
            );
        }

        public BindExpressionBuilder withArgument(Argument argument) {
            this.argument = Optional.of(argument);
            return this;
        }

        @Override
        public BindExpressionBuilder withSourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = Optional.of(sourceLocation);
            return this;
        }

        public BindExpressionBuilder withSymbol(Symbol symbol) {
            this.symbol = Optional.of(symbol);
            return this;
        }

        public BindExpressionBuilder withValue(Value value) {
            this.value = Optional.of(value);
            return this;
        }
    }

    private abstract class DoExpression {

        public abstract List<Value> toValue();

        public abstract void toValue(List<Value> values);
    }

    @AllArgsConstructor
    private class ThenExpression extends DoExpression {

        @NonNull private final Value value;

        @Override
        public List<Value> toValue() {
            return new ArrayList<>(asList(value));
        }

        @Override
        public void toValue(List<Value> values) {
            values.add(0, value);
            values.add(1, Identifier.builder()
                .withSymbol(qualified("scotch.control.monad", ">>"))
                .withType(reserveType())
                .withSourceLocation(value.getSourceLocation().getEndPoint())
                .build());
        }
    }
}
