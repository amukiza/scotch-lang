package scotch.compiler.analyzer;

import static java.util.Arrays.asList;
import static scotch.compiler.syntax.TypeError.typeError;
import static scotch.compiler.text.SourceLocation.source;
import static scotch.compiler.text.SourcePoint.point;
import static scotch.compiler.text.TextUtil.repeat;
import static scotch.compiler.util.TestUtil.access;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.conditional;
import static scotch.compiler.util.TestUtil.fn;
import static scotch.compiler.util.TestUtil.isConstructor;
import static scotch.compiler.util.TestUtil.let;
import static scotch.compiler.util.TestUtil.raise;
import static scotch.compiler.util.TestUtil.scope;
import static scotch.symbol.type.Types.fn;
import static scotch.symbol.type.Types.sum;
import static scotch.symbol.type.Types.t;
import static scotch.symbol.type.Unification.mismatch;

import java.util.Optional;
import java.util.function.Function;
import org.junit.Test;
import scotch.compiler.ClassLoaderResolver;
import scotch.compiler.Compiler;
import scotch.compiler.CompilerTest;
import scotch.compiler.syntax.definition.DefinitionGraph;
import scotch.symbol.type.SumType;
import scotch.symbol.type.Type;

public class TypeCheckerIntegrationTest extends CompilerTest<ClassLoaderResolver> {

    @Test
    public void shouldHaveTypeOfTuple3OfInts() {
        compile(
            "module scotch.test",
            "tuple = (1, 2, 3)"
        );
        shouldNotHaveErrors();
        shouldHaveValue("scotch.test.tuple", sum("scotch.data.tuple.(,,)", asList(intType, intType, intType)));
    }

    @Test
    public void shouldHaveError_whenListIsHeterogeneous() {
        compile(
            "module scotch.test",
            "list = [1, 2, \"oops\"]"
        );
        shouldHaveErrors(typeError(
            mismatch(intType, stringType),
            source("test://shouldHaveError_whenListIsHeterogeneous", point(33, 2, 15), point(39, 2, 21))
        ));
    }

    @Test
    public void shouldDetermineTypeOfSuccessfulChainedMaybe() {
        compile(
            "module scotch.test",
            "",
            "addedStuff = do",
            "    x <- Just 3",
            "    y <- Just 2",
            "    return $ x + y"
        );
        shouldNotHaveErrors();
        shouldHaveValue("scotch.test.addedStuff", sum("scotch.data.maybe.Maybe", asList(intType)));
    }

    @Test
    public void shouldDetermineTypeOfFailedChainedMaybe() {
        compile(
            "module scotch.test",
            "",
            "addedStuff = do",
            "    x <- Just 3",
            "    y <- Nothing",
            "    return $ x + y"
        );
        shouldNotHaveErrors();
        shouldHaveValue("scotch.test.addedStuff", sum("scotch.data.maybe.Maybe", asList(intType)));
    }

    @Test
    public void shouldDestructure2Tuples() {
        compile(
            "module scotch.test",
            "second (_, b) = b",
            "third (_, (_, c)) = c"
        );
        shouldNotHaveErrors();
        Type tuple = tupleType(t(49), t(50));
        String tag = "scotch.data.tuple.(,)";
        shouldHaveValue("scotch.test.second", fn(tuple, t(50)));
        shouldHaveValue("scotch.test.second", fn("scotch.test.(second#0)", arg("#0", tuple),
            conditional(
                isConstructor(arg("#0", tuple, tag), tag),
                scope("scotch.test.(second#0#0)",
                    let(t(50), "b", access(arg("#0", tuple, tag), "_1", t(50)), arg("b", t(50)))),
                raise("Incomplete match", t(50)),
                t(50)
            )
        ));
        shouldHaveValue("scotch.test.third", fn(tupleType(t(45), tupleType(t(47), t(48))), t(48)));
    }

    private static SumType tupleType(Type... types) {
        return sum("scotch.data.tuple.(" + repeat(",", types.length - 1) + ")", asList(types));
    }

    @Override
    protected Function<Compiler, DefinitionGraph> compile() {
        return Compiler::checkTypes;
    }

    @Override
    protected ClassLoaderResolver initResolver() {
        return new ClassLoaderResolver(Optional.empty(), getClass().getClassLoader());
    }
}
