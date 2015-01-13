package scotch.compiler.syntax;

import static java.util.stream.Collectors.toSet;
import static scotch.compiler.symbol.SymbolEntry.mutableEntry;
import static scotch.compiler.syntax.DefinitionReference.classRef;
import static scotch.util.StringUtil.quote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import scotch.compiler.symbol.MethodSignature;
import scotch.compiler.symbol.Operator;
import scotch.compiler.symbol.Symbol;
import scotch.compiler.symbol.Symbol.QualifiedSymbol;
import scotch.compiler.symbol.Symbol.SymbolVisitor;
import scotch.compiler.symbol.Symbol.UnqualifiedSymbol;
import scotch.compiler.symbol.SymbolEntry;
import scotch.compiler.symbol.SymbolGenerator;
import scotch.compiler.symbol.SymbolResolver;
import scotch.compiler.symbol.Type;
import scotch.compiler.symbol.Type.VariableType;
import scotch.compiler.symbol.TypeClassDescriptor;
import scotch.compiler.symbol.TypeInstanceDescriptor;
import scotch.compiler.symbol.TypeScope;
import scotch.compiler.symbol.Unification;
import scotch.compiler.symbol.exception.SymbolNotFoundException;
import scotch.compiler.syntax.DefinitionReference.ClassReference;
import scotch.compiler.syntax.DefinitionReference.ModuleReference;
import scotch.compiler.syntax.DefinitionReference.ValueReference;
import scotch.compiler.syntax.Value.Identifier;

public abstract class Scope implements TypeScope {

    public static RootScope scope(SymbolGenerator symbolGenerator, SymbolResolver resolver) {
        return new RootScope(symbolGenerator, resolver);
    }

    public static ModuleScope scope(Scope parent, TypeScope types, SymbolResolver resolver, String moduleName, List<Import> imports) {
        return new ModuleScope(parent, types, resolver, moduleName, imports);
    }

    public static ChildScope scope(Scope parent, TypeScope types) {
        return new ChildScope(parent, types);
    }

    private Scope() {
        // intentionally empty
    }

    public abstract void addDependency(Symbol symbol);

    public void addLocal(String argument) {
        throw new IllegalStateException();
    }

    public abstract void addPattern(Symbol symbol, PatternMatcher pattern);

    public Value bind(Identifier identifier) {
        return identifier.getSymbol().accept(new SymbolVisitor<Value>() {
            @Override
            public Value visit(QualifiedSymbol symbol) {
                if (isMember(symbol) || getValue(symbol).hasContext()) {
                    return identifier.unboundMethod(getValue(symbol));
                } else {
                    return identifier.boundValue(getValue(symbol));
                }
            }

            @Override
            public Value visit(UnqualifiedSymbol symbol) {
                return identifier.boundArg(getValue(symbol));
            }
        });
    }

    public void capture(String argument) {
        throw new IllegalStateException();
    }

    public abstract void defineOperator(Symbol symbol, Operator operator);

    public abstract void defineSignature(Symbol symbol, Type type);

    public abstract void defineValue(Symbol symbol, Type type);

    public abstract Scope enterScope();

    public abstract Scope enterScope(String moduleName, List<Import> imports);

    public abstract void generalize(Type type);

    public List<String> getCaptures() {
        throw new IllegalStateException();
    }

    public abstract Set<Symbol> getDependencies();

    public List<String> getLocals() {
        throw new IllegalStateException();
    }

    public abstract TypeClassDescriptor getMemberOf(ValueReference valueRef);

    public abstract Operator getOperator(Symbol symbol);

    public abstract Scope getParent();

    public abstract Map<Symbol, List<PatternMatcher>> getPatterns();

    public Type getRawValue(ValueReference reference) {
        return getRawValue(reference.getSymbol());
    }

    public abstract Type getRawValue(Symbol symbol);

    public abstract Optional<Type> getSignature(Symbol symbol);

    public abstract TypeClassDescriptor getTypeClass(ClassReference classRef);

    public TypeInstanceDescriptor getTypeInstance(ClassReference classReference, ModuleReference moduleReference, List<Type> types) {
        return getTypeInstances(classReference.getSymbol(), types).stream()
            .filter(instance -> moduleReference.is(instance.getModuleName()))
            .findFirst()
            .orElseThrow(UnsupportedOperationException::new);
    }

