package scotch.compiler.intermediate;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static me.qmx.jitescript.util.CodegenUtils.sig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static scotch.compiler.Compiler.compiler;
import static scotch.compiler.intermediate.Intermediates.access;
import static scotch.compiler.intermediate.Intermediates.apply;
import static scotch.compiler.intermediate.Intermediates.assign;
import static scotch.compiler.intermediate.Intermediates.conditional;
import static scotch.compiler.intermediate.Intermediates.constantReference;
import static scotch.compiler.intermediate.Intermediates.constructor;
import static scotch.compiler.intermediate.Intermediates.data;
import static scotch.compiler.intermediate.Intermediates.field;
import static scotch.compiler.intermediate.Intermediates.function;
import static scotch.compiler.intermediate.Intermediates.instanceOf;
import static scotch.compiler.intermediate.Intermediates.instanceRef;
import static scotch.compiler.intermediate.Intermediates.literal;
import static scotch.compiler.intermediate.Intermediates.raise;
import static scotch.compiler.intermediate.Intermediates.valueRef;
import static scotch.compiler.intermediate.Intermediates.variable;
import static scotch.compiler.syntax.reference.DefinitionReference.dataRef;
import static scotch.compiler.syntax.reference.DefinitionReference.valueRef;
import static scotch.symbol.FieldSignature.fieldSignature;
import static scotch.symbol.MethodSignature.constructor;
import static scotch.symbol.MethodSignature.staticMethod;
import static scotch.symbol.Symbol.symbol;
import static scotch.symbol.type.Types.sum;
import static scotch.symbol.type.Types.var;
import static scotch.util.StringUtil.quote;

