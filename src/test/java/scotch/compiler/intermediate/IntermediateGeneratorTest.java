package scotch.compiler.intermediate;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static scotch.compiler.Compiler.compiler;
import static scotch.compiler.intermediate.Intermediates.apply;
import static scotch.compiler.intermediate.Intermediates.assign;
import static scotch.compiler.intermediate.Intermediates.conditional;
import static scotch.compiler.intermediate.Intermediates.function;
import static scotch.compiler.intermediate.Intermediates.instanceRef;
import static scotch.compiler.intermediate.Intermediates.literal;
import static scotch.compiler.intermediate.Intermediates.valueRef;
import static scotch.compiler.intermediate.Intermediates.variable;
import static scotch.compiler.syntax.reference.DefinitionReference.valueRef;
import static scotch.symbol.Symbol.symbol;
import static scotch.symbol.type.Types.sum;
import static scotch.util.StringUtil.quote;

import java.net.URI;
import java.util.Optional;
import org.junit.Test;
import scotch.compiler.ClassLoaderResolver;
import scotch.symbol.type.Type;

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
    public void shouldPreserveType() {
        compile(
            "module scotch.test",
            "import scotch.data.num",
            "",
            "run = 2 + 2"
        );
        shouldHaveValue("scotch.test.run", sum("scotch.data.int.Int"));
    }

    @Test
    public void shouldCreateIntermediateConditional() {
        compile(
            "module scotch.test",
            "import scotch.data.ord",
            "",
            "max = if 2 >= 1",
            "        then 2",
            "        else 1"
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

    @Test
    public void shouldCreateIntermediatePattern() {
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
        shouldHaveValue("scotch.test.max", function(emptyList(), "#0i",
            function(asList("#0i"), "#1i",
                function(asList("#0i", "#1i"), "#0",
                    function(asList("#0i", "#1i", "#0"), "#1",
                        assign("a", variable("#0"),
                            assign("b", variable("#1"),
                                conditional(
                                    apply(asList("#0i", "#1i", "a", "b"),
                                        apply(asList("#0i", "#1i", "a"),
                                            apply(asList("#0i", "#1i"),
                                                apply(asList("#0i"),
                                                    valueRef("scotch.data.ord.(>=)"), variable("#0i")),
                                                variable("#1i")),
                                            variable("a")),
                                        variable("b")),
                                    variable("a"),
                                    variable("b")
                                ))))))));
    }

    private void compile(String... lines) {
        ClassLoader classLoader = IntermediateGeneratorTest.class.getClassLoader();
        ClassLoaderResolver symbolResolver = new ClassLoaderResolver(Optional.empty(), classLoader);
        graph = compiler(symbolResolver, URI.create("test://unnamed"), lines).generateIntermediateCode();
    }

    private IntermediateDefinition getDefinition(String name) {
        return graph.getValue(valueRef(symbol(name)))
            .orElseThrow(() -> new IllegalArgumentException("Value " + quote(name) + " does not exist"));
    }

    private void shouldHaveValue(String name, IntermediateValue value) {
        assertThat(getDefinition(name).getValue(), is(value));
    }

    private void shouldHaveValue(String name, Type type) {
        assertThat(getDefinition(name).getType(), is(type));
    }
}
