package scotch.compiler.intermediate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateConditional extends IntermediateValue {

    private final IntermediateValue condition;
    private final IntermediateValue whenTrue;
    private final IntermediateValue whenFalse;
}
