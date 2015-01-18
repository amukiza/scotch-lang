package scotch.compiler.symbol;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import scotch.compiler.error.SyntaxError;
import scotch.compiler.syntax.Scoped;
import scotch.compiler.syntax.definition.Definition;
import scotch.compiler.syntax.definition.DefinitionGraph;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.PatternMatcher;
import scotch.compiler.text.SourceRange;

public interface NameQualifier {

    Definition collect(Definition definition);

    Definition collect(PatternMatcher pattern);

    default void defineOperator(Symbol symbol, Operator operator) {
        scope().defineOperator(symbol, operator);
    }

    default void defineValue(Symbol symbol, Type type) {
        scope().defineValue(symbol, type);
    }

    void enterScope(Definition definition);

    void error(SyntaxError error);

    Optional<Definition> getDefinition(DefinitionReference reference);

    DefinitionGraph getGraph();

    default boolean isOperator(Symbol symbol) {
        return scope().isOperator(symbol);
    }

    @SuppressWarnings("unchecked")
    <T extends Scoped> T keep(Scoped scoped);

    void leaveScope();

    List<DefinitionReference> map(List<DefinitionReference> references, BiFunction<? super Definition, NameQualifier, ? extends Definition> function);

    void qualifyNames();

    Scope scope();

    <T extends Definition> T scoped(T definition, Supplier<? extends T> supplier);

    <T extends Scoped> T scoped(Scoped value, Supplier<? extends T> supplier);

    void symbolNotFound(Symbol symbol, SourceRange sourceRange);

    Optional<Symbol> qualify(Symbol symbol);
}