    public abstract Set<TypeInstanceDescriptor> getTypeInstances(Symbol typeClass, List<Type> parameters);

    public Type getValue(ValueReference reference) {
        return getValue(reference.getSymbol());
    }

    public Type getValue(Symbol symbol) {
        return genericCopy(getRawValue(symbol));
    }

    public abstract Optional<MethodSignature> getValueSignature(Symbol symbol);

    public void insert(Scope scope) {
        throw new IllegalStateException();
    }

    public abstract boolean isDefined(Symbol symbol);

    public boolean isMember(Symbol symbol) {
        return getEntry(symbol).map(SymbolEntry::isMember).orElse(false);
    }

    public boolean isOperator(Symbol symbol) {
        return qualify(symbol).map(this::isOperator_).orElse(false);
    }

    public abstract Scope leaveScope();

    public void prependLocals(List<String> locals) {
        throw new IllegalStateException();
    }

    public abstract Optional<Symbol> qualify(Symbol symbol);

    public abstract Symbol qualifyCurrent(Symbol symbol);

    public void redefineSignature(Symbol symbol, Type type) {
        Optional<SymbolEntry> optionalEntry = getEntry(symbol);
        if (optionalEntry.isPresent()) {
            optionalEntry.get().redefineSignature(type);
        } else {
            throw new SymbolNotFoundException("Can't redefine non-existent value " + symbol.quote());
        }
    }

    public void redefineValue(Symbol symbol, Type type) {
        Optional<SymbolEntry> optionalEntry = getEntry(symbol);
        if (optionalEntry.isPresent()) {
            optionalEntry.get().redefineValue(type);
        } else {
            throw new SymbolNotFoundException("Can't redefine non-existent value " + symbol.quote());
        }
    }

    public abstract Symbol reserveSymbol();

    public Type reserveType() {
        return getParent().reserveType();
    }

    public void setParent(Scope parent) {
        throw new IllegalStateException();
    }

    public abstract void specialize(Type type);

    protected abstract Optional<SymbolEntry> getEntry(Symbol symbol);

    protected abstract boolean isDefinedLocally(Symbol symbol);

    protected boolean isExternal(Symbol symbol) {
        return getParent().isExternal(symbol);
    }

    protected abstract boolean isOperator_(Symbol symbol);

    public static class ChildScope extends Scope {

        private final TypeScope                         types;
        private final Set<ChildScope>                   children;
        private final Map<Symbol, SymbolEntry>          entries;
        private final Map<Symbol, List<PatternMatcher>> patterns;
        private final Set<Symbol>                       dependencies;
        private final List<String>                      captures;
        private final List<String>                      locals;
        private       Scope                             parent;

        private ChildScope(Scope parent, TypeScope types) {
            this.parent = parent;
            this.types = types;
            this.children = new HashSet<>();
            this.entries = new HashMap<>();
            this.patterns = new LinkedHashMap<>();
            this.dependencies = new HashSet<>();
            this.captures = new ArrayList<>();
            this.locals = new ArrayList<>();
        }

        @Override
        public void addDependency(Symbol symbol) {
            if (!isExternal(symbol)) {
                dependencies.add(symbol);
            }
        }

        @Override
        public void addLocal(String argument) {
            if (captures.contains(argument)) {
                throw new IllegalStateException("Argument " + quote(argument) + " is a capture!");
            } else if (!locals.contains(argument)) {
                locals.add(argument);
            }
        }

        public void addPattern(Symbol symbol, PatternMatcher pattern) {
            patterns.computeIfAbsent(symbol, k -> new ArrayList<>()).add(pattern);
        }

        @Override
        public Unification bind(VariableType variableType, Type target) {
            return types.bind(variableType, target);
        }

        @Override
        public void capture(String argument) {
            if (!locals.contains(argument) && !captures.contains(argument)) {
                captures.add(argument);
            }
        }

        @Override
        public void defineOperator(Symbol symbol, Operator operator) {
            throw new IllegalStateException("Can't define operator " + symbol.quote() + " in this scope");
        }

        @Override
        public void defineSignature(Symbol symbol, Type type) {
            define(symbol).defineSignature(type);
        }

