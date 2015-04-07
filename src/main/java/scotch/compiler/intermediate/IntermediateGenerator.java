package scotch.compiler.intermediate;

import static scotch.compiler.intermediate.Intermediates.value;
import static scotch.compiler.syntax.reference.DefinitionReference.rootRef;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import com.google.common.collect.ImmutableList;
import scotch.compiler.error.CompileException;
import scotch.compiler.syntax.Scoped;
import scotch.compiler.syntax.definition.DefinitionGraph;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.syntax.scope.Scope;
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

    public void defineValue(DefinitionReference reference, Type type, IntermediateValue body) {
        definitions.add(value(reference, type, body));
    }

    public IntermediateGraph generateIntermediateCode() {
        if (graph.hasErrors()) {
            throw new CompileException(graph.getErrors());
        } else {
            graph.getDefinition(rootRef()).get().generateIntermediateCode(this);
            return new IntermediateGraph(definitions);
        }
    }

    public void generateIntermediateCode(DefinitionReference reference) {
        graph.getDefinition(reference).get().generateIntermediateCode(this);
    }

    public void reference(String name) {
        if (!references.contains(name)) {
            references.add(name);
        }
    }

    public <T extends Scoped> void scoped(T scoped, Runnable runnable) {
        scopes.push(graph.getScope(scoped.getReference()));
        try {
            runnable.run();
        } finally {
            scopes.pop();
        }
    }
}
