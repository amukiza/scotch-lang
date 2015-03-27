package scotch.compiler.intermediate;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static scotch.compiler.Compiler.compiler;
import static scotch.compiler.intermediate.Intermediates.apply;
import static scotch.compiler.intermediate.Intermediates.conditional;
import static scotch.compiler.intermediate.Intermediates.instanceRef;
import static scotch.compiler.intermediate.Intermediates.literal;
import static scotch.compiler.intermediate.Intermediates.value;
import static scotch.compiler.intermediate.Intermediates.valueRef;
import static scotch.compiler.syntax.reference.DefinitionReference.valueRef;
import static scotch.symbol.Symbol.symbol;
import static scotch.util.StringUtil.quote;

import java.net.URI;
import java.util.Optional;
import org.junit.Test;
import scotch.compiler.ClassLoaderResolver;

public class IntermediateGeneratorTest {

    private IntermediateGraph graph;

    @Test
    public void shouldCreateIntermediateCode() {
        compile(
            "module scotch.test",
            "import scotch.data.num",
            "",
            "run = 2 + 2"
        );
        shouldHaveValue("scotch.test.run", apply(
            emptyList(),
            apply(
                emptyList(),
                apply(
                    emptyList(),
                    valueRef("scotch.data.num.(+)"),
                    instanceRef("scotch.data.num.Num", "scotch.data.num", "scotch.data.int.Int")
                ),
                literal(2)
            ),
            literal(2)
        ));
    }

    @Test
    public void shouldCreateIntermediateConditional() {
        compile(
            "module scotch.test",
            "import scotch.data.ord",
            "",
            "max = if 2 >= 1 then 2",
            "                else 1"
        );
        shouldHaveValue("scotch.test.max", conditional(
            apply(emptyList(),
                apply(emptyList(),
                    apply(emptyList(),
                        apply(emptyList(),
                            valueRef("scotch.data.ord.(>=)"), instanceRef("scotch.data.eq.Eq", "scotch.data.eq", "scotch.data.int.Int")),
                        instanceRef("scotch.data.ord.Ord", "scotch.data.ord", "scotch.data.int.Int")),
                    literal(2)),
                literal(1)),
            literal(2),
            literal(1)
        ));
    }

    private void compile(String... lines) {
        ClassLoader classLoader = IntermediateGeneratorTest.class.getClassLoader();
        ClassLoaderResolver symbolResolver = new ClassLoaderResolver(Optional.empty(), classLoader);
        graph = compiler(symbolResolver, URI.create("test://unnamed"), lines).generateIntermediateCode();
    }

    private void shouldHaveValue(String name, IntermediateValue value) {
        IntermediateDefinition definition = graph.getValue(valueRef(symbol(name)))
            .orElseThrow(() -> new IllegalArgumentException("Value " + quote(name) + " does not exist"));
        assertThat(definition, is(value(name, value)));
    }
}
