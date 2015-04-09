package scotch.compiler.intermediate;

import static java.util.stream.Collectors.toList;
import static me.qmx.jitescript.util.CodegenUtils.ci;
import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;
import static org.apache.commons.lang.StringUtils.capitalize;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static scotch.symbol.FieldSignature.fieldSignature;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.JiteClass;
import me.qmx.jitescript.LambdaBlock;
import org.objectweb.asm.tree.LabelNode;
import scotch.compiler.target.BytecodeGenerator;
import scotch.runtime.Callable;
import scotch.runtime.Copyable;
import scotch.runtime.RuntimeSupport;
import scotch.symbol.FieldSignature;
import scotch.symbol.Symbol;

@EqualsAndHashCode(callSuper = false, doNotUseGetters = true)
@ToString
public class IntermediateConstructorDefinition {

    private final Symbol                   symbol;
    private final Symbol                   dataType;
    private final Optional<FieldSignature> constantField;
    private final List<IntermediateField>  fields;

    IntermediateConstructorDefinition(Symbol symbol, Symbol dataType, List<IntermediateField> fields) {
        this.symbol = symbol;
        this.dataType = dataType;
        this.fields = ImmutableList.copyOf(fields);
        if (fields.isEmpty()) {
            String className = symbol.getClassNameAsChildOf(dataType);
            constantField = Optional.of(fieldSignature(className, ACC_STATIC | ACC_PUBLIC | ACC_FINAL, "INSTANCE", ci(Callable.class)));
        } else {
            constantField = Optional.empty();
        }
    }

    public void generateBytecode(BytecodeGenerator generator) {
        JiteClass parentClass = generator.currentClass();
        if (isNiladic()) {
            generator.beginConstant(symbol);
            generateInstanceField(generator);
            generateToString(generator);
            generator.endClass();
        } else {
            generator.beginConstructor(symbol);
            generateFields(generator);
            generateConstructor(generator, parentClass);
            generateEquals(generator);
            generateGetters(generator);
            generateHashCode(generator);
            generateToString(generator);
            generateCopyConstructor(generator);
            generator.endClass();
        }
    }

    public boolean isNiladic() {
        return fields.isEmpty();
    }

    private void generateConstructor(BytecodeGenerator generator, JiteClass parentClass) {
        Class<?>[] parameters = getParameters();
        generator.method("<init>", ACC_PUBLIC, sig(void.class, parameters), new CodeBlock() {{
            aload(0);
            invokespecial(parentClass.getClassName(), "<init>", sig(void.class));
            AtomicInteger counter = new AtomicInteger(1);
            fields.forEach(field -> {
                int offset = counter.get();
                aload(0);
                aload(offset);
                putfield(generator.currentClass().getClassName(), field.getJavaName(), ci(Callable.class));
                counter.getAndIncrement();
            });
            voidreturn();
        }});
    }

    private void generateCopyConstructor(BytecodeGenerator generator) {
        generator.method("copy", ACC_PUBLIC, sig(Copyable.class, Map.class), new CodeBlock() {{
            newobj(generator.currentClass().getClassName());
            dup();
            for (IntermediateField field : fields) {
                LabelNode fromField = new LabelNode();
                LabelNode endField = new LabelNode();
                aload(1);
                ldc(field.getJavaName());
                invokeinterface(p(Map.class), "containsKey", sig(boolean.class, Object.class));
                iffalse(fromField);
                aload(1);
                ldc(field.getJavaName());
                invokeinterface(p(Map.class), "get", sig(Object.class, Object.class));
                checkcast(p(Callable.class));
                go_to(endField);
                label(fromField);
                aload(0);
                getfield(generator.currentClass().getClassName(), field.getJavaName(), ci(Callable.class));
                label(endField);
            }
            invokespecial(generator.currentClass().getClassName(), "<init>", sig(void.class, getParameters()));
            areturn();
        }});
    }

    private void generateEquals(BytecodeGenerator generator) {
        generator.method("equals", ACC_PUBLIC, sig(boolean.class, Object.class), new CodeBlock() {{
            String className = generator.currentClass().getClassName();
            LabelNode equal = new LabelNode();
            LabelNode valueCompare = new LabelNode();
            LabelNode notEqual = new LabelNode();

            // o == this
            aload(0);
            aload(1);
            if_acmpne(valueCompare);
            go_to(equal);

            // o instanceof {class} && values equal
            label(valueCompare);
            aload(1);
            instance_of(className);
            ifeq(notEqual);
            if (!fields.isEmpty()) {
                aload(1);
                checkcast(className);
                astore(2);
                fields.forEach(field -> {
                    aload(0);
                    getfield(className, field.getJavaName(), ci(Callable.class));
                    invokeinterface(p(Callable.class), "call", sig(Object.class));
                    aload(2);
                    checkcast(className);
                    getfield(className, field.getJavaName(), ci(Callable.class));
                    invokeinterface(p(Callable.class), "call", sig(Object.class));
                    invokestatic(p(Objects.class), "equals", sig(boolean.class, Object.class, Object.class));
                    ifeq(notEqual);
                });
            }

            label(equal);
            iconst_1();
            ireturn();

            // not this && not {class}
            label(notEqual);
            iconst_0();
            ireturn();
        }});
    }

