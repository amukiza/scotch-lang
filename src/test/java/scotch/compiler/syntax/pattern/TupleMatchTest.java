package scotch.compiler.syntax.pattern;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static scotch.compiler.util.TestUtil.access;
import static scotch.compiler.util.TestUtil.arg;
import static scotch.compiler.util.TestUtil.capture;
import static scotch.compiler.util.TestUtil.field;
import static scotch.compiler.util.TestUtil.tuple;
import static scotch.symbol.type.Types.t;

import org.junit.Test;
import scotch.compiler.syntax.scope.Scope;
import scotch.symbol.type.VariableType;

public class TupleMatchTest {

    @Test
    public void shouldBindTupleMatch() {
        VariableType t = t(0);
        Scope scope = mock(Scope.class);
        when(scope.reserveType()).thenReturn(t);
        PatternMatch struct = tuple("(,)", t, asList(field(t, capture("a", t(1))), field(t, capture("b", t(2)))));
        assertThat(struct.bind(arg("#0", t), scope), is(tuple(arg("#0", t), "(,)", t, asList(
            field("_0", t, capture(access(arg("#0", t), "_0", t), "a", t(1))),
            field("_1", t, capture(access(arg("#0", t), "_1", t), "b", t(2)))
        ))));
    }
}