        @Override
        public void defineValue(Symbol symbol, Type type) {
            define(symbol).defineValue(type);
        }

        @Override
        public Scope enterScope() {
            ChildScope child = scope(this, types);
            children.add(child);
            return child;
        }

        @Override
        public Scope enterScope(String moduleName, List<Import> imports) {
            throw new IllegalStateException();
        }

        @Override
        public void extendContext(Type type, Set<Symbol> additionalContext) {
            types.extendContext(type, additionalContext);
        }

        @Override
        public void generalize(Type type) {
            types.generalize(type);
        }

        @Override
        public Type generate(Type type) {
            return types.generate(type);
        }

        @Override
        public Type genericCopy(Type type) {
            return types.genericCopy(type);
        }

        @Override
        public List<String> getCaptures() {
            return ImmutableList.copyOf(captures);
        }

        @Override
        public Set<Symbol> getContext(Type type) {
            return ImmutableSet.<Symbol>builder()
                .addAll(types.getContext(type))
                .addAll(parent.getContext(type))
                .build();
        }

        @Override
        public Set<Symbol> getDependencies() {
            return new HashSet<>(dependencies);
        }

        @Override
        public List<String> getLocals() {
            return ImmutableList.copyOf(locals);
        }

        @Override
        public TypeClassDescriptor getMemberOf(ValueReference valueRef) {
            return parent.getMemberOf(valueRef);
        }

        @Override
        public Operator getOperator(Symbol symbol) {
            return requireEntry(symbol).getOperator();
        }

        @Override
        public Scope getParent() {
            return parent;
        }

        public Map<Symbol, List<PatternMatcher>> getPatterns() {
            return patterns;
        }

        @Override
        public Type getRawValue(Symbol symbol) {
            return requireEntry(symbol).getValue();
        }

        @Override
        public Optional<Type> getSignature(Symbol symbol) {
            return requireEntry(symbol).getSignature().map(this::genericCopy);
        }

        @Override
        public Type getTarget(Type type) {
            return types.getTarget(type);
        }

        @Override
        public TypeClassDescriptor getTypeClass(ClassReference classRef) {
            return parent.getTypeClass(classRef);
        }

        @Override
        public Set<TypeInstanceDescriptor> getTypeInstances(Symbol typeClass, List<Type> parameters) {
            return parent.getTypeInstances(typeClass, parameters);
        }

        @Override
        public Optional<MethodSignature> getValueSignature(Symbol symbol) {
            return parent.getValueSignature(symbol);
        }

        @Override
        public void insert(Scope scope) {
            insert_((ChildScope) scope);
        }

        @Override
        public boolean isBound(VariableType type) {
            return types.isBound(type);
        }

        @Override
        public boolean isDefined(Symbol symbol) {
            return symbol.accept(new SymbolVisitor<Boolean>() {
                @Override
                public Boolean visit(QualifiedSymbol symbol) {
                    return parent.isDefined(symbol);
                }

                @Override
                public Boolean visit(UnqualifiedSymbol symbol) {
                    return isDefinedLocally(symbol) || parent.isDefined(symbol);
                }
            });
        }

        @Override
        public boolean isOperator_(Symbol symbol) {
            return parent.isOperator_(symbol);
        }

        @Override
        public Scope leaveScope() {
            return parent;
        }

        @Override
        public void prependLocals(List<String> locals) {
            this.locals.addAll(0, locals);
        }

        @Override
        public Optional<Symbol> qualify(Symbol symbol) {
            return symbol.accept(new SymbolVisitor<Optional<Symbol>>() {
                @Override
                public Optional<Symbol> visit(QualifiedSymbol symbol) {
                    return parent.qualify(symbol);
                }

                @Override
                public Optional<Symbol> visit(UnqualifiedSymbol symbol) {
                    if (isDefinedLocally(symbol)) {
                        return Optional.of(symbol);
                    } else {
                        return parent.qualify(symbol);
                    }
                }
            });
        }

        @Override
        public Symbol qualifyCurrent(Symbol symbol) {
            return parent.qualifyCurrent(symbol);
        }

        @Override
        public Symbol reserveSymbol() {
            return parent.reserveSymbol();
        }

