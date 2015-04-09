package scotch.compiler.intermediate;

import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;
import static org.apache.commons.lang.WordUtils.capitalize;
import static scotch.symbol.Symbol.toJavaName;

import java.util.List;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.LambdaBlock;
import scotch.compiler.target.BytecodeGenerator;
import scotch.runtime.AccessorSupport;
import scotch.runtime.Callable;
import scotch.runtime.SuppliedThunk;

@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateAccessor extends IntermediateValue {

    private final List<String> captures;
    private final IntermediateValue target;
    private final String fieldName;

    IntermediateAccessor(List<String> captures, IntermediateValue target, String fieldName) {
        this.captures = ImmutableList.copyOf(captures);
        this.target = target;
        this.fieldName = fieldName;
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        List<Integer> argumentOffsets = generator.getArgumentOffsets();
        Class<?>[] argumentTypes = getArgumentTypes(argumentOffsets);
        return new CodeBlock() {{
            newobj(p(SuppliedThunk.class));
            dup();
            argumentOffsets.forEach(this::aload);
            lambda(generator.currentClass(), new LambdaBlock(generator.reserveAccess()) {{
                function(p(Supplier.class), "get", sig(Object.class));
                specialize(sig(Callable.class));
                capture(argumentTypes);
                delegateTo(ACC_STATIC, sig(Callable.class, argumentTypes), new CodeBlock() {{
                    List<String> arguments = generator.getArguments();
                    generator.beginMethod(arguments);
                    append(target.generateBytecode(generator));
                    ldc("get" + capitalize(toJavaName(fieldName)));
                    invokestatic(p(AccessorSupport.class), "access", sig(Callable.class, Callable.class, String.class));
                    areturn();
                    generator.endMethod();
                }});
            }});
            invokespecial(p(SuppliedThunk.class), "<init>", sig(void.class, Supplier.class));
        }};
    }

    private Class<?>[] getArgumentTypes(List<Integer> offsets) {
        Class<?>[] types = new Class<?>[offsets.size()];
        for (int i = 0; i < offsets.size(); i++) {
            types[i] = Callable.class;
        }
        return types;
    }
}
