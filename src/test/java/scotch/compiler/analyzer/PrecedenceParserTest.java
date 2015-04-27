package scotch.compiler.analyzer;

import static java.util.Arrays.asList;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.text.SourceLocation.source;
import static scotch.compiler.text.SourcePoint.point;
import static scotch.compiler.util.TestUtil.access;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.capture;
import static scotch.compiler.util.TestUtil.equal;
import static scotch.compiler.util.TestUtil.field;
import static scotch.compiler.util.TestUtil.id;
import static scotch.compiler.util.TestUtil.ignore;
import static scotch.compiler.util.TestUtil.literal;
import static scotch.compiler.util.TestUtil.matcher;
import static scotch.compiler.util.TestUtil.pattern;
import static scotch.compiler.util.TestUtil.struct;
import static scotch.symbol.Operator.operator;
import static scotch.symbol.Symbol.symbol;
import static scotch.symbol.SymbolEntry.mutableEntry;
import static scotch.symbol.Value.Fixity.RIGHT_INFIX;
import static scotch.symbol.descriptor.DataFieldDescriptor.field;
import static scotch.symbol.type.Types.sum;
import static scotch.symbol.type.Types.t;
import static scotch.symbol.type.Types.var;

import java.util.function.Function;
import org.junit.Test;
import scotch.compiler.Compiler;
import scotch.compiler.IsolatedCompilerTest;
import scotch.compiler.analyzer.PrecedenceParser.ArityMismatch;
import scotch.compiler.syntax.StubResolver;
import scotch.compiler.syntax.definition.DefinitionGraph;
import scotch.symbol.SymbolEntry;
import scotch.symbol.descriptor.DataConstructorDescriptor;

public class PrecedenceParserTest extends IsolatedCompilerTest {

    @Test
    public void shouldShuffleTwoPlusTwo() {
        compile(
            "module scotch.test",
            "left infix 7 (+)",
            "four = 2 + 2"
        );
        shouldNotHaveErrors();
        shouldHaveValue("scotch.test.four", apply(
            apply(
                id("scotch.test.(+)", t(0)),
                literal(2),
                t(1)
            ),
            literal(2),
            t(2)
        ));
    }

    @Test
    public void shouldShufflePattern() {
        compile(
            "module scotch.test",
            "right infix 1 ($)",
            "x $ y = x y"
        );
        shouldNotHaveErrors();
        shouldHaveValue("scotch.test.($)", matcher("scotch.test.($#0)", t(7), asList(arg("#0", t(5)), arg("#1", t(6))), pattern(
            "scotch.test.($#0#0)",
            asList(capture(arg("#0", t(8)), "x", t(0)), capture(arg("#1", t(9)), "y", t(2))),
            apply(id("x", t(3)), id("y", t(4)), t(10))
        )));
    }

    @Test
    public void shouldTranslateEqualMatchToUseEq() {
        compile(
            "module scotch.test",
            "fib 0 = 0"
        );
        shouldHaveValue("scotch.test.fib", matcher("scotch.test.(fib#0)", t(2), arg("#0", t(1)), pattern(
            "scotch.test.fib#0#0",
            asList(equal(arg("#0", t(3)), literal(0))),
            literal(0)
        )));
    }

    @Test
    public void shouldReportArityMismatchInPattern() {
        compile(
            "module scotch.test",
            "fn a b = a b",
            "fn a b c = a c"
        );
        shouldHaveErrors(new ArityMismatch(symbol("scotch.test.(fn#0)"), 2, 3,
            source("test://shouldReportArityMismatchInPattern", point(32, 3, 1), point(46, 3, 15))));
    }

    @Test
    public void shouldParsePrecedenceOfConstructorOperator() {
        compile(
            "module scotch.test",
            "import scotch.data.list",
            "tail (_:xs) = xs"
        );
        shouldNotHaveErrors();
        shouldHaveValue("scotch.test.tail", matcher("scotch.test.(tail#0)", t(8), arg("#0", t(7)), pattern(
            "scotch.test.(tail#0#0)",
            asList(struct(arg("#0", t(12)), ":", t(9), asList(
                field("_0", t(10), ignore(t(2))),
                field("_1", t(11), capture(access(arg("#0", t(12)), "_1", t(14)), "xs", t(4)))
            ))),
            id("xs", t(6))
        )));
    }

    @Test
    public void shouldParsePrecedenceOfNestedConstructorOperator() {
        compile(
            "module scotch.test",
            "import scotch.data.list",
            "secondTail (_:_:xs) = xs"
        );
        shouldNotHaveErrors();
        shouldHaveValue("scotch.test.secondTail", matcher("scotch.test.(secondTail#0)", t(10), arg("#0", t(9)), pattern(
            "scotch.test.(secondTail#0#0)",
            asList(struct(arg("#0", t(17)), ":", t(14), asList(
                field("_0", t(15), ignore(t(2))),
                field("_1", t(16), struct(access(arg("#0", t(17)), "_1", t(19)), ":", t(11), asList(
                    field("_0", t(12), ignore(t(4))),
                    field("_1", t(13), capture(access(access(arg("#0", t(17)), "_1", t(19)), "_1", t(21)), "xs", t(6)))
                )))
            ))),
            id("xs", t(8))
        )));
    }

    @Override
    protected StubResolver initResolver() {
        StubResolver resolver = super.initResolver();
        SymbolEntry symbolEntry = mutableEntry(symbol("scotch.data.list.(:)"));
        symbolEntry.defineOperator(operator(RIGHT_INFIX, 5));
        symbolEntry
            .defineDataConstructor(DataConstructorDescriptor
                .builder(1, symbol("scotch.data.list.[]"), symbol("scotch.data.list.(:)"), "scotch/data/list/ConsList$Cons")
                .withFields(asList(
                    field(0, "_0", "get_0", var("a")),
                    field(1, "_1", "get_1", sum("scotch.data.list.[]", var("a")))
                ))
                .build());
        resolver.define(symbolEntry);
        return resolver;
    }

    @Override
    protected Function<Compiler, DefinitionGraph> compile() {
        return Compiler::parsePrecedence;
    }
}
