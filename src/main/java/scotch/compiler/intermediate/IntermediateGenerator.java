package scotch.compiler.intermediate;

import static java.util.stream.Collectors.toList;
import static scotch.compiler.intermediate.Intermediates.data;
import static scotch.compiler.intermediate.Intermediates.module;
import static scotch.compiler.intermediate.Intermediates.root;
import static scotch.compiler.intermediate.Intermediates.value;
import static scotch.compiler.syntax.reference.DefinitionReference.dataRef;
import static scotch.compiler.syntax.reference.DefinitionReference.moduleRef;
import static scotch.compiler.syntax.reference.DefinitionReference.rootRef;
import static scotch.compiler.syntax.reference.DefinitionReference.valueRef;
import static scotch.util.StringUtil.quote;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;
import scotch.compiler.error.CompileException;
import scotch.compiler.syntax.Scoped;
import scotch.compiler.syntax.definition.DefinitionGraph;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.syntax.reference.InstanceReference;
import scotch.compiler.syntax.reference.ValueReference;
import scotch.compiler.syntax.scope.Scope;
import scotch.symbol.FieldSignature;
import scotch.symbol.MethodSignature;
import scotch.symbol.Symbol;
import scotch.symbol.descriptor.DataConstructorDescriptor;
import scotch.symbol.type.Type;

public class IntermediateGenerator {

    private final DefinitionGraph              graph;
    private final List<IntermediateDefinition> definitions;
    private final Deque<Scope>                 scopes;
    private final List<String>                 references;

    public IntermediateGenerator(DefinitionGraph graph) {
        this.graph = graph;
        this.definitions = new ArrayList<>();
        this.scopes = new ArrayDeque<>();
        this.references = new ArrayList<>();
    }

    public void addArgument(String name) {
        references.remove(name);
    }

    public List<String> capture() {
        return ImmutableList.copyOf(references);
    }

    public IntermediateValue constantReference(Symbol symbol, Symbol dataType, FieldSignature constantField) {
        return Intermediates.constantReference(symbol, dataType, constantField);
    }

    public IntermediateConstructor createConstructor(Symbol symbol, String className, MethodSignature methodSignature, List<IntermediateValue> arguments) {
        return Intermediates.constructor(symbol, className, methodSignature, arguments);
    }

    public Optional<DefinitionReference> defineData(Symbol symbol, List<Type> parameters, List<IntermediateConstructorDefinition> constructors) {
        definitions.add(data(symbol, parameters, constructors));
        return Optional.of(dataRef(symbol));
    }

    public DefinitionReference defineModule(String symbol, List<DefinitionReference> references) {
        definitions.add(module(symbol, references));
        return moduleRef(symbol);
    }

    public DefinitionReference defineRoot(List<DefinitionReference> references) {
        definitions.add(root(references));
        return rootRef();
    }

    public DefinitionReference defineValue(Symbol symbol, Type type, IntermediateValue body) {
        definitions.add(value(symbol, type, body));
        return valueRef(symbol);
    }

    public IntermediateGraph generateIntermediateCode() {
        if (graph.hasErrors()) {
            throw new CompileException(graph.getErrors());
        } else {
            graph.getDefinition(rootRef()).get().generateIntermediateCode(this);
            return new IntermediateGraph(definitions);
        }
    }

    public Optional<DefinitionReference> generateIntermediateCode(DefinitionReference reference) {
        return graph.getDefinition(reference).flatMap(definition -> definition.generateIntermediateCode(this));
    }

    public DataConstructorDescriptor getDataConstructor(Symbol constructor) {
        return scope().getDataConstructor(constructor)
            .orElseThrow(() -> new IllegalStateException("Constructor " + quote(constructor) + " not found"));
    }

    public MethodSignature instanceGetter(InstanceReference reference) {
        return scope().getTypeInstance(
            reference.getClassReference(),
            reference.getModuleReference(),
            reference.getParameters().stream()
                .map(parameter -> parameter.copy(scope()::reserveType))
                .collect(toList())
        ).get().getInstanceGetter();
    }

    public void reference(String name) {
        if (!references.contains(name)) {
            references.add(name);
        }
    }

    public <T extends Scoped> Optional<DefinitionReference> scoped(T scoped, Supplier<DefinitionReference> runnable) {
        scopes.push(graph.getScope(scoped.getReference()));
        try {
            return Optional.of(runnable.get());
        } finally {
            scopes.pop();
        }
    }

    public MethodSignature valueSignature(ValueReference reference) {
        return scope().getValueSignature(reference.getSymbol()).get();
    }

    private Scope scope() {
        return scopes.peek();
    }
}
