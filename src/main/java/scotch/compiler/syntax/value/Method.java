package scotch.compiler.syntax.value;

import static java.util.stream.Collectors.joining;
import static scotch.compiler.syntax.value.NoBindingError.noBinding;
import static scotch.compiler.syntax.value.Values.apply;
import static scotch.util.StringUtil.stringify;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.intermediate.Intermediates;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.syntax.reference.ValueReference;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;
import scotch.symbol.type.FunctionType;
import scotch.symbol.type.InstanceType;
import scotch.symbol.type.Type;

public class Method extends Value {

    private final SourceLocation sourceLocation;
    private final ValueReference reference;
    private final List<Type>     instances;
    private final Type           type;

    Method(SourceLocation sourceLocation, ValueReference reference, List<? extends Type> instances, Type type) {
        this.sourceLocation = sourceLocation;
        this.reference = reference;
        this.instances = ImmutableList.copyOf(instances);
        this.type = type;
    }

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        return Intermediates.valueRef(reference, state.valueSignature(reference));
    }

    @Override
    public Value bindMethods(TypeChecker typeChecker) {
        List<InstanceType> instanceTypes = new ArrayList<>();
        Type type = this.type;
        for (int i = 0; i < this.instances.size(); i++) {
            instanceTypes.add((InstanceType) ((FunctionType) type).getArgument());
            type = ((FunctionType) type).getResult();
        }
        Value result = this;
        for (InstanceType instanceType : instanceTypes) {
            Value typeArgument;
            if (instanceType.isBound()) {
                typeArgument = typeChecker.findInstance(this, instanceType);
            } else {
                Optional<Value> optionalTypeArgument = typeChecker.findArgument(instanceType);
                if (optionalTypeArgument.isPresent()) {
                    typeArgument = optionalTypeArgument.get();
                } else {
                    typeChecker.error(noBinding(reference.getSymbol(), sourceLocation));
                    return this;
                }
            }
            result = apply(result, typeArgument, ((FunctionType) result.getType()).getResult());
        }
        return result;
    }

    @Override
    public Value bindTypes(TypeChecker typeChecker) {
        return withType(typeChecker.generate(type));
    }

    @Override
    public Value checkTypes(TypeChecker typeChecker) {
        return this;
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Method) {
            Method other = (Method) o;
            return Objects.equals(sourceLocation, other.sourceLocation)
                && Objects.equals(reference, other.reference)
                && Objects.equals(instances, other.instances)
                && Objects.equals(type, other.type);
        } else {
            return false;
        }
    }

    public ValueReference getReference() {
        return reference;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public Symbol getSymbol() {
        return reference.getSymbol();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, instances, type);
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public String toString() {
        return stringify(this) + "(" + reference.getMemberName() + " [" + instances.stream().map(Object::toString).collect(joining(", ")) + "])";
    }

    @Override
    public Method withType(Type type) {
        return new Method(sourceLocation, reference, instances, type);
    }
}
