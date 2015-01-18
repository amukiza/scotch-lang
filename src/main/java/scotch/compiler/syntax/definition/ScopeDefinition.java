package scotch.compiler.syntax.definition;

import static scotch.compiler.syntax.reference.DefinitionReference.scopeRef;
import static scotch.util.StringUtil.stringify;

import java.util.Objects;
import java.util.Optional;
import scotch.compiler.symbol.NameQualifier;
import scotch.compiler.symbol.Symbol;
import scotch.compiler.syntax.BytecodeGenerator;
import scotch.compiler.syntax.DependencyAccumulator;
import scotch.compiler.syntax.NameAccumulator;
import scotch.compiler.syntax.OperatorDefinitionParser;
import scotch.compiler.syntax.PrecedenceParser;
import scotch.compiler.syntax.TypeChecker;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.text.SourceRange;

public class ScopeDefinition extends Definition {

    private final SourceRange sourceRange;
    private final Symbol      symbol;

    ScopeDefinition(SourceRange sourceRange, Symbol symbol) {
        this.sourceRange = sourceRange;
        this.symbol = symbol;
    }

    @Override
    public Definition accumulateDependencies(DependencyAccumulator state) {
        return state.keep(this);
    }

    @Override
    public Definition accumulateNames(NameAccumulator state) {
        return state.keep(this);
    }

    @Override
    public Definition bindTypes(TypeChecker state) {
        return state.keep(this);
    }

    @Override
    public Definition checkTypes(TypeChecker state) {
        return state.keep(this);
    }

    @Override
    public Definition defineOperators(OperatorDefinitionParser state) {
        return state.keep(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ScopeDefinition) {
            ScopeDefinition other = (ScopeDefinition) o;
            return Objects.equals(sourceRange, other.sourceRange)
                && Objects.equals(symbol, other.symbol);
        } else {
            return false;
        }
    }

    @Override
    public void generateBytecode(BytecodeGenerator state) {
        // intentionally empty
    }

    @Override
    public DefinitionReference getReference() {
        return scopeRef(symbol);
    }

    @Override
    public SourceRange getSourceRange() {
        return sourceRange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public Optional<Definition> parsePrecedence(PrecedenceParser state) {
        return Optional.of(state.keep(this));
    }

    @Override
    public Definition qualifyNames(NameQualifier state) {
        return state.keep(this);
    }

    @Override
    public String toString() {
        return stringify(this) + "(" + symbol.getCanonicalName() + ")";
    }
}
