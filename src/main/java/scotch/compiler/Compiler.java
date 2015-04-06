package scotch.compiler;

import java.net.URI;
import java.util.List;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.intermediate.IntermediateGraph;
import scotch.compiler.output.GeneratedClass;
import scotch.compiler.parser.InputParser;
import scotch.compiler.scanner.Scanner;
import scotch.compiler.steps.BytecodeGenerator;
import scotch.compiler.steps.DependencyAccumulator;
import scotch.compiler.steps.NameAccumulator;
import scotch.compiler.steps.OperatorAccumulator;
import scotch.compiler.steps.PatternReducerStep;
import scotch.compiler.steps.PrecedenceParser;
import scotch.compiler.steps.ScopedNameQualifier;
import scotch.compiler.steps.TypeChecker;
import scotch.compiler.syntax.definition.DefinitionGraph;
import scotch.symbol.SymbolResolver;

// TODO multiple file compilation
// TODO incremental compilation
public class Compiler {

    public static Compiler compiler(SymbolResolver symbolResolver, URI source, String... lines) {
        return new Compiler(symbolResolver, Scanner.forString(source, lines));
    }

    private final SymbolResolver symbolResolver;
    private final Scanner        scanner;

    private Compiler(SymbolResolver symbolResolver, Scanner scanner) {
        this.symbolResolver = symbolResolver;
        this.scanner = scanner;
    }

    public DefinitionGraph accumulateDependencies() {
        return new DependencyAccumulator(qualifyNames()).accumulateDependencies();
    }

    public DefinitionGraph accumulateNames() {
        return new NameAccumulator(parsePrecedence()).accumulateNames();
    }

    public DefinitionGraph accumulateOperators() {
        return new OperatorAccumulator(parseInput()).accumulateOperators();
    }

    public DefinitionGraph checkTypes() {
        return new TypeChecker(accumulateDependencies()).checkTypes();
    }

    public List<GeneratedClass> generateBytecode() {
        return new BytecodeGenerator(checkTypes()).generateBytecode();
    }

    public IntermediateGraph generateIntermediateCode() {
        return new IntermediateGenerator(checkTypes()).generateIntermediateCode();
    }

    public DefinitionGraph parseInput() {
        return new InputParser(symbolResolver, scanner).parse();
    }

    public DefinitionGraph parsePrecedence() {
        return new PrecedenceParser(accumulateOperators()).parsePrecedence();
    }

    public DefinitionGraph qualifyNames() {
        return new ScopedNameQualifier(accumulateNames()).qualifyNames();
    }

    public DefinitionGraph reducePatterns() {
        return new PatternReducerStep(qualifyNames()).reducePatterns();
    }
}
