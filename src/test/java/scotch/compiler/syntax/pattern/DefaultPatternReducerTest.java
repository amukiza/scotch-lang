package scotch.compiler.syntax.pattern;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.text.SourceLocation.NULL_SOURCE;
import static scotch.compiler.util.TestUtil.access;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.capture;
import static scotch.compiler.util.TestUtil.conditional;
import static scotch.compiler.util.TestUtil.equal;
import static scotch.compiler.util.TestUtil.field;
import static scotch.compiler.util.TestUtil.fn;
import static scotch.compiler.util.TestUtil.id;
import static scotch.compiler.util.TestUtil.ignore;
import static scotch.compiler.util.TestUtil.isConstructor;
import static scotch.compiler.util.TestUtil.let;
import static scotch.compiler.util.TestUtil.literal;
import static scotch.compiler.util.TestUtil.matcher;
import static scotch.compiler.util.TestUtil.pattern;
import static scotch.compiler.util.TestUtil.raise;
import static scotch.compiler.util.TestUtil.scope;
import static scotch.compiler.util.TestUtil.struct;
import static scotch.symbol.type.Types.t;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import scotch.compiler.syntax.value.Value;
import scotch.symbol.type.VariableType;
import scotch.symbol.util.DefaultSymbolGenerator;
import scotch.symbol.util.SymbolGenerator;