        @Override
        public void setParent(Scope parent) {
            this.parent = parent;
        }

        @Override
        public void specialize(Type type) {
            types.specialize(type);
        }

        private SymbolEntry define(Symbol symbol) {
            return symbol.accept(new SymbolVisitor<SymbolEntry>() {
                @Override
                public SymbolEntry visit(QualifiedSymbol symbol) {
                    throw new IllegalStateException("Can't define symbol with qualified name " + symbol.quote());
                }

                @Override
                public SymbolEntry visit(UnqualifiedSymbol symbol) {
                    return entries.computeIfAbsent(symbol, k -> mutableEntry(symbol));
                }
            });
        }

        private void insert_(ChildScope scope) {
            scope.children.clear();
            scope.children.addAll(children);
            children.forEach(child -> child.setParent(scope));
            children.clear();
            children.add(scope);
        }

        private SymbolEntry requireEntry(Symbol symbol) {
            return getEntry(symbol).orElseThrow(() -> new SymbolNotFoundException("Could not find symbol " + symbol.quote()));
        }

        @Override
        protected Optional<SymbolEntry> getEntry(Symbol symbol) {
            Optional<SymbolEntry> localEntry = Optional.ofNullable(entries.get(symbol));
            if (localEntry.isPresent()) {
                return localEntry;
            } else {
                return parent.getEntry(symbol);
            }
        }

        @Override
        protected boolean isDefinedLocally(Symbol symbol) {
            return entries.containsKey(symbol);
        }
    }

    public static class ModuleScope extends Scope {

        private final Scope                             parent;
        private final TypeScope                         types;
        private final SymbolResolver                    resolver;
        private final String                            moduleName;
        private final List<Import>                      imports;
        private final Map<Symbol, SymbolEntry>          entries;
        private final Map<Symbol, List<PatternMatcher>> patterns;
        private final Set<Symbol>                       dependencies;

        private ModuleScope(Scope parent, TypeScope types, SymbolResolver resolver, String moduleName, List<Import> imports) {
            this.parent = parent;
            this.types = types;
            this.resolver = resolver;
            this.moduleName = moduleName;
            this.imports = ImmutableList.copyOf(imports);
            this.entries = new HashMap<>();
            this.patterns = new LinkedHashMap<>();
            this.dependencies = new HashSet<>();
        }

        @Override
        public void addDependency(Symbol symbol) {
            if (!isExternal(symbol)) {
                dependencies.add(symbol);
            }
        }

        @Override
        public void addPattern(Symbol symbol, PatternMatcher pattern) {
            patterns.computeIfAbsent(symbol, k -> new ArrayList<>()).add(pattern);
        }

        @Override
        public Unification bind(VariableType variableType, Type target) {
            return types.bind(variableType, target);
        }

        @Override
        public void defineOperator(Symbol symbol, Operator operator) {
            define(symbol).defineOperator(operator);
        }

        @Override
        public void defineSignature(Symbol symbol, Type type) {
            define(symbol).defineSignature(type);
        }

        @Override
        public void defineValue(Symbol symbol, Type type) {
            define(symbol).defineValue(type);
        }

        @Override
        public Scope enterScope() {
            return scope(this, types);
        }

        @Override
        public Scope enterScope(String moduleName, List<Import> imports) {
            throw new IllegalStateException();
        }

        @Override
        public void extendContext(Type type, Set<Symbol> additionalContext) {
            types.extendContext(type, additionalContext);
        }

        @Override
        public void generalize(Type type) {
            types.generalize(type);
        }

        @Override
        public Type generate(Type type) {
            return types.generate(type);
        }

        @Override
        public Type genericCopy(Type type) {
            return types.genericCopy(type);
        }

        @Override
        public Set<Symbol> getContext(Type type) {
            return ImmutableSet.<Symbol>builder()
                .addAll(types.getContext(type))
                .addAll(imports.stream()
                    .map(import_ -> import_.getContext(type, resolver))
                    .flatMap(Collection::stream)
                    .collect(toSet()))
                .build();
        }

        @Override
        public Set<Symbol> getDependencies() {
            return new HashSet<>(dependencies);
        }

