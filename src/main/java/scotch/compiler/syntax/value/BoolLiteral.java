package scotch.compiler.syntax.value;

import static me.qmx.jitescript.util.CodegenUtils.ci;
import static me.qmx.jitescript.util.CodegenUtils.p;

import me.qmx.jitescript.CodeBlock;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Types;

public class BoolLiteral extends LiteralValue<Boolean> {

    BoolLiteral(SourceLocation sourceLocation, boolean value) {
        super(sourceLocation, value, Types.sum("scotch.data.bool.Bool"));
    }

    @Override
    protected CodeBlock loadValue() {
        return new CodeBlock().getstatic(p(Boolean.class), getValue() ? "TRUE" : "FALSE", ci(Boolean.class));
    }
}
