package scotch.compiler.analyzer;

import static java.util.Arrays.asList;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.conditional;
import static scotch.compiler.util.TestUtil.fn;
import static scotch.compiler.util.TestUtil.id;
import static scotch.compiler.util.TestUtil.let;
import static scotch.compiler.util.TestUtil.scope;
import static scotch.symbol.type.Types.t;

import java.util.Optional;
import java.util.function.Function;
import org.junit.Test;
import scotch.compiler.ClassLoaderResolver;
import scotch.compiler.Compiler;
import scotch.compiler.CompilerTest;
import scotch.compiler.syntax.definition.DefinitionGraph;

public class PatternAnalyzerTest extends CompilerTest<ClassLoaderResolver> {

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
                            t(15)
                        ),
                        id("a", t(6)),
                        id("b", t(7)),
                        t(8)
                    ))))
        ));
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
