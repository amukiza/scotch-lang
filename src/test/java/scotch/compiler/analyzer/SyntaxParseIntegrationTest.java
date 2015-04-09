package scotch.compiler.analyzer;

import static java.util.Arrays.asList;
import static scotch.compiler.syntax.StubResolver.defaultEq;
import static scotch.compiler.syntax.StubResolver.defaultPlus;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.capture;
import static scotch.compiler.util.TestUtil.equal;
import static scotch.compiler.util.TestUtil.id;
import static scotch.compiler.util.TestUtil.literal;
import static scotch.compiler.util.TestUtil.matcher;
import static scotch.compiler.util.TestUtil.pattern;
import static scotch.compiler.util.TestUtil.valueRef;
import static scotch.symbol.Symbol.qualified;
import static scotch.symbol.SymbolEntry.immutableEntry;
import static scotch.symbol.type.Types.fn;
import static scotch.symbol.type.Types.sum;
import static scotch.symbol.type.Types.t;
import static scotch.symbol.type.Types.var;

import java.util.function.Function;
import org.junit.Ignore;
import org.junit.Test;
import scotch.compiler.Compiler;
import scotch.compiler.IsolatedCompilerTest;
import scotch.compiler.syntax.definition.DefinitionGraph;

public class SyntaxParseIntegrationTest extends IsolatedCompilerTest {

    @Test
    public void shouldShufflePattern() {
        resolver.define(immutableEntry(qualified("scotch.data.bool", "not")).build());
        compile(
            "module scotch.test",
            "import scotch.data.bool",
            "left infix 6 (==), (/=)",
            "x == y = not (x /= y)"
        );
        shouldHaveValue("scotch.test.(==)", matcher("scotch.test.(==#0)", t(11), asList(arg("#0", t(9)), arg("#1", t(10))),
            pattern("scotch.test.(==#0#0)", asList(capture(arg("#0", t(12)), "x", t(0)), capture(arg("#1", t(13)), "y", t(2))), apply(
                id("scotch.data.bool.not", t(3)),
                apply(
                    apply(id("scotch.test.(/=)", t(5)), id("x", t(4)), t(14)),
                    id("y", t(6)),
                    t(15)
                ),
                t(16)
            ))
        ));
    }

    @Test
    public void shouldConsolidatePatternsIntoSingleValue() {
        compile(
            "module scotch.test",
            "left infix 8 (+), (-)",
            "fib 0 = 0",
            "fib 1 = 1",
            "fib n = fib (n - 1) + fib (n - 2)"
        );
        shouldHaveValue("scotch.test.fib", matcher("scotch.test.(fib#0)", t(16), asList(arg("#0", t(15))),
            pattern("scotch.test.(fib#0#0)", asList(equal(arg("#0", t(17)), literal(0), value -> apply(
                apply(id("scotch.data.eq.(==)", t(18)), arg("#0", t(17)), t(19)),
                value,
                t(20)
            ))), literal(0)),
            pattern("scotch.test.(fib#0#1)", asList(equal(arg("#0", t(21)), literal(1), value -> apply(
                apply(id("scotch.data.eq.(==)", t(22)), arg("#0", t(21)), t(23)),
                value,
                t(24)
            ))), literal(1)),
            pattern("scotch.test.(fib#0#2)", asList(capture(arg("#0", t(25)), "n", t(3))), apply(
                apply(
                    id("scotch.test.(+)", t(9)),
                    apply(
                        id("scotch.test.fib", t(4)),
                        apply(
                            apply(id("scotch.test.(-)", t(6)), id("n", t(5)), t(26)),
                            literal(1),
                            t(27)
                        ),
                        t(28)
                    ),
                    t(32)
                ),
                apply(
                    id("scotch.test.fib", t(10)),
                    apply(
                        apply(id("scotch.test.(-)", t(12)), id("n", t(11)), t(29)),
                        literal(2),
                        t(30)
                    ),
                    t(31)
                ),
                t(33)
            ))
        ));
    }

    @Test
    public void shouldQualifySiblingValues() {
        resolver
            .define(defaultPlus())
            .define(defaultEq());
        compile(
            "module scotch.test",
            "import scotch.data.eq",
            "import scotch.data.num",
            "fn a b = a + b == b + a",
            "commutative? a b = fn a b"
        );
        shouldNotHaveErrors();
        shouldHaveValue("scotch.test.(commutative?)", matcher("scotch.test.(commutative?#0)", t(29), asList(arg("#0", t(27)), arg("#1", t(28))),
            pattern(
                "scotch.test.(commutative?#0#0)",
                asList(capture(arg("#0", t(30)), "a", t(11)), capture(arg("#1", t(31)), "b", t(12))),
                apply(
                    apply(
                        id("scotch.test.fn", t(13)),
                        id("a", t(14)),
                        t(32)
                    ),
                    id("b", t(15)),
                    t(33)
                )
            )
        ));
    }

    @Ignore
    @Test
    public void shouldParseTypeClass() {
        compile(
            "module scotch.test",
            "import scotch.data.bool",
            "class Eq a where",
            "    (==), (/=) :: a -> a -> Bool",
            "    x == y = not $ x /= y",
            "    x /= y = not $ x == y"
        );
        shouldHaveClass("scotch.test.Eq", asList(var("a")), asList(
            valueRef("scotch.test.(==)"),
            valueRef("scotch.test.(/=)"),
            valueRef("scotch.test.(==)"),
            valueRef("scotch.test.(/=)")
        ));
        shouldHaveValue("scotch.test.(==)", fn(var("a", asList("Eq")), fn(var("a", asList("Eq")), sum("Bool"))));
    }

    @Override
    protected Function<scotch.compiler.Compiler, DefinitionGraph> compile() {
        return Compiler::qualifyNames;
    }
}