        @Override
        public TypeClassDescriptor getMemberOf(ValueReference valueRef) {
            return resolver.getEntry(valueRef.getSymbol())
                .map(SymbolEntry::getMemberOf)
                .map(symbol -> getTypeClass(classRef(symbol)))
                .orElseThrow(() -> new SymbolNotFoundException("Could not get parent class of symbol " + valueRef.getSymbol().quote()));
        }

        @Override
        public Operator getOperator(Symbol symbol) {
            return getEntry(symbol)
                .map(SymbolEntry::getOperator)
                .orElseThrow(() -> new SymbolNotFoundException("Symbol " + symbol.quote() + " is not an operator"));
        }

        @Override
        public Scope getParent() {
            return parent;
        }

        @Override
        public Map<Symbol, List<PatternMatcher>> getPatterns() {
            return patterns;
        }

        @Override
        public Type getRawValue(Symbol symbol) {
            return getEntry(symbol)
                .map(SymbolEntry::getValue)
                .orElseThrow(() -> new SymbolNotFoundException("Symbol " + symbol.quote() + " is not a value"));
        }

        @Override
        public Optional<Type> getSignature(Symbol symbol) {
            return getEntry(symbol).flatMap(SymbolEntry::getSignature).map(this::genericCopy);
        }

        @Override
        public Type getTarget(Type type) {
            return types.getTarget(type);
        }

        @Override
        public TypeClassDescriptor getTypeClass(ClassReference classRef) {
            return resolver.getEntry(classRef.getSymbol())
                .map(SymbolEntry::getTypeClass)
                .orElseThrow(() -> new SymbolNotFoundException("Symbol " + classRef.getSymbol().quote() + " is not a type class"));
        }

        @Override
        public Set<TypeInstanceDescriptor> getTypeInstances(Symbol typeClass, List<Type> parameters) {
            return resolver.getTypeInstances(typeClass, parameters);
        }

        @Override
        public Optional<MethodSignature> getValueSignature(Symbol symbol) {
            return parent.getValueSignature(symbol);
        }

        @Override
        public boolean isBound(VariableType type) {
            return types.isBound(type);
        }

        @Override
        public boolean isDefined(Symbol symbol) {
            return getEntry(symbol).isPresent();
        }

        @Override
        public boolean isOperator_(Symbol symbol) {
            return getEntry(symbol).map(SymbolEntry::isOperator).orElse(false);
        }

        @Override
        public Scope leaveScope() {
            return parent;
        }

        @Override
        public Optional<Symbol> qualify(Symbol symbol) {
            return symbol.accept(new SymbolVisitor<Optional<Symbol>>() {
                @Override
                public Optional<Symbol> visit(QualifiedSymbol symbol) {
                    if (Objects.equals(moduleName, symbol.getModuleName())) {
                        return Optional.of(symbol);
                    } else {
                        return imports.stream()
                            .filter(i -> i.isFrom(symbol.getModuleName()))
                            .findFirst()
                            .flatMap(i -> i.qualify(symbol.getMemberName(), resolver));
                    }
                }

                @Override
                public Optional<Symbol> visit(UnqualifiedSymbol symbol) {
                    Symbol qualified = symbol.qualifyWith(moduleName);
                    if (isDefinedLocally(qualified)) {
                        return Optional.of(qualified);
                    } else {
                        return imports.stream()
                            .map(i -> i.qualify(symbol.getMemberName(), resolver))
                            .filter(Optional::isPresent)
                            .findFirst()
                            .flatMap(s -> s);
                    }
                }
            });
        }

        @Override
        public Symbol qualifyCurrent(Symbol symbol) {
            return symbol.qualifyWith(moduleName);
        }

        @Override
        public Symbol reserveSymbol() {
            return parent.reserveSymbol().qualifyWith(moduleName);
        }

        @Override
        public void specialize(Type type) {
            types.specialize(type);
        }

        private SymbolEntry define(Symbol symbol) {
            return symbol.accept(new SymbolVisitor<SymbolEntry>() {
                @Override
                public SymbolEntry visit(QualifiedSymbol symbol) {
                    if (Objects.equals(moduleName, symbol.getModuleName())) {
                        return entries.computeIfAbsent(symbol, k -> mutableEntry(symbol));
                    } else {
                        throw new IllegalArgumentException("Can't define symbol " + symbol.quote() + " within different module " + quote(moduleName));
                    }
                }

                @Override
                public SymbolEntry visit(UnqualifiedSymbol symbol) {
                    throw new IllegalArgumentException("Can't define unqualified symbol " + symbol.quote());
                }
            });
        }

