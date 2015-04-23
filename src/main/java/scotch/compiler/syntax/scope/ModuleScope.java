package scotch.compiler.syntax.scope;

import static scotch.compiler.syntax.definition.Import.moduleImport;
import static scotch.compiler.text.SourceLocation.NULL_SOURCE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import scotch.compiler.syntax.definition.Import;
import scotch.compiler.syntax.reference.ValueReference;
import scotch.symbol.Operator;
import scotch.symbol.Symbol;
import scotch.symbol.Symbol.QualifiedSymbol;
import scotch.symbol.Symbol.SymbolVisitor;
import scotch.symbol.Symbol.UnqualifiedSymbol;
import scotch.symbol.SymbolEntry;
import scotch.symbol.SymbolResolver;
import scotch.symbol.descriptor.TypeClassDescriptor;
import scotch.symbol.type.Type;
import scotch.symbol.type.TypeScope;
import scotch.symbol.util.SymbolGenerator;

public class ModuleScope extends BlockScope {

    private final List<ImportScope> importScopes;

    ModuleScope(Scope parent, TypeScope types, SymbolResolver resolver, SymbolGenerator symbolGenerator, String moduleName) {
        super(parent, types, moduleName, resolver, symbolGenerator);
        this.importScopes = new ArrayList<>();
    }

    @Override
    public Scope enterScope(List<Import> imports) {
        ImportScope scope = new ImportScope(this, new DefaultTypeScope(symbolGenerator, resolver), resolver, symbolGenerator, moduleName, ImmutableList.<Import>builder()
            .add(moduleImport(NULL_SOURCE, "scotch.lang"))
            .add(moduleImport(NULL_SOURCE, "scotch.data.bool"))
            .add(moduleImport(NULL_SOURCE, "scotch.data.char"))
            .add(moduleImport(NULL_SOURCE, "scotch.data.double"))
            .add(moduleImport(NULL_SOURCE, "scotch.data.int"))
            .add(moduleImport(NULL_SOURCE, "scotch.data.list"))
            .add(moduleImport(NULL_SOURCE, "scotch.data.num"))
            .add(moduleImport(NULL_SOURCE, "scotch.data.string"))
            .add(moduleImport(NULL_SOURCE, "scotch.control.monad"))
            .addAll(imports)
            .build());
        importScopes.add(scope);
        return scope;
    }

    @Override
    public Set<Symbol> getContext(Type type) {
        throw new IllegalStateException();
    }

    @Override
    public Optional<TypeClassDescriptor> getMemberOf(ValueReference valueRef) {
        throw new IllegalStateException();
    }

    @Override
    public Optional<Operator> getOperator(Symbol symbol) {
        return getEntry(symbol).flatMap(SymbolEntry::getOperator);
    }

    @Override
    public Optional<Type> getRawValue(Symbol symbol) {
        return getEntry(symbol).flatMap(SymbolEntry::getValue);
    }

    @Override
    public Optional<SymbolEntry> getSiblingEntry(Symbol symbol) {
        return importScopes.stream()
            .filter(importScope -> importScope.isDefinedLocally(symbol))
            .findFirst()
            .flatMap(importScope -> importScope.getEntry(symbol));
    }

    @Override
    public boolean isDefined(Symbol symbol) {
        return getEntry(symbol).isPresent();
    }

    @Override
    public Optional<Symbol> qualify(Symbol symbol) {
        return importScopes.stream()
            .map(importScope -> importScope.qualify_(symbol))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    @Override
    protected SymbolEntry define(Symbol symbol) {
        throw new IllegalStateException();
    }

    @Override
    protected boolean isDefinedLocally(Symbol symbol) {
        return importScopes.stream().anyMatch(importScope -> importScope.isDefinedLocally(symbol));
    }

    @Override
    protected Optional<SymbolEntry> getEntry(Symbol symbol) {
        return symbol.accept(new SymbolVisitor<Optional<SymbolEntry>>() {
            @Override
            public Optional<SymbolEntry> visit(QualifiedSymbol symbol) {
                if (Objects.equals(symbol.getModuleName(), moduleName)) {
                    return getSiblingEntry(symbol);
                } else {
                    return parent.getEntry(symbol);
                }
            }

            @Override
            public Optional<SymbolEntry> visit(UnqualifiedSymbol symbol) {
                return qualify(symbol).flatMap(ModuleScope.this::getEntry);
            }
        });
    }

    @Override
    protected boolean isDataConstructor(Symbol symbol) {
        return parent.isDataConstructor(symbol);
    }
}
