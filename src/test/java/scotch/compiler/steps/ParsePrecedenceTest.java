package scotch.compiler.steps;

import static java.util.Arrays.asList;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.text.SourceLocation.source;
import static scotch.compiler.text.SourcePoint.point;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.capture;
import static scotch.compiler.util.TestUtil.equal;
import static scotch.compiler.util.TestUtil.id;
import static scotch.compiler.util.TestUtil.literal;
import static scotch.compiler.util.TestUtil.matcher;
import static scotch.compiler.util.TestUtil.pattern;
import static scotch.symbol.Symbol.symbol;
import static scotch.symbol.type.Types.t;

import java.util.function.Function;
import org.junit.Test;
import scotch.compiler.IsolatedCompilerTest;
import scotch.compiler.steps.PrecedenceParser.ArityMismatch;
import scotch.compiler.syntax.definition.DefinitionGraph;

public class ParsePrecedenceTest extends IsolatedCompilerTest {

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
            asList(capture("#0", "x", t(0)), capture("#1", "y", t(2))),
            apply(id("x", t(3)), id("y", t(4)), t(8))
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
            asList(equal("#0", literal(0), value -> apply(
                apply(id("scotch.data.eq.(==)", t(3)), id("#0", t(4)), t(5)), value, t(6)
            ))),
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

    @Override
    protected Function<scotch.compiler.Compiler, DefinitionGraph> compile() {
        return scotch.compiler.Compiler::parsePrecedence;
    }
}
