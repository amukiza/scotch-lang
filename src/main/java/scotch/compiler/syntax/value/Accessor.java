package scotch.compiler.syntax.value;

import static lombok.AccessLevel.PACKAGE;
import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;
import static org.apache.commons.lang.WordUtils.capitalize;
import static scotch.symbol.Symbol.toJavaName;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.steps.BytecodeGenerator;
import scotch.compiler.steps.DependencyAccumulator;
import scotch.compiler.steps.NameAccumulator;
import scotch.compiler.steps.OperatorAccumulator;
import scotch.compiler.steps.PrecedenceParser;
import scotch.compiler.steps.ScopedNameQualifier;
import scotch.compiler.steps.TypeChecker;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.runtime.AccessorSupport;
import scotch.runtime.Callable;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class Accessor extends Value {

    @Getter
    private final SourceLocation sourceLocation;
    private final Value          target;
    private final String         field;
    @Getter
    private final Type           type;

    @Override
    public Value accumulateDependencies(DependencyAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value accumulateNames(NameAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value bindMethods(TypeChecker state) {
        return new Accessor(sourceLocation, target.bindMethods(state), field, type);
    }

    @Override
    public Value bindTypes(TypeChecker state) {
        return new Accessor(sourceLocation, target.bindTypes(state), field, state.generate(type));
    }

    @Override
    public Value checkTypes(TypeChecker state) {
        return new Accessor(sourceLocation, target.checkTypes(state), field, type);
    }

    @Override
    public Value defineOperators(OperatorAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public CodeBlock generateBytecode(BytecodeGenerator state) {
        return new CodeBlock() {{
            append(target.generateBytecode(state));
            checkcast(p(Callable.class));
            ldc("get" + capitalize(toJavaName(field)));
            invokestatic(p(AccessorSupport.class), "access", sig(Callable.class, Callable.class, String.class));
        }};
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        throw new UnsupportedOperationException(); // TODO
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
    public Value withType(Type type) {
        return new Accessor(sourceLocation, target, field, type);
    }
}
