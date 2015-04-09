package scotch.compiler.syntax.value;

import me.qmx.jitescript.CodeBlock;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Types;

public class StringLiteral extends LiteralValue<String> {

    StringLiteral(SourceLocation sourceLocation, String value) {
        super(sourceLocation, value, Types.sum("scotch.data.string.String"));
    }

    @Override
    protected CodeBlock loadValue() {
        return new CodeBlock().ldc(getValue());
    }
}
