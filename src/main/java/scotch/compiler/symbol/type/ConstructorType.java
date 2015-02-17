package scotch.compiler.symbol.type;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.symbol.Unification.mismatch;
import static scotch.compiler.symbol.Unification.unified;
import static scotch.compiler.symbol.type.Types.unifyVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import scotch.compiler.steps.NameQualifier;
import scotch.compiler.symbol.Symbol;
import scotch.compiler.symbol.TypeScope;
import scotch.compiler.symbol.Unification;
import scotch.compiler.text.SourceRange;
import scotch.compiler.util.Pair;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode
public class ConstructorType extends Type {

    private final Type head;
    private final Type tail;

    public Unification apply(SumType type, TypeScope scope) {
        return type.apply(head, scope).unify(
            (appliedType, remainingParameters) -> {
                if (remainingParameters.isEmpty()) {
                    return unified(new ConstructorType(appliedType, tail).flatten());
                } else {
                    return tail.apply(appliedType, remainingParameters, scope);
                }
            });
    }

    @Override
    public Type flatten() {
        return head.flatten(tail.flatten_());
    }

    @Override
    public Map<String, Type> getContexts(Type type, TypeScope scope) {
        return new HashMap<String, Type>() {{
            putAll(head.getContexts(type, scope));
            putAll(tail.getContexts(type, scope));
        }};
    }

    @Override
    public Class<?> getJavaType() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public String getSignature() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public SourceRange getSourceRange() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Type qualifyNames(NameQualifier qualifier) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    protected boolean contains(VariableType type) {
        return head.contains(type) || tail.contains(type);
    }

    @Override
    protected List<Type> flatten_() {
        return new ArrayList<Type>() {{
            addAll(head.flatten_());
            addAll(tail.flatten_());
        }};
    }

    @Override
    protected Set<Pair<VariableType, Symbol>> gatherContext_() {
        return new HashSet<Pair<VariableType, Symbol>>() {{
            addAll(head.gatherContext_());
            addAll(tail.gatherContext_());
        }};
    }

    @Override
    protected Type generate(TypeScope scope, Set<Type> visited) {
        return new ConstructorType(head.generate(scope, visited), tail.generate(scope, visited)).flatten();
    }

    @Override
    protected Type genericCopy(TypeScope scope, Map<Type, Type> mappings) {
        return new ConstructorType(
            head.genericCopy(scope, mappings),
            tail.genericCopy(scope, mappings)
        );
    }

    @Override
    protected String getSignature_() {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    protected String toParenthesizedString() {
        return toString_();
    }

    @Override
    protected String toString_() {
        return "Λ(" + head.toString_() + ", " + tail.toString_() + ")";
    }

    @Override
    protected Unification unifyWith(ConstructorType target, TypeScope scope) {
        return head.unify(target.head, scope)
            .map(checkedHead -> tail.unify(target.tail, scope)
                .map(checkedTail -> unified(new ConstructorType(checkedHead, checkedTail))));
    }

    @Override
    protected Unification unifyWith(FunctionType target, TypeScope scope) {
        return mismatch(target, this);
    }

    @Override
    protected Unification unifyWith(VariableType target, TypeScope scope) {
        return unifyVariable(this, target, scope);
    }

    @Override
    protected Unification unifyWith(SumType target, TypeScope scope) {
        return target.unify(flatten(), scope); // TODO
    }

    @Override
    protected Unification unify_(Type type, TypeScope scope) {
        return type.unifyWith(this, scope);
    }
}
