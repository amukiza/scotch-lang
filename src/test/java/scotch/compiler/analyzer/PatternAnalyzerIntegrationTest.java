package scotch.compiler.analyzer;

import static java.util.Arrays.asList;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.util.TestUtil.access;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.conditional;
import static scotch.compiler.util.TestUtil.fn;
import static scotch.compiler.util.TestUtil.id;
import static scotch.compiler.util.TestUtil.isConstructor;
import static scotch.compiler.util.TestUtil.let;
import static scotch.compiler.util.TestUtil.literal;
import static scotch.compiler.util.TestUtil.raise;
import static scotch.compiler.util.TestUtil.scope;
import static scotch.symbol.type.Types.t;

import java.util.Optional;
import java.util.function.Function;
import org.junit.Test;
import scotch.compiler.ClassLoaderResolver;
import scotch.compiler.Compiler;
import scotch.compiler.CompilerTest;
import scotch.compiler.syntax.definition.DefinitionGraph;

public class PatternAnalyzerIntegrationTest extends CompilerTest<ClassLoaderResolver> {

    @Test
    public void shouldReduceCapturesToLets() {
        compile(
            "module scotch.test",
            "max a b = if a >= b",
            "            then a",
            "            else b"
        );
        shouldHaveValue("scotch.test.max", fn("scotch.test.(max#0)", asList(arg("#0", t(9)), arg("#1", t(10))),
            scope("scotch.test.(max#0#0)", let(t(19), "a", arg("#0", t(12)),
                let(t(18), "b", arg("#1", t(13)),
                    conditional(
                        apply(
                            apply(id("scotch.data.ord.(>=)", t(4)), id("a", t(3)), t(14)),
                            id("b", t(5)),
                            t(15)),
                        id("a", t(6)),
                        id("b", t(7)),
                        t(8)
                    ))))
        ));
    }

    @Test
    public void shouldDeeplyTagConditionals() {
        compile(
            "module scotch.test",
            "fourth (_, (_, (_, d))) = d"
        );
        String tag = "scotch.data.tuple.(,)";
        shouldHaveValue("scotch.test.fourth", fn("scotch.test.(fourth#0)", arg("#0", t(21)),
            conditional(
                apply(
                    apply(
                        id("scotch.data.bool.(&&)", t(34)),
                        apply(
                            apply(
                                id("scotch.data.bool.(&&)", t(31)),
                                isConstructor(arg("#0", t(23), tag), tag),
                                t(32)),
                            isConstructor(access(arg("#0", t(23), tag), "_1", t(25), tag), tag),
                            t(33)),
                        t(35)),
                    isConstructor(access(access(arg("#0", t(23), tag), "_1", t(25), tag), "_1", t(27), tag), tag),
                    t(36)),
                scope("scotch.test.(fourth#0#0)",
                    let(t(37), "d", access(access(access(arg("#0", t(23), tag), "_1", t(25), tag), "_1", t(27), tag), "_1", t(29)),
                        id("d", t(20)))),
                raise("Incomplete match", t(30)),
                t(38)
            )));
    }

    @Test
    public void shouldTagDestructuredToast() {
        compile(
            "module scotch.test",
            "data Toast { kind :: String, burnLevel :: Int }",
            "isBurned? Toast { burnLevel = b } = b > 3"
        );
        String tag = "scotch.test.Toast";
        shouldHaveValue("scotch.test.(isBurned?)", fn("scotch.test.(isBurned?#0)", arg("#0", t(6)),
            conditional(
                isConstructor(arg("#0", t(8), tag), tag),
                scope("scotch.test.(isBurned?#0#0)",
                    let(t(13), "b", access(arg("#0", t(8), tag), "burnLevel",  t(9)),
                        apply(
                            apply(
                                id("scotch.data.ord.(>)", t(5)),
                                id("b", t(4)),
                                t(10)),
                            literal(3),
                            t(11)))),
                raise("Incomplete match", t(12)),
                t(14)
            )));
    }

    @Test
    public void shouldTagSecondTail() {
        compile(
            "module scotch.test",
            "secondTail (_:_:xs) = xs"
        );
        String tag = "scotch.data.list.(:)";
        shouldHaveValue("scotch.test.secondTail", fn("scotch.test.(secondTail#0)", arg("#0", t(9)),
            conditional(
                apply(
                    apply(
                        id("scotch.data.bool.(&&)", t(23)),
                        isConstructor(arg("#0", t(17), tag), tag),
                        t(24)),
                    isConstructor(access(arg("#0", t(17), tag), "_1", t(19), tag), tag),
                    t(25)),
                scope("scotch.test.(secondTail#0#0)",
                    let(t(26), "xs", access(access(arg("#0", t(17), tag), "_1", t(19), tag), "_1", t(21)), id("xs", t(8)))),
                raise("Incomplete match", t(22)),
                t(27)
            )));
    }

    @Test
    public void shouldDestructurePersonWithMultipleCases() {
        compile(
            "module scotch.test",
            "data Person { age :: Int }",
            "newborn? Person { age = 0 } = True",
            "newborn? Person { age = _ } = False",
            "run = newborn? Person { age = 1 }"
        );
        String tag = "scotch.test.Person";
        shouldHaveValue("scotch.test.(newborn?)", fn("scotch.test.(newborn?#0)", arg("#0", t(11)),
            conditional(
                apply(
                    apply(
                        id("scotch.data.bool.(&&)", t(22)),
                        isConstructor(arg("#0", t(13), tag), tag),
                        t(23)),
                    apply(
                        apply(
                            id("scotch.data.eq.(==)", t(17)),
                            access(arg("#0", t(13), tag), "age", t(14)),
                            t(18)),
                        literal(0),
                        t(19)),
                    t(24)),
                scope("scotch.test.(newborn?#0#0)", literal(true)),
                conditional(
                    isConstructor(arg("#0", t(15), tag), tag),
                    scope("scotch.test.(newborn?#0#1)", literal(false)),
                    raise("Incomplete match", t(20)),
                    t(21)),
                t(25)
            )));
    }

    @Override
    protected Function<Compiler, DefinitionGraph> compile() {
        return Compiler::reducePatterns;
    }

    @Override
    protected ClassLoaderResolver initResolver() {
        return new ClassLoaderResolver(Optional.empty(), getClass().getClassLoader());
    }
}
