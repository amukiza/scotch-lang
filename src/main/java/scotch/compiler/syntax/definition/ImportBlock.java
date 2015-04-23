package scotch.compiler.syntax.definition;

import static java.util.stream.Collectors.toList;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.reference.DefinitionReference.scopeRef;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.OperatorAccumulator;
import scotch.compiler.analyzer.PatternAnalyzer;
import scotch.compiler.analyzer.PrecedenceParser;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.intermediate.IntermediateGenerator;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.reference.DefinitionReference;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class ImportBlock extends Definition {

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    private final SourceLocation            sourceLocation;
    private final Symbol                    symbol;
    private final List<Import>              imports;
    private final List<DefinitionReference> definitions;

    ImportBlock(SourceLocation sourceLocation, Symbol symbol, List<Import> imports, List<DefinitionReference> definitions) {
        this.sourceLocation = sourceLocation;
        this.symbol = symbol;
        this.imports = ImmutableList.copyOf(imports);
        this.definitions = ImmutableList.copyOf(definitions);
    }

    @Override
    public Definition accumulateDependencies(DependencyAccumulator state) {
        return state.scoped(this, () -> withDefinitions(state.accumulateDependencies(definitions)));
    }

    @Override
    public Definition accumulateNames(NameAccumulator state) {
        return state.scoped(this, () -> withDefinitions(state.accumulateNames(definitions)));
    }

    @Override
    public Definition checkTypes(TypeChecker state) {
        return state.keep(this);
    }

    @Override
    public Definition defineOperators(OperatorAccumulator state) {
        return state.scoped(this, () -> withDefinitions(state.defineDefinitionOperators(definitions)));
    }

    @Override
    public Optional<DefinitionReference> generateIntermediateCode(IntermediateGenerator generator) {
        generator.scoped(this, () -> generator.defineMembers(definitions.stream()
            .map(generator::generateIntermediateCode)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList())));
        return Optional.empty();
    }

    @Override
    public DefinitionReference getReference() {
        return scopeRef(symbol);
    }

    @Override
    public Optional<Definition> parsePrecedence(PrecedenceParser state) {
        return Optional.of(state.scoped(this, () -> withDefinitions(new ArrayList<DefinitionReference>() {{
            addAll(state.mapOptional(definitions, Definition::parsePrecedence));
            addAll(state.processPatterns());
        }})));
    }

    @Override
    public Definition qualifyNames(ScopedNameQualifier state) {
        return state.scoped(this, () -> withDefinitions(state.qualifyDefinitionNames(definitions)));
    }

    @Override
    public Definition reducePatterns(PatternAnalyzer state) {
        return state.scoped(this, () -> withDefinitions(state.reducePatterns(definitions)));
    }

    public ImportBlock withDefinitions(List<DefinitionReference> definitions) {
        return new ImportBlock(sourceLocation, symbol, imports, definitions);
    }

    public static class Builder implements SyntaxBuilder<ImportBlock> {

        private Optional<SourceLocation>            sourceLocation = Optional.empty();
        private Optional<Symbol>                    symbol         = Optional.empty();
        private Optional<List<Import>>              imports        = Optional.empty();
        private Optional<List<DefinitionReference>> definitions    = Optional.empty();

        @Override
        public ImportBlock build() {
            return new ImportBlock(
                require(sourceLocation, "Source location"),
                require(symbol, "Import scope symbol"),
                require(imports, "Imports"),
                require(definitions, "Definition references")
            );
        }

        public Builder withDefinitions(List<DefinitionReference> definitions) {
            this.definitions = Optional.of(definitions);
            return this;
        }

        public Builder withImports(List<Import> imports) {
            this.imports = Optional.of(imports);
            return this;
        }

        @Override
        public Builder withSourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = Optional.of(sourceLocation);
            return this;
        }

        public Builder withSymbol(Symbol symbol) {
            this.symbol = Optional.of(symbol);
            return this;
        }
    }
}
