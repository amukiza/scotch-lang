package scotch.compiler.syntax.definition;

import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.definition.Definitions.module;
import static scotch.compiler.syntax.reference.DefinitionReference.moduleRef;
import static scotch.util.StringUtil.stringify;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
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

@EqualsAndHashCode(callSuper = false)
public class ModuleDefinition extends Definition {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation            sourceLocation;
    private final String                    symbol;
    private final List<DefinitionReference> importScopes;

    ModuleDefinition(SourceLocation sourceLocation, String symbol, List<DefinitionReference> importScopes) {
        this.sourceLocation = sourceLocation;
        this.symbol = symbol;
        this.importScopes = ImmutableList.copyOf(importScopes);
    }

    @Override
    public Definition accumulateDependencies(DependencyAccumulator state) {
        return state.scoped(this, () -> withImportScopes(state.accumulateDependencies(importScopes)));
    }

    @Override
    public Definition accumulateNames(NameAccumulator state) {
        return state.scoped(this, () -> withImportScopes(state.accumulateNames(importScopes)));
    }

    @Override
    public Definition checkTypes(TypeChecker state) {
        return state.keep(this);
    }

    @Override
    public Definition defineOperators(OperatorAccumulator state) {
        return state.scoped(this, () -> withImportScopes(state.defineDefinitionOperators(importScopes)));
    }

    @Override
    public Optional<DefinitionReference> generateIntermediateCode(IntermediateGenerator generator) {
        return generator.scoped(this,
            () -> generator.defineModule(symbol,
                () -> importScopes.forEach(generator::generateIntermediateCode)));
    }

    @Override
    public DefinitionReference getReference() {
        return moduleRef(symbol);
    }

    @Override
    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    @Override
    public Optional<Definition> parsePrecedence(PrecedenceParser state) {
        return Optional.of(state.scoped(this, () -> withImportScopes(new ArrayList<DefinitionReference>() {{
            addAll(state.mapOptional(importScopes, Definition::parsePrecedence));
            addAll(state.processPatterns());
        }})));
    }

    @Override
    public Definition qualifyNames(ScopedNameQualifier state) {
        return state.scoped(this, () -> withImportScopes(state.qualifyDefinitionNames(importScopes)));
    }

    @Override
    public Definition reducePatterns(PatternAnalyzer state) {
        return state.scoped(this, () -> withImportScopes(state.reducePatterns(importScopes)));
    }

    @Override
    public String toString() {
        return stringify(this) + "(" + symbol + ")";
    }

    public ModuleDefinition withImportScopes(List<DefinitionReference> importScopes) {
        return new ModuleDefinition(sourceLocation, symbol, importScopes);
    }

    public static class Builder implements SyntaxBuilder<ModuleDefinition> {

        private final List<DefinitionReference> importScopes   = new ArrayList<>();
        private       Optional<String>          symbol         = Optional.empty();
        private       Optional<SourceLocation>  sourceLocation = Optional.empty();

        private Builder() {
            // intentionally empty
        }

        @Override
        public ModuleDefinition build() {
            return module(
                require(sourceLocation, "Source location"),
                require(symbol, "Module symbol"),
                importScopes
            );
        }

        public Builder withImportScope(DefinitionReference importScope) {
            importScopes.add(importScope);
            return this;
        }

        @Override
        public Builder withSourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = Optional.of(sourceLocation);
            return this;
        }

        public Builder withSymbol(String symbol) {
            this.symbol = Optional.of(symbol);
            return this;
        }
    }
}
