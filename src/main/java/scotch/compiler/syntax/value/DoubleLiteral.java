package scotch.compiler.syntax.value;

import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import me.qmx.jitescript.CodeBlock;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateValue;
import scotch.compiler.syntax.pattern.PatternReducer;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Types;

public class DoubleLiteral extends LiteralValue<Double> {

    DoubleLiteral(SourceLocation sourceLocation, double value) {
        super(sourceLocation, value, Types.sum("scotch.data.double.Double"));
    }

    @Override
    public IntermediateValue generateIntermediateCode(IntermediateGenerator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public Value reducePatterns(PatternReducer reducer) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    protected CodeBlock loadValue() {
        return new CodeBlock() {{
            ldc(getValue());
            invokestatic(p(Double.class), "valueOf", sig(Double.class, double.class));
        }};
    }
}
