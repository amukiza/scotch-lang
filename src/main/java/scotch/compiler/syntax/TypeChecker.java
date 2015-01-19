package scotch.compiler.syntax;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import scotch.compiler.error.SyntaxError;
import scotch.compiler.symbol.Symbol;
import scotch.compiler.symbol.Type;
import scotch.compiler.symbol.Type.InstanceType;
import scotch.compiler.symbol.TypeScope;
import scotch.compiler.syntax.definition.Definition;
import scotch.compiler.syntax.definition.DefinitionGraph;
import scotch.compiler.syntax.definition.ValueDefinition;
import scotch.compiler.syntax.definition.ValueSignature;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.Method;
import scotch.compiler.syntax.value.Value;

public interface TypeChecker extends TypeScope {

    void addLocal(Symbol symbol);

    Definition bind(ValueDefinition definition);

    void capture(Symbol symbol);

    default List<DefinitionReference> checkTypes(List<DefinitionReference> references) {
        return map(references, Definition::checkTypes);
    }

    <T extends Scoped> T enclose(T scoped, Supplier<T> supplier);

    void error(SyntaxError error);

    Value findArgument(InstanceType type);

    Value findInstance(Method method, InstanceType instanceType);

    void fromSorted(BiFunction<? super Definition, TypeChecker, ? extends Definition> function);

    Optional<Definition> getDefinition(DefinitionReference reference);

    DefinitionGraph getGraph();

    Type getType(ValueDefinition definition);

    Definition keep(Definition definition);

    List<DefinitionReference> map(List<DefinitionReference> references, BiFunction<? super Definition, TypeChecker, ? extends Definition> function);

    void redefine(ValueDefinition definition);

    void redefine(ValueSignature signature);

    Type reserveType();

    Scope scope();

    <T extends Scoped> T scoped(T scoped, Supplier<T> supplier);
}