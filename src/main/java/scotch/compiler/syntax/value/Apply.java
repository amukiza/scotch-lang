package scotch.compiler.syntax.value;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.intermediate.Intermediates.apply;
import static scotch.compiler.syntax.TypeError.typeError;
import static scotch.symbol.type.Types.fn;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.FunctionType;
import scotch.symbol.type.Type;
import scotch.symbol.type.Unification;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(of = { "type", "function", "argument" })
public class Apply extends Value {

    private final SourceLocation sourceLocation;
    private final Value          function;
    private final Value          argument;
    private final Type           type;

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        return withFunction(function.accumulateDependencies(state)).withArgument(argument.accumulateDependencies(state));
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        return withFunction(function.accumulateNames(state))
            .withArgument(argument.accumulateNames(state));
    }

    @Override
    public Value bindMethods(TypeChecker typeChecker) {
        return withFunction(function.bindMethods(typeChecker))
            .withArgument(argument.bindMethods(typeChecker));
    }

    @Override
    public Value bindTypes(TypeChecker typeChecker) {
        return new Apply(sourceLocation, function.bindTypes(typeChecker), argument.bindTypes(typeChecker), typeChecker.generate(type));
    }

    @Override
    public Value checkTypes(TypeChecker typeChecker) {
        Value checkedFunction = function.checkTypes(typeChecker);
        Value checkedArgument = argument.checkTypes(typeChecker);
        Unification unify = fn(checkedArgument.getType(), type)
            .unify(checkedFunction.getType(), typeChecker.scope());
        return new Apply(sourceLocation, checkedFunction, checkedArgument, unify
            .mapType(t -> ((FunctionType) t).getResult())
            .orElseGet(unification -> {
                typeChecker.error(typeError(unification.flip(), checkedArgument.getSourceLocation()));
                return type;
            }));
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        IntermediateValue intermediateFunction = function.generateIntermediateCode(state);
        IntermediateValue intermediateArgument = argument.generateIntermediateCode(state);
        return apply(state.capture(), intermediateFunction, intermediateArgument);
    }

    public Value getArgument() {
        return argument;
    }

    public Value getFunction() {
        return function;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Value parsePrecedence(PrecedenceParser state) {
        return withFunction(function.parsePrecedence(state))
            .withArgument(argument.parsePrecedence(state));
    }

    @Override
    public Value qualifyNames(ScopedNameQualifier state) {
        return withFunction(function.qualifyNames(state)).withArgument(argument.qualifyNames(state));
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        return new Apply(sourceLocation, function.reducePatterns(reducer), argument.reducePatterns(reducer), type);
    }

    public Apply withArgument(Value argument) {
        return new Apply(sourceLocation, function, argument, type);
    }

    public Apply withFunction(Value function) {
        return new Apply(sourceLocation, function, argument, type);
    }

    public Apply withSourceLocation(SourceLocation sourceLocation) {
        return new Apply(sourceLocation, function, argument, type);
    }

    @Override
    public Apply withType(Type type) {
        return new Apply(sourceLocation, function, argument, type);
    }
}