public class DefaultPatternReducerTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldReduceCapturesToLets() {
        // max a b = a b
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
        SymbolGenerator generator = new DefaultSymbolGenerator() {{
            startTypesAt(9);
        }};
        assertThat(pattern.reducePatterns(new DefaultPatternReducer(generator)),
            is(fn("scotch.test.max", asList(arg("#0", t(2)), arg("#1", t(3))),
                scope("scotch.test.(max#0)", let(t(12), "a", arg("#0", t(10)),
                    let(t(11), "b", arg("#1", t(12)),
                        apply(
                            id("a", t(6)),
                            id("b", t(7)),
                            t(8)
                        ))))
            )));
    }

    @Test
    public void shouldCreateIfAndElseForPattern() {
        // empty? [] = true
        // empty? _  = false
        Value pattern = matcher("scotch.test.(empty?)", t(1), arg("#0", t(2)),
            pattern("scotch.test.(empty?#0)",
                asList(equal(arg("#0", t(3)), id("[]", t(4)))),
                literal(true)),
            pattern("scotch.test.(empty?#1)", asList(ignore(t(9))), literal(false))
        );
        SymbolGenerator generator = new DefaultSymbolGenerator() {{
            startTypesAt(10);
        }};
        assertThat(pattern.reducePatterns(new DefaultPatternReducer(generator)),
            is(fn("scotch.test.(empty?)", arg("#0", t(2)), conditional(
                apply(
                    apply(id("scotch.data.eq.(==)", t(10)), arg("#0", t(3)), t(11)),
                    id("[]", t(4)),
                    t(12)
                ),
                scope("scotch.test.(empty?#0)", literal(true)),
                scope("scotch.test.(empty?#1)", literal(false)),
                t(13)
            ))));
    }

    @Test
    public void shouldRaiseErrorWhenPatternHasNonTerminalDefaultCase() {
        // max a b = a
        // max a b = b
        exception.expect(PatternReductionException.class);
        exception.expect(is(new PatternReductionException("Non-terminal default pattern case", NULL_SOURCE)));
        Value pattern = matcher("scotch.test.max", t(1), asList(arg("#0", t(2)), arg("#1", t(3))),
            pattern(
                "scotch.test.(max#0)",
                asList(capture(arg("#0", t(10)), "a", t(4)), capture(arg("#1", t(12)), "b", t(5))),
                id("a", t(6))
            ),
            pattern(
                "scotch.test.(max#1)",
                asList(capture(arg("#0", t(10)), "a", t(4)), capture(arg("#1", t(12)), "b", t(5))),
                id("b", t(7))
            )
        );
        SymbolGenerator generator = new DefaultSymbolGenerator();
        pattern.reducePatterns(new DefaultPatternReducer(generator));
    }

    @Test
    public void patternShouldAddDefaultCase() {
        // oneOrZero 1 = true
        // oneOrZero 0 = true
        Value pattern = matcher("scotch.test.oneOrZero", t(1), arg("#0", t(2)),
            pattern("scotch.test.(oneOrZero#0)", asList(equal(arg("#0", t(3)), literal(1))), literal(true)),
            pattern("scotch.test.(oneOrZero#1)", asList(equal(arg("#0", t(10)), literal(0))), literal(true))
        );
        SymbolGenerator generator = new DefaultSymbolGenerator() {{
            startTypesAt(14);
        }};
        assertThat(pattern.reducePatterns(new DefaultPatternReducer(generator)),
            is(fn("scotch.test.oneOrZero", arg("#0", t(2)), conditional(
                apply(
                    apply(id("scotch.data.eq.(==)", t(14)), arg("#0", t(3)), t(15)),
                    literal(1),
                    t(16)
                ),
                scope("scotch.test.(oneOrZero#0)", literal(true)),
                conditional(
                    apply(
                        apply(id("scotch.data.eq.(==)", t(17)), arg("#0", t(10)), t(18)),
                        literal(0),
                        t(19)
                    ),
                    scope("scotch.test.(oneOrZero#1)", literal(true)),
                    raise("Incomplete match", t(20)),
                    t(21)
                ),
                t(22)
            ))));
    }

    @Test
    public void shouldAddAssignmentsWithinCondition() {
        // oneAndVar 1 n = n
        Value pattern = matcher("scotch.test.oneAndVar", t(1), asList(arg("#0", t(2)), arg("#1", t(3))),
            pattern("scotch.test.(oneAndVar#0)",
                asList(
                    equal(arg("#0", t(4)), literal(1)),
                    capture(arg("#0", t(8)), "n", t(9))
                ),
                id("n", t(10)))
        );
        SymbolGenerator generator = new DefaultSymbolGenerator() {{
            startTypesAt(11);
        }};
        assertThat(pattern.reducePatterns(new DefaultPatternReducer(generator)),
            is(fn("scotch.test.oneAndVar", asList(arg("#0", t(2)), arg("#1", t(3))), conditional(
                apply(
                    apply(id("scotch.data.eq.(==)", t(11)), arg("#0", t(4)), t(12)),
                    literal(1),
                    t(13)
                ),
                scope("scotch.test.(oneAndVar#0)", let(t(15), "n", arg("#0", t(8)), id("n", t(10)))),
                raise("Incomplete match", t(14)),
                t(16)
            ))));
    }

    @Test
    public void shouldDestructureTupleAndTagValues() {
        // second (_, b) = b
        String tuple2 = "scotch.data.tuple.(,)";
        Value pattern = matcher("scotch.test.second", t(1), arg("#0", t(2)),
            pattern("scotch.test.(second#0)",
                asList(struct(arg("#0", t(3)), tuple2, t(4), asList(
                    field("_0", t(5), ignore(t(6))),
                    field("_1", t(7), capture(access(arg("#0", t(8)), "_1", t(9)), "b", t(10)))
                ))),
                id("b", t(11))
            )
        );
        SymbolGenerator generator = new DefaultSymbolGenerator() {{
            startTypesAt(12);
        }};
        assertThat(pattern.reducePatterns(new DefaultPatternReducer(generator)),
            is(fn("scotch.test.second", arg("#0", t(2)), conditional(
                isConstructor(arg("#0", t(3), tuple2), tuple2),
                scope("scotch.test.(second#0)", let(t(13), "b", access(arg("#0", t(8), tuple2), "_1", t(9)), id("b", t(11)))),
                raise("Incomplete match", t(12)),
                t(14)
            ))));
    }

    @Test
    public void shouldDestructureNestedTupleAndTagValues() {
        // third (_, (_, c)) = c
        VariableType t = t(0);
        String tag = "scotch.data.tuple.(,)";
        Value pattern = matcher("scotch.test.third", t, arg("#0", t),
            pattern("scotch.test.(third#0)",
                asList(struct(arg("#0", t), tag, t, asList(
                    field("_0", t, ignore(t)),
                    field("_1", t, struct(access(arg("#0", t), "_1", t), tag, t, asList(
                        field("_0", t, ignore(t)),
                        field("_1", t, capture(access(access(arg("#0", t), "_1", t), "_1", t), "c", t))
                    )))
                ))),
                id("c", t)
            )
        );
        SymbolGenerator generator = mock(SymbolGenerator.class);
        when(generator.reserveType()).thenReturn(t);
        assertThat(pattern.reducePatterns(new DefaultPatternReducer(generator)),
            is(fn("scotch.test.third", arg("#0", t), conditional(
                apply(
                    apply(
                        id("scotch.data.bool.(&&)", t),
                        isConstructor(arg("#0", t, tag), tag),
                        t),
                    isConstructor(access(arg("#0", t, tag), "_1", t, tag), tag),
                    t),
                scope("scotch.test.(third#0)", let(t, "c", access(access(arg("#0", t, tag), "_1", t, tag), "_1", t), id("c", t))),
                raise("Incomplete match", t),
                t
            ))));
    }
}
