package scotch.compiler.syntax.pattern;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.capture;
import static scotch.compiler.util.TestUtil.fn;
import static scotch.compiler.util.TestUtil.id;
import static scotch.compiler.util.TestUtil.let;
import static scotch.compiler.util.TestUtil.matcher;
import static scotch.compiler.util.TestUtil.pattern;
import static scotch.symbol.type.Types.t;

import org.junit.Ignore;
import org.junit.Test;
import scotch.compiler.syntax.value.Value;
import scotch.symbol.util.SymbolGenerator;

public class DefaultPatternReducerTest {

    @Ignore("WIP")
    @Test
    public void shouldReduceCapturesToLets() {
        Value pattern = matcher("scotch.test.max", t(1), asList(arg("#0", t(2)), arg("#1", t(3))),
            pattern(
                "scotch.test.(max#0)",
                asList(capture(arg("#0", t(0)), "a", t(4)), capture(arg("#1", t(0)), "b", t(5))),
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
            is(fn("scotch.test.(max#0)", asList(arg("#0", t(2)), arg("#1", t(3))),
                let(t(9), "a", id("#0", t(10)),
                    let(t(11), "b", id("#1", t(12)),
                        apply(
                            id("a", t(6)),
                            id("b", t(7)),
                            t(8)
                        )))
            ))
        );
    }
}
