package scotch.compiler.intermediate;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static scotch.compiler.syntax.reference.DefinitionReference.classRef;
import static scotch.compiler.syntax.reference.DefinitionReference.moduleRef;
import static scotch.symbol.Symbol.symbol;
import static scotch.symbol.descriptor.TypeParameterDescriptor.typeParam;
import static scotch.symbol.type.Types.sum;

import java.util.List;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.syntax.reference.InstanceReference;
import scotch.compiler.syntax.reference.ValueReference;
import scotch.symbol.FieldSignature;
import scotch.symbol.MethodSignature;
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

public final class Intermediates {

    public static IntermediateAccessor access(List<String> captures, IntermediateValue target, String fieldName, String methodName) {
        return new IntermediateAccessor(captures, target, fieldName, methodName);
    }

    public static IntermediateApply apply(List<String> captures, IntermediateValue function, IntermediateValue argument) {
        return new IntermediateApply(captures, function, argument);
    }

    public static IntermediateAssign assign(String variable, IntermediateValue value, IntermediateValue body) {
        return new IntermediateAssign(variable, value, body);
    }

    public static IntermediateConditional conditional(IntermediateValue condition, IntermediateValue truePath, IntermediateValue falsePath) {
        return new IntermediateConditional(condition, truePath, falsePath);
    }

    public static IntermediateConstantReference constantReference(String name, String dataType, FieldSignature constantField) {
        return constantReference(symbol(name), symbol(dataType), constantField);
    }

    public static IntermediateConstantReference constantReference(Symbol symbol, Symbol dataType, FieldSignature constantField) {
        return new IntermediateConstantReference(symbol, dataType, constantField);
    }

    public static IntermediateConstructorDefinition constructor(String symbol, String dataType) {
        return constructor(symbol(symbol), symbol(dataType), emptyList());
    }

    public static IntermediateConstructorDefinition constructor(String symbol, List<IntermediateField> fields) {
        return new IntermediateConstructorDefinition(symbol(symbol), symbol(symbol), fields);
    }

    public static IntermediateConstructorDefinition constructor(String symbol, String dataType, List<IntermediateField> fields) {
        return constructor(symbol(symbol), symbol(dataType), fields);
    }

    public static IntermediateConstructorDefinition constructor(Symbol symbol, Symbol dataType, List<IntermediateField> fields) {
        return new IntermediateConstructorDefinition(symbol, dataType, fields);
    }

    public static IntermediateConstructor constructor(String symbol, String className, MethodSignature methodSignature, List<IntermediateValue> arguments) {
        return constructor(symbol(symbol), className, methodSignature, arguments);
    }

    public static IntermediateConstructor constructor(Symbol symbol, String className, MethodSignature methodSignature, List<IntermediateValue> arguments) {
        return new IntermediateConstructor(symbol, className, methodSignature, arguments);
    }

    public static IntermediateDataDefinition data(String name, List<Type> parameters, List<IntermediateConstructorDefinition> constructors) {
        return data(symbol(name), parameters, constructors);
    }

    public static IntermediateDataDefinition data(Symbol symbol, List<Type> parameters, List<IntermediateConstructorDefinition> constructors) {
        return new IntermediateDataDefinition(symbol, parameters, constructors);
    }

    public static IntermediateField field(String name, Type type) {
        return new IntermediateField(name, type);
    }

    public static IntermediateFunction function(List<String> captures, String argument, IntermediateValue body) {
        return new IntermediateFunction(captures, argument, body);
    }

    public static IntermediateValue instanceOf(IntermediateValue intermediateValue, String className) {
        return new IntermediateInstanceOf(intermediateValue, className);
    }

    public static IntermediateReference instanceRef(String className, String moduleName, String dataType, MethodSignature methodSignature) {
        return instanceRef(className, moduleName, sum(dataType), methodSignature);
    }

    public static IntermediateReference instanceRef(String className, String moduleName, Type dataType, MethodSignature methodSignature) {
        return instanceRef(
            DefinitionReference.instanceRef(
                classRef(symbol(className)),
                moduleRef(moduleName),
                asList(typeParam(dataType))
            ),
            methodSignature
        );
    }

    public static IntermediateReference instanceRef(InstanceReference instanceReference, MethodSignature methodSignature) {
        return new IntermediateReference(instanceReference, methodSignature);
    }

    public static IntermediateLiteral literal(Object value) {
        return new IntermediateLiteral(value);
    }

    public static IntermediateModule module(String symbol, List<DefinitionReference> definitions) {
        return new IntermediateModule(symbol, definitions);
    }

    public static IntermediateRaise raise(String message) {
        return new IntermediateRaise(message);
    }

    public static IntermediateDefinition root(List<DefinitionReference> references) {
        return new IntermediateRoot(references);
    }

    public static IntermediateDefinition value(String name, Type type, IntermediateValue value) {
        return value(symbol(name), type, value);
    }

    public static IntermediateDefinition value(Symbol symbol, Type type, IntermediateValue value) {
        return new IntermediateValueDefinition(symbol, type, value);
    }

    public static IntermediateReference valueRef(String name, MethodSignature methodSignature) {
        return valueRef(DefinitionReference.valueRef(symbol(name)), methodSignature);
    }

    public static IntermediateReference valueRef(ValueReference valueReference, MethodSignature methodSignature) {
        return new IntermediateReference(valueReference, methodSignature);
    }

    public static IntermediateVariable variable(String name) {
        return new IntermediateVariable(name);
    }

    private Intermediates() {
        // intentionally empty
    }
}
