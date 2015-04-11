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

    @Override
    protected Function<Compiler, DefinitionGraph> compile() {
        return Compiler::reducePatterns;
    }

    @Override
    protected ClassLoaderResolver initResolver() {
        return new ClassLoaderResolver(Optional.empty(), getClass().getClassLoader());
    }
}
