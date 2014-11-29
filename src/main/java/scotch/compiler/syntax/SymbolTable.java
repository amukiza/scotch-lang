package scotch.compiler.syntax;

import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import scotch.compiler.syntax.Definition.DefinitionVisitor;
import scotch.compiler.syntax.Definition.ValueDefinition;
import scotch.compiler.syntax.Definition.ValueSignature;
import scotch.compiler.syntax.DefinitionEntry.DefinitionEntryVisitor;
import scotch.compiler.syntax.DefinitionEntry.ScopedEntry;
import scotch.compiler.syntax.DefinitionEntry.UnscopedEntry;

public class SymbolTable {

    public static SymbolTableBuilder symbols(Collection<DefinitionEntry> entries) {
        return new SymbolTableBuilder(entries);
    }

    private final Map<DefinitionReference, DefinitionEntry> definitions;
    private final TypeGenerator                             typeGenerator;
    private final List<SyntaxError>                         errors;

    private SymbolTable(Collection<DefinitionEntry> entries, TypeGenerator typeGenerator, List<SyntaxError> errors) {
        this.typeGenerator = typeGenerator;
        this.errors = ImmutableList.copyOf(errors);
        ImmutableMap.Builder<DefinitionReference, DefinitionEntry> builder = ImmutableMap.builder();
        entries.forEach(entry -> builder.put(entry.getReference(), entry));
        this.definitions = builder.build();
    }

    public SymbolTableBuilder copyWith(Collection<DefinitionEntry> entries) {
        return symbols(entries)
            .withErrors(errors)
            .withSequence(typeGenerator);
    }

    public Definition getDefinition(DefinitionReference reference) {
        return definitions.get(reference).accept(new DefinitionEntryVisitor<Definition>() {
            @Override
            public Definition visit(ScopedEntry entry) {
                return entry.getDefinition();
            }

            @Override
            public Definition visit(UnscopedEntry entry) {
                return entry.getDefinition();
            }
        });
    }

    public List<SyntaxError> getErrors() {
        return errors;
    }

    public Scope getScope(DefinitionReference reference) {
        return definitions.get(reference).getScope();
    }

    public TypeGenerator getTypeGenerator() {
        return typeGenerator;
    }

    public Type getValue(DefinitionReference reference) {
        return getDefinition(reference).accept(new DefinitionVisitor<Type>() {
            @Override
            public Type visit(ValueDefinition definition) {
                return definition.getType();
            }

            @Override
            public Type visit(ValueSignature signature) {
                return signature.getType();
            }

            @Override
            public Type visitOtherwise(Definition definition) {
                throw new IllegalArgumentException("Can't get type of " + definition.getClass().getSimpleName());
            }
        });
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public static class SymbolTableBuilder {

        private final Collection<DefinitionEntry> definitions;
        private       Optional<TypeGenerator>     optionalSequence;
        private       Optional<List<SyntaxError>> optionalErrors;

        private SymbolTableBuilder(Collection<DefinitionEntry> definitions) {
            this.definitions = definitions;
            this.optionalSequence = Optional.empty();
            this.optionalErrors = Optional.empty();
        }

        public SymbolTable build() {
            return new SymbolTable(definitions, optionalSequence.orElseGet(TypeGenerator::new), optionalErrors.orElse(emptyList()));
        }

        public SymbolTableBuilder withErrors(List<SyntaxError> errors) {
            optionalErrors = Optional.of(errors);
            return this;
        }

        public SymbolTableBuilder withSequence(TypeGenerator typeGenerator) {
            optionalSequence = Optional.of(typeGenerator);
            return this;
        }
    }
}