package scotch.compiler.symbol.type;

import static me.qmx.jitescript.util.CodegenUtils.p;
import static scotch.compiler.symbol.Unification.circular;
import static scotch.compiler.symbol.Unification.mismatch;
import static scotch.compiler.symbol.Unification.unified;
import static scotch.compiler.symbol.type.Types.fn;
import static scotch.compiler.symbol.type.Types.unifyVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import com.google.common.collect.ImmutableSortedSet;
import scotch.compiler.steps.NameQualifier;
import scotch.compiler.symbol.Symbol;
import scotch.compiler.symbol.TypeScope;
import scotch.compiler.symbol.Unification;
import scotch.compiler.text.SourceRange;
import scotch.compiler.util.Pair;
import scotch.runtime.Applicable;

public class FunctionType extends Type {

    private final SourceRange sourceRange;
    private final Type        argument;
    private final Type        result;

    FunctionType(SourceRange sourceRange, Type argument, Type result) {
        this.sourceRange = sourceRange;
        this.argument = argument;
        this.result = result;
    }

    @Override
    public Unification apply(SumType sum, TypeScope scope) {
        return rebind(scope).map(type -> ((FunctionType) type).apply_(sum, scope));
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof FunctionType) {
            FunctionType other = (FunctionType) o;
            return argument.equals(other.argument) && result.equals(other.result);
        } else {
            return false;
        }
    }

    public Type getArgument() {
        return argument;
    }

    @Override
    public Map<String, Type> getContexts(Type type, TypeScope scope) {
        Map<String, Type> map = new HashMap<>();
        if (type instanceof FunctionType) {
            map.putAll(argument.getContexts(((FunctionType) type).getArgument(), scope));
            map.putAll(result.getContexts(((FunctionType) type).getResult(), scope));
        }
        return map;
    }

    @Override
    public List<Pair<VariableType, Symbol>> getInstanceMap() {
        List<Pair<VariableType, Symbol>> instances = new ArrayList<>();
        instances.addAll(argument.getInstanceMap());
        instances.addAll(result.getInstanceMap());
        return instances;
    }

    @Override
    public Class<?> getJavaType() {
        return Applicable.class;
    }

    public Type getResult() {
        return result;
    }

    @Override
    public String getSignature() {
        return "(" + argument.getSignature_() + ");" + result.getSignature_();
    }

    @Override
    public SourceRange getSourceRange() {
        return sourceRange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(argument, result);
    }

    @Override
    public Type qualifyNames(NameQualifier qualifier) {
        return withArgument(argument.qualifyNames(qualifier)).withResult(result.qualifyNames(qualifier));
    }

    @Override
    public Unification rebind(TypeScope scope) {
        return argument.rebind(scope).map(
            argResult -> result.rebind(scope).map(
                resultResult -> unified(withArgument(argResult).withResult(resultResult))));
    }

    @Override
    public String toString() {
        return gatherContext() + toString_();
    }

    @Override
    protected Optional<List<Pair<Type, Type>>> zip_(Type other) {
        return other.zipWith(this);
    }

    @Override
    protected Optional<List<Pair<Type, Type>>> zipWith(FunctionType target) {
        return target.argument.zip_(argument).flatMap(
            argumentList -> target.result.zip_(result).map(
                resultList -> new ArrayList<Pair<Type, Type>>() {{
                    addAll(argumentList);
                    addAll(resultList);
                }}));
    }

    public FunctionType withArgument(Type argument) {
        return new FunctionType(sourceRange, argument, result);
    }

    public FunctionType withResult(Type result) {
        return new FunctionType(sourceRange, argument, result);
    }

    public FunctionType withSourceRange(SourceRange sourceRange) {
        return new FunctionType(sourceRange, argument, result);
    }

    private Unification apply_(SumType sum, TypeScope scope) {
        return argument.apply(sum, scope)
            .map(argResult -> result.apply(sum, scope)
                .map(resultResult -> unified(withArgument(argResult).withResult(resultResult))));
    }

    @Override
    protected boolean contains(VariableType type) {
        return argument.contains(type) || result.contains(type);
    }

    @Override
    protected Set<Pair<VariableType, Symbol>> gatherContext_() {
        Set<Pair<VariableType, Symbol>> context = new HashSet<>();
        context.addAll(argument.gatherContext_());
        context.addAll(result.gatherContext_());
        return ImmutableSortedSet.copyOf(Types::sort, context);
    }

    @Override
    protected Type generate(TypeScope scope, Set<Type> visited) {
        return new FunctionType(sourceRange, argument.generate(scope), result.generate(scope));
    }

    @Override
    protected Type genericCopy(TypeScope scope, Map<Type, Type> mappings) {
        return new FunctionType(
            sourceRange,
            argument.genericCopy(scope, mappings),
            result.genericCopy(scope, mappings)
        );
    }

    @Override
    protected String getSignature_() {
        return p(Function.class);
    }

    @Override
    protected String toParenthesizedString() {
        return "(" + argument.toParenthesizedString() + " -> " + result.toString_() + ")";
    }

    @Override
    protected String toString_() {
        return argument.toParenthesizedString() + " -> " + result.toString_();
    }

    @Override
    protected Unification unifyWith(SumType target, TypeScope scope) {
        return mismatch(target, this);
    }

    @Override
    protected Unification unifyWith(VariableSum target, TypeScope scope) {
        return mismatch(target, this);
    }

    @Override
    protected Unification unifyWith(FunctionType target, TypeScope scope) {
        return target.argument.unify(argument, scope).map(
            argumentResult -> target.result.unify(result, scope).map(
                resultResult -> unified(fn(argumentResult, resultResult))
            )
        );
    }

    @Override
    protected Unification unifyWith(VariableType target, TypeScope scope) {
        if (contains(target)) {
            return circular(target, this);
        } else {
            return unifyVariable(this, target, scope);
        }
    }

    @Override
    protected Unification unify_(Type type, TypeScope scope) {
        return type.unifyWith(this, scope);
    }
}