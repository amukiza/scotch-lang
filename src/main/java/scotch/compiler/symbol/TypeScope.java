package scotch.compiler.symbol;

import java.util.Set;
import scotch.compiler.symbol.type.SumType;
import scotch.compiler.symbol.type.Type;
import scotch.compiler.symbol.type.VariableType;

public interface TypeScope {

    Unification bind(VariableType variableType, Type targetType);

    void extendContext(Type type, Set<Symbol> additionalContext);

    void generalize(Type type);

    Type generate(Type type);

    Set<Symbol> getContext(Type type);

    Type getTarget(Type type);

    void implement(Symbol typeClass, SumType type);

    boolean isBound(VariableType variableType);

    boolean isGeneric(VariableType variableType);

    default boolean isImplemented(Symbol typeClass, SumType type) {
        throw new UnsupportedOperationException(); // TODO
    }

    VariableType reserveType();

    void specialize(Type type);
}
