package scotch.compiler.intermediate;

import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import java.util.List;
import java.util.function.Supplier;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.LambdaBlock;
import scotch.compiler.target.BytecodeGenerator;
import scotch.runtime.Applicable;
import scotch.runtime.Callable;
import scotch.runtime.SuppliedThunk;

@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateApply extends IntermediateValue {

    private final List<String>      captures;
    private final IntermediateValue function;
    private final IntermediateValue argument;

    public IntermediateApply(List<String> captures, IntermediateValue function, IntermediateValue argument) {
        this.captures = ImmutableList.copyOf(captures);
        this.function = function;
        this.argument = argument;
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return new CodeBlock() {{
            newobj(p(SuppliedThunk.class));
            dup();
            captures.forEach(capture -> aload(generator.offsetOf(capture)));
            lambda(generator.currentClass(), new LambdaBlock(generator.reserveApply()) {{
                function(p(Supplier.class), "get", sig(Object.class));
                specialize(sig(Callable.class));
                capture(getCaptureTypes());
                delegateTo(ACC_STATIC, sig(Callable.class, getCaptureTypes()), new CodeBlock() {{
                    generator.beginMethod(captures);
                    append(function.generateBytecode(generator));
                    invokeinterface(p(Callable.class), "call", sig(Object.class));
                    checkcast(p(Applicable.class));
                    append(argument.generateBytecode(generator));
                    invokeinterface(p(Applicable.class), "apply", sig(Callable.class, Callable.class));
                    areturn();
                    generator.endMethod();
                }});
            }});
            invokespecial(p(SuppliedThunk.class), "<init>", sig(void.class, Supplier.class));
        }};
    }

    private Class<?>[] getCaptureTypes() {
        int size = captures.size();
        Class<?>[] callables = new Class<?>[size];
        for (int i = 0; i < size; i++) {
            callables[i] = Callable.class;
        }
        return callables;
    }
}
