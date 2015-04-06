package scotch.compiler.steps;

import static java.util.stream.Collectors.toList;
import static scotch.compiler.syntax.definition.DefinitionEntry.entry;
import static scotch.compiler.syntax.reference.DefinitionReference.rootRef;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import scotch.compiler.syntax.definition.Definition;
import scotch.compiler.syntax.definition.DefinitionEntry;
import scotch.compiler.syntax.definition.DefinitionGraph;
import scotch.compiler.syntax.pattern.CaptureMatch;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.PatternMatcher;
import scotch.compiler.syntax.value.Value;

public class PatternReducerStep implements PatternReducer {

    private final DefinitionGraph       graph;
    private final List<DefinitionEntry> entries;
    private final Deque<Scope>          scopes;

    public PatternReducerStep(DefinitionGraph graph) {
        this.graph = graph;
        this.entries = new ArrayList<>();
        this.scopes = new ArrayDeque<>();
    }

    @Override
    public void addAssignment(CaptureMatch capture) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void addCondition(Value condition) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void beginPattern(PatternMatcher matcher) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void beginPatternCase(Value body) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void endPattern() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void endPatternCase() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value reducePattern() {
        throw new UnsupportedOperationException(); // TODO
    }

    public DefinitionGraph reducePatterns() {
        getDefinition(rootRef())
            .orElseThrow(() -> new IllegalStateException("Root definition not found"))
            .reducePatterns(this);
        return graph
            .copyWith(entries)
            .build();
    }

    public List<DefinitionReference> reducePatterns(List<DefinitionReference> references) {
        return references.stream()
            .map(this::getDefinition)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(definition -> definition.reducePatterns(this))
            .map(Definition::getReference)
            .collect(toList());
    }

    public Definition scoped(Definition definition, Supplier<Definition> runnable) {
        scopes.push(graph.getScope(definition.getReference()));
        try {
            Definition result = runnable.get();
            entries.add(entry(scopes.peek(), result));
            return result;
        } finally {
            scopes.pop();
        }
    }

    private Optional<Definition> getDefinition(DefinitionReference reference) {
        return graph.getDefinition(reference);
    }
}
