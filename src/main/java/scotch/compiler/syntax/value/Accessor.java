package scotch.compiler.syntax.value;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.syntax.TypeError.typeError;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;
import scotch.symbol.descriptor.DataConstructorDescriptor;
import scotch.symbol.descriptor.DataTypeDescriptor;
import scotch.symbol.type.SumType;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class Accessor extends Value {

    @Getter
    private final SourceLocation   sourceLocation;
    private final Value            target;
    private final String           field;
    @Getter
    private final Type             type;
    @Getter
    private final Optional<Symbol> tag;

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return new Accessor(sourceLocation, target.accumulateDependencies(state), field, type, tag);
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value bindMethods(TypeChecker typeChecker) {
        return new Accessor(sourceLocation, target.bindMethods(typeChecker), field, type, tag);
    }

    @Override
    public Value bindTypes(TypeChecker typeChecker) {
        return new Accessor(sourceLocation, target.bindTypes(typeChecker), field, typeChecker.generate(type), tag);
    }

    @Override
    public Value checkTypes(TypeChecker typeChecker) {
        Value checkedTarget = target.checkTypes(typeChecker);
        SumType targetType = (SumType) checkedTarget.getType();
        Type fieldType = getFieldType(typeChecker, checkedTarget, targetType);
        return new Accessor(sourceLocation, checkedTarget, field, fieldType, tag);
    }

    private Type getFieldType(TypeChecker typeChecker, Value checkedTarget, SumType targetType) {
        return target.getTag()
            .map(tag -> {
                // TODO absent tag makes field access fundamentally unsafe, may require a post-type check step to verify field access
                DataTypeDescriptor dataType = typeChecker.getDataType(checkedTarget.getType()).get();
                HashMap<Type, Type> mappedParameters = new HashMap<Type, Type>() {{
                    for (int i = 0; i < targetType.getParameters().size(); i++) {
                        put(dataType.getParameters().get(i), targetType.getParameters().get(i));
                    }
                }};
                DataConstructorDescriptor mappedConstructor = dataType.getConstructor(tag).get().mapParameters(mappedParameters);
                return mappedConstructor.getField(field).get().getType().unify(type, typeChecker).orElseGet(unification -> {
                    typeChecker.error(typeError(unification, sourceLocation));
                    return type;
                });
            })
            .orElse(type);
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public boolean equalsBeta(Value o) {
        if (equals(o)) {
            return true;
        } else if (o instanceof Accessor) {
            Accessor other = (Accessor) o;
            return target.equalsBeta(other.target)
                && Objects.equals(field, other.field);
        } else {
            return false;
        }
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        IntermediateValue intermediateTarget = target.generateIntermediateCode(state);
        return Intermediates.access(state.capture(), intermediateTarget, field);
    }

    public DataConstructorDescriptor mapConstructor(TypeChecker typeChecker, Value checkedTarget, SumType targetType) {
        DataTypeDescriptor dataType = typeChecker.getDataType(checkedTarget.getType()).get();
        HashMap<Type, Type> mappedParameters = new HashMap<Type, Type>() {{
            for (int i = 0; i < targetType.getParameters().size(); i++) {
                put(dataType.getParameters().get(i), targetType.getParameters().get(i));
            }
        }};
        return dataType.getConstructor(target.getTag().get()).get().mapParameters(mappedParameters);
    }

    @Override
    public Value mapTags(Function<Value, Value> mapper) {
        Accessor accessor = new Accessor(sourceLocation, target.mapTags(mapper), field, type, tag);
        return mapper.apply(accessor);
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value withTag(Symbol tag) {
        return new Accessor(sourceLocation, target, field, type, Optional.of(tag));
    }

    @Override
    public Value withType(Type type) {
        return new Accessor(sourceLocation, target, field, type, tag);
    }
}