import java.net.URI;
import java.util.Optional;
import org.junit.Test;
import scotch.compiler.ClassLoaderResolver;
import scotch.runtime.Applicable;
import scotch.runtime.Callable;
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
                    valueRef(
                        "scotch.data.num.(+)",
                        staticMethod("scotch/data/num/Num", "add", sig(Applicable.class))),
                    instanceRef("scotch.data.num.Num", "scotch.data.num", "scotch.data.int.Int",
                        staticMethod("scotch/data/num/NumInt", "instance", sig(Callable.class)))
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
            "max = if 2 >= 1",
            "        then 2",
            "        else 1"
        );
        shouldHaveValue("scotch.test.max", conditional(
            apply(emptyList(),
                apply(emptyList(),
                    apply(emptyList(),
                        apply(emptyList(),
                            valueRef(
                                "scotch.data.ord.(>=)",
                                staticMethod("scotch/data/ord/Ord", "greaterThanEquals", sig(Applicable.class))),
                            instanceRef("scotch.data.eq.Eq", "scotch.data.eq", "scotch.data.int.Int",
                                staticMethod("scotch/data/eq/EqInt", "instance", sig(Callable.class)))),
                        instanceRef("scotch.data.ord.Ord", "scotch.data.ord", "scotch.data.int.Int",
                            staticMethod("scotch/data/ord/OrdInt", "instance", sig(Callable.class)))),
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
                                                    valueRef(
                                                        "scotch.data.ord.(>=)",
                                                        staticMethod("scotch/data/ord/Ord", "greaterThanEquals", sig(Applicable.class))),
                                                    variable("#0i")),
                                                variable("#1i")),
                                            variable("a")),
                                        variable("b")),
                                    variable("a"),
                                    variable("b")
                                ))))))));
    }

    @Test
    public void shouldGenerateEmptyListEquals() {
        compile(
            "module scotch.test",
            "bothEqual? = [] == []"
        );
        shouldHaveValue("scotch.test.(bothEqual?)", apply(emptyList(),
            apply(emptyList(),
                apply(emptyList(),
                    valueRef(
                        "scotch.data.eq.(==)",
                        staticMethod("scotch/data/eq/Eq", "eq", sig(Applicable.class))),
                    instanceRef("scotch.data.eq.Eq", "scotch.data.list", sum("scotch.data.list.[]", asList(sum("scotch.data.list.[]"))),
                        staticMethod("scotch/data/list/EqList", "instance", sig(Callable.class)))),
                valueRef("scotch.data.list.[]",
                    staticMethod("scotch/data/list/ConsList", "empty", sig(Callable.class)))),
            valueRef("scotch.data.list.[]",
                staticMethod("scotch/data/list/ConsList", "empty", sig(Callable.class)))));
    }

    @Test
    public void shouldGenerateDataDefinition() {
        compile(
            "module scotch.test",
            "data Thing n { value :: n }"
        );
        assertThat(graph.getDefinition(dataRef(symbol("scotch.test.Thing"))),
            is(Optional.of(data("scotch.test.Thing", asList(var("n")), asList(
                constructor("scotch.test.Thing", asList(
                    field("value", var("n"))
                ))
            )))));
    }

    @Test
    public void shouldGenerateConstructor() {
        compile(
            "module scotch.test",
            "data Thing n { value :: n }"
        );
        shouldHaveValue("scotch.test.Thing", function(emptyList(), "value",
            constructor("scotch.test.Thing", "scotch/test/Thing$Thing",
                constructor("scotch/test/Thing$Thing:<init>:" + sig(void.class, Callable.class)),
                asList(variable("value")))));
    }

    @Test
    public void shouldGenerateNullaryConstructor() {
        compile(
            "module scotch.test",
            "data Maybe a = Nothing | Just a"
        );
        assertThat(graph.getDefinition(dataRef(symbol("scotch.test.Maybe"))),
            is(Optional.of(data("scotch.test.Maybe", asList(var("a")), asList(
                constructor("scotch.test.Nothing", "scotch.test.Maybe", emptyList()),
                constructor("scotch.test.Just", "scotch.test.Maybe", asList(
                    field("_0", var("a"))
                ))
            )))));
        shouldHaveValue("scotch.test.Nothing", constantReference("scotch.test.Nothing", "scotch.test.Maybe",
            fieldSignature("scotch/test/Maybe$Nothing", ACC_PUBLIC | ACC_FINAL | ACC_STATIC, "INSTANCE", "Lscotch/runtime/Callable;")));
    }

    @Test
    public void shouldGenerateTupleDestructure() {
        compile(
            "module scotch.test",
            "second (_, b) = b"
        );
        shouldHaveValue("scotch.test.second", function(emptyList(), "#0", conditional(
            instanceOf(variable("#0"), "scotch/data/tuple/Tuple2$Tuple2Data"),
            assign("b", access(asList("#0"), variable("#0"), "_1", "get_1"), variable("b")),
            raise("Incomplete match")
        )));
    }

    @Test
    public void shouldGenerateNestedTupleDestructure() {
        compile(
            "module scotch.test",
            "third (_, (_, c)) = c"
        );
        shouldHaveValue("scotch.test.third", function(emptyList(), "#0", conditional(
            apply(asList("#0"),
                apply(asList("#0"),
                    valueRef("scotch.data.bool.(&&)",
                        staticMethod("scotch/data/bool/Bool", "and", sig(Applicable.class))),
                    instanceOf(variable("#0"), "scotch/data/tuple/Tuple2$Tuple2Data")),
                instanceOf(access(asList("#0"), variable("#0"), "_1", "get_1"), "scotch/data/tuple/Tuple2$Tuple2Data")),
            assign("c", access(asList("#0"), access(asList("#0"), variable("#0"), "_1", "get_1"), "_1", "get_1"), variable("c")),
            raise("Incomplete match")
        )));
    }

    private void compile(String... lines) {
        ClassLoader classLoader = IntermediateGeneratorTest.class.getClassLoader();
        ClassLoaderResolver symbolResolver = new ClassLoaderResolver(Optional.empty(), classLoader);
        graph = compiler(symbolResolver, URI.create("test://unnamed"), lines).generateIntermediateCode();
    }

    private IntermediateDefinition getDefinition(String name) {
        return graph.getDefinition(valueRef(symbol(name)))
            .orElseThrow(() -> new IllegalArgumentException("Value " + quote(name) + " does not exist"));
    }

    private void shouldHaveValue(String name, IntermediateValue value) {
        assertThat(((IntermediateValueDefinition) getDefinition(name)).getValue(), is(value));
    }

    private void shouldHaveValue(String name, Type type) {
        assertThat(((IntermediateValueDefinition) getDefinition(name)).getType(), is(type));
    }
}
