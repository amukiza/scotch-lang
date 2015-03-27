package scotch.compiler.intermediate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateAssign extends IntermediateValue {

    private final String variable;
    private final IntermediateValue value;
    private final IntermediateValue body;
}
