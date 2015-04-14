package scotch.compiler.analyzer;

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
import scotch.compiler.syntax.pattern.DefaultPatternReducer;
import scotch.compiler.syntax.pattern.PatternCase;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.FunctionValue;
import scotch.compiler.syntax.value.IsConstructor;
import scotch.compiler.syntax.value.PatternMatcher;
import scotch.compiler.syntax.value.Value;
import scotch.symbol.Symbol;
import scotch.symbol.type.VariableType;
import scotch.symbol.util.SymbolGenerator;

public class PatternAnalyzer implements PatternReducer {

    private final DefinitionGraph       graph;
    private final List<DefinitionEntry> entries;
    private final Deque<Scope>          scopes;
    private final PatternReducer        patternReducer;

    public PatternAnalyzer(DefinitionGraph graph) {
        this.graph = graph;
        this.entries = new ArrayList<>();
        this.scopes = new ArrayDeque<>();
        this.patternReducer = new DefaultPatternReducer(new SymbolGeneratorShim());
    }

    @Override
    public void addAssignment(CaptureMatch capture) {
        patternReducer.addAssignment(capture);
    }

    @Override
    public void addCondition(Value argument, Value value) {
        patternReducer.addCondition(argument, value);
    }

    @Override
    public void addCondition(IsConstructor constructor) {
        patternReducer.addCondition(constructor);
    }

    @Override
    public void addTaggedArgument(Value taggedArgument) {
        patternReducer.addTaggedArgument(taggedArgument);
    }

    @Override
    public void markFunction(FunctionValue function) {
        entries.add(entry(graph.getScope(function.getReference()), function.getDefinition()));
    }

    @Override
    public void beginPattern(PatternMatcher matcher) {
        entries.add(entry(graph.getScope(matcher.getReference()), matcher.getDefinition()));
        patternReducer.beginPattern(matcher);
    }

    @Override
    public void beginPatternCase(PatternCase patternCase) {
        entries.add(entry(graph.getScope(patternCase.getReference()), patternCase.getDefinition()));
        patternReducer.beginPatternCase(patternCase);
    }

    @Override
    public void endPattern() {
        patternReducer.endPattern();
    }

    @Override
    public void endPatternCase() {
        patternReducer.endPatternCase();
    }

    @Override
    public Value getTaggedArgument(Value argument) {
        return patternReducer.getTaggedArgument(argument);
    }

    public Definition keep(Definition definition) {
        entries.add(entry(graph.getScope(definition.getReference()), definition));
        return definition;
    }

    @Override
    public Value reducePattern() {
        return patternReducer.reducePattern();
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

    public Scope scope() {
        return scopes.peek();
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

    private class SymbolGeneratorShim implements SymbolGenerator {

        @Override
        public Symbol reserveSymbol() {
            return scope().reserveSymbol();
        }

        @Override
        public Symbol reserveSymbol(List<String> nestings) {
            return scope().reserveSymbol(nestings);
        }

        @Override
        public VariableType reserveType() {
            return scope().reserveType();
        }

        @Override
        public void startTypesAt(int counter) {
            throw new UnsupportedOperationException();
        }
    }
}
