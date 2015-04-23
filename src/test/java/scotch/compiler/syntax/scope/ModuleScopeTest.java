package scotch.compiler.syntax.scope;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static scotch.compiler.syntax.scope.Scope.scope;
import static scotch.compiler.util.TestUtil.intType;
import static scotch.symbol.Symbol.qualified;
import static scotch.symbol.Symbol.symbol;
import static scotch.symbol.Symbol.unqualified;
import static scotch.symbol.type.Types.t;

import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import scotch.compiler.syntax.definition.Import;
import scotch.symbol.Operator;
import scotch.symbol.Symbol;
import scotch.symbol.SymbolResolver;
import scotch.symbol.type.Type;
import scotch.symbol.util.DefaultSymbolGenerator;
import scotch.symbol.util.SymbolGenerator;

@RunWith(MockitoJUnitRunner.class)
public class ModuleScopeTest {

    @Rule public final ExpectedException exception = none();
    @Mock private SymbolResolver resolver;
    @Mock private Scope          rootScope;
    @Mock private Import         import_;
    private       Scope          moduleScope;
    private       String         moduleName;

    @Before
    public void setUp() {
        moduleName = "scotch.test";
        SymbolGenerator symbolGenerator = new DefaultSymbolGenerator();
        moduleScope = scope(rootScope, new DefaultTypeScope(symbolGenerator, resolver), resolver, symbolGenerator, moduleName, asList(import_));
        when(rootScope.enterScope(anyListOf(Import.class))).thenReturn(moduleScope);
        when(import_.qualify(any(String.class), any(SymbolResolver.class))).thenReturn(Optional.empty());
    }

    @Test
    public void leavingChildScopeShouldGiveBackModuleScope() {
        assertThat(moduleScope.enterScope(emptyList()).leaveScope(), sameInstance(moduleScope));
    }

    @Test
    public void shouldThrow_whenEnteringScopeWithModuleName() {
        exception.expect(IllegalStateException.class);
        moduleScope.enterScope("module.name");
    }

    @Test
    public void shouldThrow_whenDefiningValue() {
        exception.expect(IllegalStateException.class);
        moduleScope.defineValue(symbol("value"), mock(Type.class));
    }

    @Test
    public void shouldQualifyValueDefinedInImportScopeByUnqualifiedSymbol() {
        String memberName = "fn";
        moduleScope.enterScope(emptyList()).defineValue(qualified(moduleName, memberName), t(2));
        assertThat(moduleScope.qualify(unqualified(memberName)), is(Optional.of(qualified(moduleName, memberName))));
    }

    @Test
    public void shouldGetNothingWhenQualifyingSymbolNotDefinedLocallyAndNotImported() {
        assertThat(moduleScope.qualify(unqualified("fn")), is(Optional.empty()));
    }

    @Test
    public void shouldDelegateQualificationToImportWhenUnqualifiedSymbolNotDefinedLocally() {
        String externalModule = "scotch.external";
        String memberName = "fn";
        when(resolver.getEntry(any(Symbol.class))).thenReturn(Optional.empty());
        when(import_.qualify(memberName, resolver)).thenReturn(Optional.of(qualified(externalModule, memberName)));
        moduleScope.enterScope(asList(import_));
        assertThat(moduleScope.qualify(unqualified(memberName)), is(Optional.of(qualified(externalModule, memberName))));
    }

    @Test
    public void shouldGetNothingWhenQualifyingQualifiedSymbolThatHasUnimportedModuleName() {
        assertThat(moduleScope.qualify(qualified("scotch.external", "fn")), is(Optional.empty()));
    }

    @Test
    public void shouldQualifyQualifiedSymbolThatHasImportedModuleName() {
        String moduleName = "scotch.external";
        String memberName = "fn";
        when(import_.isFrom(moduleName)).thenReturn(true);
        when(import_.qualify(memberName, resolver)).thenReturn(Optional.of(qualified(moduleName, memberName)));
        moduleScope.enterScope(asList(import_));
        assertThat(moduleScope.qualify(qualified(moduleName, memberName)), is(Optional.of(qualified(moduleName, memberName))));
    }

    @Test
    public void shouldThrow_whenDefiningOperator() {
        exception.expect(IllegalStateException.class);
        moduleScope.defineOperator(unqualified("fn"), mock(Operator.class));
    }

    @Test
    public void shouldThrow_whenGettingContext() {
        exception.expect(IllegalStateException.class);
        moduleScope.getContext(intType());
    }
}
