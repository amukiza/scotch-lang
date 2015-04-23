package scotch.compiler.syntax.scope;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static scotch.compiler.syntax.definition.Import.moduleImport;
import static scotch.compiler.syntax.scope.Scope.scope;
import static scotch.compiler.text.SourceLocation.NULL_SOURCE;
import static scotch.symbol.Operator.operator;
import static scotch.symbol.Symbol.symbol;
import static scotch.symbol.SymbolEntry.mutableEntry;
import static scotch.symbol.Value.Fixity.RIGHT_INFIX;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import scotch.symbol.Symbol;
import scotch.symbol.SymbolEntry;
import scotch.symbol.SymbolResolver;
import scotch.symbol.util.DefaultSymbolGenerator;

public class ImportScopeTest {

    private SymbolResolver resolver;
    private ModuleScope    moduleScope;

    @Before
    public void setUp() {
        resolver = mock(SymbolResolver.class);
        RootScope rootScope = scope(new DefaultSymbolGenerator(), resolver);
        moduleScope = (ModuleScope) rootScope.enterScope("scotch.test.module");
    }

    @Test
    public void shouldGetSiblingEntry() {
        when(resolver.getEntry(any(Symbol.class))).thenReturn(Optional.empty());
        Scope scope1 = moduleScope.enterScope(emptyList());
        Scope scope2 = moduleScope.enterScope(emptyList());

        scope1.defineOperator(symbol("scotch.test.module.($)"), operator(RIGHT_INFIX, 0));

        assertThat(scope2.getOperator(symbol("scotch.test.module.($)")), is(Optional.of(operator(RIGHT_INFIX, 0))));
    }

    @Test
    public void shouldNotUseSiblingImportStatements() {
        SymbolEntry entry = mutableEntry(symbol("scotch.test.function.($)"));
        entry.defineOperator(operator(RIGHT_INFIX, 0));
        when(resolver.getEntry(any(Symbol.class))).thenReturn(Optional.empty());
        when(resolver.getEntry(symbol("scotch.test.function.($)"))).thenReturn(Optional.of(entry));

        Scope scope1 = moduleScope.enterScope(asList(moduleImport(NULL_SOURCE, "scotch.test.function")));
        Scope scope2 = moduleScope.enterScope(emptyList());

        assertThat(scope1.getOperator(symbol("scotch.test.function.($)")), is(Optional.of(operator(RIGHT_INFIX, 0))));
        assertThat(scope2.getOperator(symbol("scotch.test.function.($)")), is(Optional.empty()));
    }
}
