package scotch.compiler.syntax.pattern;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.capture;
import static scotch.compiler.util.TestUtil.conditional;
import static scotch.compiler.util.TestUtil.equal;
import static scotch.compiler.util.TestUtil.fn;
import static scotch.compiler.util.TestUtil.id;
import static scotch.compiler.util.TestUtil.ignore;
import static scotch.compiler.util.TestUtil.let;
import static scotch.compiler.util.TestUtil.literal;
import static scotch.compiler.util.TestUtil.matcher;
import static scotch.compiler.util.TestUtil.pattern;
import static scotch.symbol.type.Types.t;

import org.junit.Ignore;
import org.junit.Test;
import scotch.compiler.syntax.value.Value;
import scotch.symbol.util.SymbolGenerator;

public class DefaultPatternReducerTest {

    @Test
    public void shouldReduceCapturesToLets() {
        Value pattern = matcher("scotch.test.max", t(1), asList(arg("#0", t(2)), arg("#1", t(3))),
            pattern(
                "scotch.test.(max#0)",
                asList(capture(arg("#0", t(10)), "a", t(4)), capture(arg("#1", t(12)), "b", t(5))),
                apply(
                    id("a", t(6)),
                    id("b", t(7)),
                    t(8)
                )
            )
        );
        SymbolGenerator generator = new SymbolGenerator() {{
            startTypesAt(9);
        }};
        assertThat(pattern.reducePatterns(new DefaultPatternReducer(generator)),
            is(fn("scotch.test.max", asList(arg("#0", t(2)), arg("#1", t(3))),
                let(t(10), "a", arg("#0", t(10)),
                    let(t(9), "b", arg("#1", t(12)),
                        apply(
                            id("a", t(6)),
                            id("b", t(7)),
                            t(8)
                        )))
            )));
    }

    @Test
    public void shouldCreateIfAndElseForPattern() {
        Value pattern = matcher("scotch.test.(empty?)", t(1), arg("#0", t(2)),
            pattern("scotch.test.(empty?#0)",
                asList(equal(arg("#0", t(3)), id("[]", t(4)), value -> apply(
                    apply(id("scotch.data.eq.(==)", t(5)), value, t(6)),
                    id("[]", t(7)),
                    t(8)
                ))),
                literal(true)),
            pattern("scotch.test.(empty?#1)", asList(ignore(t(9))), literal(false))
        );
        SymbolGenerator generator = new SymbolGenerator() {{
            startTypesAt(10);
        }};
        assertThat(pattern.reducePatterns(new DefaultPatternReducer(generator)),
            is(fn("scotch.test.(empty?)", arg("#0", t(2)), conditional(
                apply(
                    apply(id("scotch.data.eq.(==)", t(5)), id("[]", t(4)), t(6)),
                    id("[]", t(7)),
                    t(8)
                ),
                literal(true),
                literal(false),
                t(10)
            ))));
    }

    @Ignore("WIP")
    @Test(expected = PatternReductionException.class)
    public void shouldRaiseErrorWhenPatternHasTwoCapturingCasesInARow() {
        Value pattern = matcher("scotch.test.max", t(1), asList(arg("#0", t(2)), arg("#1", t(3))),
            pattern(
                "scotch.test.(max#0)",
                asList(capture(arg("#0", t(10)), "a", t(4)), capture(arg("#1", t(12)), "b", t(5))),
                id("a", t(6))
            ),
            pattern(
                "scotch.test.(max#0)",
                asList(capture(arg("#0", t(10)), "a", t(4)), capture(arg("#1", t(12)), "b", t(5))),
                id("b", t(7))
            )
        );
        SymbolGenerator generator = new SymbolGenerator();
        pattern.reducePatterns(new DefaultPatternReducer(generator));
    }
}
