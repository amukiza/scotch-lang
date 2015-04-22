package scotch.compiler.target;

import static java.util.stream.Collectors.toList;
import static me.qmx.jitescript.util.CodegenUtils.ci;
import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static scotch.compiler.output.GeneratedClass.ClassType.DATA_CONSTRUCTOR;
import static scotch.compiler.output.GeneratedClass.ClassType.DATA_TYPE;
import static scotch.compiler.output.GeneratedClass.ClassType.MODULE;
import static scotch.compiler.syntax.reference.DefinitionReference.rootRef;
import static scotch.compiler.util.Pair.pair;
import static scotch.symbol.Symbol.moduleClass;
import static scotch.symbol.Symbol.toJavaName;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.google.common.collect.ImmutableList;
import me.qmx.jitescript.CodeBlock;
import me.qmx.jitescript.JDKVersion;
import me.qmx.jitescript.JiteClass;
import scotch.compiler.intermediate.IntermediateGraph;
import scotch.compiler.output.GeneratedClass;
import scotch.compiler.output.GeneratedClass.ClassType;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.util.Pair;
import scotch.runtime.Callable;
import scotch.runtime.Copyable;
import scotch.symbol.Symbol;
import scotch.symbol.Symbol.QualifiedSymbol;

public class BytecodeGenerator {

    private final IntermediateGraph                 graph;
    private final Map<String, JiteClass>            moduleClasses;
    private final Deque<Pair<JiteClass, ClassType>> classes;
    private final List<Pair<JiteClass, ClassType>>  finishedClasses;
    private final Deque<List<String>>               argumentOffsets;
    private       int                               lambdas;
    private       int                               applies;
    private       int                               accesses;

    public BytecodeGenerator(IntermediateGraph graph) {
        this.graph = graph;
        this.moduleClasses = new HashMap<>();
        this.classes = new ArrayDeque<>();
        this.finishedClasses = new ArrayList<>();
        this.argumentOffsets = new ArrayDeque<>();
    }

    public void beginConstant(Symbol symbol) {
        JiteClass jiteClass = new JiteClass(
            currentClass().getClassName() + "$" + toJavaName(symbol.getMemberName()),
            currentClass().getClassName(),
            new String[0]
        );
        pushClass(jiteClass, DATA_CONSTRUCTOR);
        jiteClass.defineDefaultConstructor();
        jiteClass.defineMethod("call", ACC_PUBLIC, sig(Object.class), new CodeBlock() {{
            aload(0);
            areturn();
        }});
    }

    public void beginConstructor(Symbol symbol) {
        JiteClass jiteClass = new JiteClass(
            currentClass().getClassName() + "$" + toJavaName(symbol.getMemberName()),
            currentClass().getClassName(),
            new String[] { p(Copyable.class) });
        pushClass(jiteClass, DATA_CONSTRUCTOR);
    }

    public List<Integer> getArgumentOffsets() {
        AtomicInteger counter = new AtomicInteger();
        return argumentOffsets.peek().stream()
            .map(argument -> counter.getAndIncrement())
            .collect(toList());
    }

    public void defineField(String name) {
        currentClass().defineField(toJavaName(name), ACC_PRIVATE | ACC_FINAL, ci(Callable.class), null);
    }

    public List<String> getArguments() {
        return ImmutableList.copyOf(argumentOffsets.peek());
    }

    public void method(String name, int access, String signature, CodeBlock body) {
        currentClass().defineMethod(name, access, signature, body);
    }

    public void pushClass(JiteClass jiteClass, ClassType dataType) {
        classes.push(pair(jiteClass, dataType));
    }

    public void beginData(Symbol symbol) {
        JiteClass dataClass = new JiteClass(symbol.getClassName()) {{
            defineDefaultConstructor();
        }};
        pushClass(dataClass, DATA_TYPE);
    }

    public void beginMethod(List<String> captures) {
        argumentOffsets.push(ImmutableList.copyOf(captures));
    }

    public void beginMethod(List<String> captures, String argument) {
        argumentOffsets.push(new ArrayList<String>() {{
            addAll(ImmutableList.copyOf(captures));
            add(argument);
        }});
    }

    public void beginModule(String moduleName) {
        lambdas = 0;
        applies = 0;
        accesses = 0;
        JiteClass jiteClass = new JiteClass(moduleClass(moduleName)) {{
            defineDefaultConstructor(ACC_PRIVATE);
        }};
        moduleClasses.put(moduleName, jiteClass);
        pushClass(jiteClass, MODULE);
    }

    public void createValue(Symbol symbol, CodeBlock valueBody) {
        moduleClasses.get(((QualifiedSymbol) symbol).getModuleName())
            .defineMethod(symbol.getMethodName(), ACC_STATIC | ACC_PUBLIC, sig(Callable.class), valueBody);
    }

    public JiteClass currentClass() {
        return classes.peek().getLeft();
    }

    public void endClass() {
        finishedClasses.add(classes.pop());
    }

    public void endMethod() {
        argumentOffsets.pop();
    }

    public List<GeneratedClass> generateBytecode() {
        generateBytecode(rootRef());
        return finishedClasses.stream()
            .map(pair -> pair.into(
                (jiteClass, type) -> new GeneratedClass(type, jiteClass.getClassName().replace("/", "."), jiteClass.toBytes(JDKVersion.V1_8))))
            .sorted()
            .collect(toList());
    }

    public void generateBytecode(DefinitionReference reference) {
        graph.getDefinition(reference)
            .orElseThrow(IllegalStateException::new)
            .generateBytecode(this);
    }

    public int offsetOf(String argument) {
        return argumentOffsets.peek().indexOf(argument);
    }

    public String reserveAccess() {
        return "access$" + accesses++;
    }

    public String reserveApply() {
        return "apply$" + applies++;
    }

    public String reserveLambda() {
        return "lambda$" + lambdas++;
    }

    public void storeOffset(String variable) {
        argumentOffsets.peek().add(variable);
    }
}
