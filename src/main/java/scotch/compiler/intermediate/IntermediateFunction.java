package scotch.compiler.intermediate;

import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import java.util.List;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.LambdaBlock;
import scotch.compiler.target.BytecodeGenerator;
import scotch.runtime.Applicable;
import scotch.runtime.Callable;

@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateFunction extends IntermediateValue {

    private final List<String>      captures;
    private final String            argument;
    private final IntermediateValue body;

    public IntermediateFunction(List<String> captures, String argument, IntermediateValue body) {
        this.captures = ImmutableList.copyOf(captures);
        this.argument = argument;
        this.body = body;
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator generator) {
        return new CodeBlock() {{
            captures.forEach(capture -> aload(generator.offsetOf(capture)));
            lambda(generator.currentClass(), new LambdaBlock(generator.reserveLambda()) {{
                function(p(Applicable.class), "apply", sig(Callable.class, Callable.class));
                capture(getCaptureTypes());
                delegateTo(ACC_STATIC, sig(Callable.class, getLambdaArgumentTypes()), new CodeBlock() {{
                    generator.beginMethod(captures, argument);
                    append(body.generateBytecode(generator));
                    areturn();
                    generator.endMethod();
                }});
            }});
        }};
    }

    private Class<?>[] getLambdaArgumentTypes() {
        return getCallables(captures.size() + 1);
    }

    private Class<?>[] getCallables(int size) {
        Class<?>[] callables = new Class<?>[size];
        for (int i = 0; i < size; i++) {
            callables[i] = Callable.class;
        }
        return callables;
    }

    private Class<?>[] getCaptureTypes() {
        return getCallables(captures.size());
    }
}
