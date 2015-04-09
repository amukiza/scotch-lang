package scotch.compiler.syntax.definition;

import static scotch.compiler.util.Either.left;

import java.util.Optional;
import me.qmx.jitescript.CodeBlock;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PatternAnalyzer;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.syntax.Scoped;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.text.SourceLocation;
import scotch.compiler.util.Either;
import scotch.symbol.Symbol;

public abstract class Definition implements Scoped {

    protected Definition() {
        // intentionally empty
    }

    public abstract Definition accumulateDependencies(DependencyAccumulator state);

    public abstract Definition accumulateNames(NameAccumulator state);

    public Either<Definition, ValueSignature> asSignature() {
        return left(this);
    }

    public Optional<Symbol> asSymbol() {
        return Optional.empty();
    }

    public Either<Definition, ValueDefinition> asValue() {
        return left(this);
    }

    public abstract Definition checkTypes(TypeChecker state);

    public abstract Definition defineOperators(OperatorAccumulator state);

    @Override
    public abstract boolean equals(Object o);

    public abstract Optional<DefinitionReference> generateIntermediateCode(IntermediateGenerator generator);

    @Override
    public Definition getDefinition() {
        return this;
    }

    public abstract SourceLocation getSourceLocation();

    @Override
    public abstract int hashCode();

    public void markLine(CodeBlock codeBlock) {
        getSourceLocation().markLine(codeBlock);
    }

    public abstract Optional<Definition> parsePrecedence(PrecedenceParser state);

    public abstract Definition qualifyNames(ScopedNameQualifier state);

    public abstract Definition reducePatterns(PatternAnalyzer state);

    @Override
    public abstract String toString();
}