    private void generateFields(BytecodeGenerator generator) {
        fields.forEach(field -> field.generateBytecode(generator));
    }

    private void generateGetters(BytecodeGenerator generator) {
        Class<?>[] parameters = getParameters();
        AtomicInteger counter = new AtomicInteger(0);
        fields.forEach(field -> {
            Class<?> type = parameters[counter.getAndIncrement()];
            generator.method("get" + capitalize(field.getJavaName()), ACC_PUBLIC, sig(type), new CodeBlock() {{
                aload(0);
                getfield(generator.currentClass().getClassName(), field.getJavaName(), ci(type));
                areturn();
            }});
        });
    }

    private void generateHashCode(BytecodeGenerator generator) {
        generator.method("hashCode", ACC_PUBLIC, sig(int.class), new CodeBlock() {{
            if (fields.size() == 1) {
                aload(0);
                IntermediateField field = fields.iterator().next();
                getfield(generator.currentClass().getClassName(), field.getJavaName(), ci(Callable.class));
                invokeinterface(p(Callable.class), "call", sig(Object.class));
                invokestatic(p(Objects.class), "hashCode", sig(int.class, Object.class));
            } else {
                ldc(fields.size());
                anewarray(p(Object.class));
                AtomicInteger counter = new AtomicInteger();
                fields.forEach(field -> {
                    dup();
                    ldc(counter.getAndIncrement());
                    aload(0);
                    getfield(generator.currentClass().getClassName(), field.getJavaName(), ci(Callable.class));
                    invokeinterface(p(Callable.class), "call", sig(Object.class));
                    aastore();
                });
                invokestatic(p(Objects.class), "hash", sig(int.class, Object[].class));
            }
            ireturn();
        }});
    }

    private void generateInstanceField(BytecodeGenerator generator) {
        JiteClass jiteClass = generator.currentClass();
        String className = jiteClass.getClassName();
        getConstantField().defineOn(jiteClass);
        jiteClass.defineMethod("<clinit>", ACC_STATIC | ACC_SYNTHETIC | ACC_PRIVATE, sig(void.class), new CodeBlock() {{
            lambda(jiteClass, new LambdaBlock(generator.reserveLambda()) {{
                function(p(Supplier.class), "get", sig(Object.class));
                delegateTo(ACC_STATIC, sig(Object.class), new CodeBlock() {{
                    newobj(className);
                    dup();
                    invokespecial(className, "<init>", sig(void.class));
                    areturn();
                }});
            }});
            invokestatic(p(RuntimeSupport.class), "callable", sig(Callable.class, Supplier.class));
            append(getConstantField().putValue());
            voidreturn();
        }});
    }

    private void generateToString(BytecodeGenerator generator) {
        generator.method("toString", ACC_PUBLIC, sig(String.class), new CodeBlock() {{
            newobj(p(StringBuilder.class));
            dup();
            ldc(symbol.getMemberName());
            invokespecial(p(StringBuilder.class), "<init>", sig(void.class, String.class));
            int count = 0;
            if (!fields.isEmpty()) {
                ldc(" {");
                invokevirtual(p(StringBuilder.class), "append", sig(StringBuilder.class, String.class));
                for (IntermediateField field : fields) {
                    if (count != 0) {
                        ldc(",");
                        invokevirtual(p(StringBuilder.class), "append", sig(StringBuilder.class, String.class));
                    }
                    ldc(" " + field.getName() + " = ");
                    invokevirtual(p(StringBuilder.class), "append", sig(StringBuilder.class, String.class));
                    aload(0);
                    getfield(generator.currentClass().getClassName(), field.getJavaName(), ci(Callable.class));
                    invokeinterface(p(Callable.class), "call", sig(Object.class));
                    invokevirtual(p(Object.class), "toString", sig(String.class));
                    invokevirtual(p(StringBuilder.class), "append", sig(StringBuilder.class, String.class));
                    count++;
                }
                ldc(" }");
                invokevirtual(p(StringBuilder.class), "append", sig(StringBuilder.class, String.class));
            }
            invokevirtual(p(Object.class), "toString", sig(String.class));
            areturn();
        }});
    }

    private FieldSignature getConstantField() {
        return constantField.orElseThrow(IllegalStateException::new);
    }

    private Class<?>[] getParameters() {
        List<Class<?>> parameters = fields.stream()
            .map(field -> Callable.class)
            .collect(toList());
        return parameters.toArray(new Class<?>[parameters.size()]);
    }
}