        @Override
        protected Optional<SymbolEntry> getEntry(Symbol symbol) {
            return symbol.accept(new SymbolVisitor<Optional<SymbolEntry>>() {
                @Override
                public Optional<SymbolEntry> visit(QualifiedSymbol symbol) {
                    Optional<SymbolEntry> optionalEntry = Optional.ofNullable(entries.get(symbol));
                    if (optionalEntry.isPresent()) {
                        return optionalEntry;
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
        protected boolean isDefinedLocally(Symbol symbol) {
            return entries.containsKey(symbol);
        }
    }

    public static class RootScope extends Scope {

        private final SymbolGenerator    symbolGenerator;
        private final RootResolver       resolver;
        private final Map<String, Scope> children;

        private RootScope(SymbolGenerator symbolGenerator, SymbolResolver resolver) {
            this.symbolGenerator = symbolGenerator;
            this.resolver = new RootResolver(resolver);
            this.children = new HashMap<>();
        }

        @Override
        public void addDependency(Symbol symbol) {
            throw new IllegalStateException();
        }

        @Override
        public void addPattern(Symbol symbol, PatternMatcher pattern) {
            throw new IllegalStateException();
        }

        @Override
        public Unification bind(VariableType variableType, Type target) {
            throw new IllegalStateException();
        }

        @Override
        public void defineOperator(Symbol symbol, Operator operator) {
            throw new IllegalStateException();
        }

        @Override
        public void defineSignature(Symbol symbol, Type type) {
            throw new IllegalStateException();
        }

        @Override
        public void defineValue(Symbol symbol, Type type) {
            throw new IllegalStateException();
        }

        @Override
        public Scope enterScope() {
            throw new IllegalStateException();
        }

        @Override
        public Scope enterScope(String moduleName, List<Import> imports) {
            Scope scope = scope(this, new DefaultTypeScope(symbolGenerator), resolver, moduleName, imports);
            children.put(moduleName, scope);
            return scope;
        }

        @Override
        public void extendContext(Type type, Set<Symbol> additionalContext) {
            throw new IllegalStateException();
        }

        @Override
        public void generalize(Type type) {
            throw new IllegalStateException();
        }

        @Override
        public Type generate(Type type) {
            throw new IllegalStateException();
        }

        @Override
        public Type genericCopy(Type type) {
            throw new IllegalStateException();
        }

        @Override
        public Set<Symbol> getContext(Type type) {
            throw new IllegalStateException();
        }

        @Override
        public Set<Symbol> getDependencies() {
            throw new IllegalStateException();
        }

        @Override
        public TypeClassDescriptor getMemberOf(ValueReference valueRef) {
            throw new IllegalStateException();
        }

        @Override
        public Operator getOperator(Symbol symbol) {
            throw new IllegalStateException();
        }

        @Override
        public Scope getParent() {
            throw new IllegalStateException();
        }

        @Override
        public Map<Symbol, List<PatternMatcher>> getPatterns() {
            throw new IllegalStateException();
        }

        @Override
        public Type getRawValue(Symbol symbol) {
            throw new IllegalStateException();
        }

        @Override
        public Optional<Type> getSignature(Symbol symbol) {
            throw new IllegalStateException();
        }

        @Override
        public Type getTarget(Type type) {
            throw new IllegalStateException();
        }

        @Override
        public TypeClassDescriptor getTypeClass(ClassReference classRef) {
            throw new IllegalStateException();
        }

        @Override
        public Set<TypeInstanceDescriptor> getTypeInstances(Symbol typeClass, List<Type> parameters) {
            throw new IllegalStateException();
        }

        @Override
        public Optional<MethodSignature> getValueSignature(Symbol symbol) {
            return resolver.getEntry(symbol).map(SymbolEntry::getValueSignature);
        }

        @Override
        public boolean isBound(VariableType type) {
            throw new IllegalStateException();
        }

        @Override
        public boolean isDefined(Symbol symbol) {
            return resolver.isDefined(symbol);
        }

        @Override
        public boolean isOperator_(Symbol symbol) {
            return symbol.accept(new SymbolVisitor<Boolean>() {
                @Override
                public Boolean visit(QualifiedSymbol symbol) {
                    return children.containsKey(symbol.getModuleName()) && children.get(symbol.getModuleName()).isOperator_(symbol);
                }

                @Override
                public Boolean visit(UnqualifiedSymbol symbol) {
                    return false;
                }
            });
        }

        @Override
        public Scope leaveScope() {
            throw new IllegalStateException("Can't leave root scope");
        }

        @Override
        public Optional<Symbol> qualify(Symbol symbol) {
            return symbol.accept(new SymbolVisitor<Optional<Symbol>>() {
                @Override
                public Optional<Symbol> visit(QualifiedSymbol symbol) {
                    if (resolver.isDefined(symbol)) {
                        return Optional.of(symbol);
                    } else {
                        throw new SymbolNotFoundException("Could not qualify undefined symbol " + symbol.quote());
                    }
                }

                @Override
                public Optional<Symbol> visit(UnqualifiedSymbol symbol) {
                    throw new SymbolNotFoundException("Could not qualify undefined symbol " + symbol.quote());
                }
            });
        }

        @Override
        public Symbol qualifyCurrent(Symbol symbol) {
            throw new IllegalStateException();
        }

        @Override
        public Symbol reserveSymbol() {
            return symbolGenerator.reserveSymbol();
        }

        @Override
        public Type reserveType() {
            return symbolGenerator.reserveType();
        }

        @Override
        public void specialize(Type type) {
            throw new IllegalStateException();
        }

        @Override
        protected Optional<SymbolEntry> getEntry(Symbol symbol) {
            return symbol.accept(new SymbolVisitor<Optional<SymbolEntry>>() {
                @Override
                public Optional<SymbolEntry> visit(QualifiedSymbol symbol) {
                    return resolver.getEntry(symbol);
                }

                @Override
                public Optional<SymbolEntry> visit(UnqualifiedSymbol symbol) {
                    return Optional.empty();
                }
            });
        }

        @Override
        protected boolean isDefinedLocally(Symbol symbol) {
            return false;
        }

        @Override
        protected boolean isExternal(Symbol symbol) {
            return resolver.isExternal(symbol);
        }

        private final class RootResolver implements SymbolResolver {

            private final SymbolResolver resolver;

            public RootResolver(SymbolResolver resolver) {
                this.resolver = resolver;
            }

            @Override
            public Optional<SymbolEntry> getEntry(Symbol symbol) {
                return symbol.accept(new SymbolVisitor<Optional<SymbolEntry>>() {
                    @Override
                    public Optional<SymbolEntry> visit(QualifiedSymbol symbol) {
                        if (children.containsKey(symbol.getModuleName()) && children.get(symbol.getModuleName()).isDefinedLocally(symbol)) {
                            return children.get(symbol.getModuleName()).getEntry(symbol);
                        } else {
                            return resolver.getEntry(symbol);
                        }
                    }

                    @Override
                    public Optional<SymbolEntry> visit(UnqualifiedSymbol symbol) {
                        return Optional.empty();
                    }
                });
            }

            @Override
            public Set<TypeInstanceDescriptor> getTypeInstances(Symbol symbol, List<Type> types) {
                return resolver.getTypeInstances(symbol, types);
            }

            @Override
            public Set<TypeInstanceDescriptor> getTypeInstancesByModule(String moduleName) {
                return resolver.getTypeInstancesByModule(moduleName);
            }

            @Override
            public boolean isDefined(Symbol symbol) {
                return symbol.accept(new SymbolVisitor<Boolean>() {
                    @Override
                    public Boolean visit(QualifiedSymbol symbol) {
                        return children.containsKey(symbol.getModuleName()) && children.get(symbol.getModuleName()).isDefinedLocally(symbol)
                            || resolver.isDefined(symbol);
                    }

                    @Override
                    public Boolean visit(UnqualifiedSymbol symbol) {
                        return false;
                    }
                });
            }

            public boolean isExternal(Symbol symbol) {
                return resolver.isDefined(symbol);
            }
        }
    }
}
