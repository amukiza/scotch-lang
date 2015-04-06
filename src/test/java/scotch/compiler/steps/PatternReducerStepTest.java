package scotch.compiler.steps;

import static java.util.Arrays.asList;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.conditional;
import static scotch.compiler.util.TestUtil.fn;
import static scotch.compiler.util.TestUtil.id;
import static scotch.compiler.util.TestUtil.let;
import static scotch.symbol.type.Types.t;

import java.util.Optional;
import java.util.function.Function;
import org.junit.Ignore;
import org.junit.Test;
import scotch.compiler.ClassLoaderResolver;
import scotch.compiler.Compiler;
import scotch.compiler.CompilerTest;
import scotch.compiler.syntax.definition.DefinitionGraph;

public class PatternReducerStepTest extends CompilerTest<ClassLoaderResolver> {

    @Ignore("WIP")
    @Test
    public void shouldReduceCapturesToLets() {
        compile(
            "module scotch.test",
            "import scotch.data.eq",
            "import scotch.data.int",
            "import scotch.data.ord",
            "",
            "max a b = if a >= b",
            "            then a",
            "            else b"
        );
        shouldHaveValue("scotch.test.max", fn("scotch.test.(max#0)", asList(arg("#0", t(0)), arg("#1", t(1))),
            let(t(0), "a", id("#0", t(0)),
                let(t(0), "b", id("#1", t(0)),
                    conditional(
                        apply(
                            apply(id("scotch.data.ord.(>=)", t(0)), id("a", t(0)), t(0)),
                            id("b", t(0)),
                            t(0)
                        ),
                        id("a", t(0)),
                        id("b", t(0)),
                        t(0)
                    )))
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
