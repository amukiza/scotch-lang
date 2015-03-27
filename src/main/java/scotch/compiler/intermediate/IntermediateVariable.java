package scotch.compiler.intermediate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class IntermediateVariable extends IntermediateValue {

    private final String name;
}
