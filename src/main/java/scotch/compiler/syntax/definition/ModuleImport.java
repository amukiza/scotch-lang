package scotch.compiler.syntax.definition;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.symbol.Symbol.qualified;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.Symbol;
import scotch.symbol.SymbolEntry;
import scotch.symbol.SymbolResolver;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public final class ModuleImport extends Import {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation sourceLocation;
    private final String         moduleName;

    @Override
    public Set<Symbol> getContext(Type type, SymbolResolver resolver) {
        return getContext_(moduleName, type, resolver);
    }

    @Override
    public boolean isFrom(String moduleName) {
        return Objects.equals(this.moduleName, moduleName);
    }

    @Override
    public Optional<Symbol> qualify(String name, SymbolResolver resolver) {
        return resolver.getEntry(qualified(moduleName, name)).map(SymbolEntry::getSymbol);
    }

    @Override
    public ModuleImport withSourceLocation(SourceLocation sourceLocation) {
        return new ModuleImport(sourceLocation, moduleName);
    }

    public static class Builder implements SyntaxBuilder<ModuleImport> {

        private Optional<SourceLocation> sourceLocation = Optional.empty();
        private Optional<String>         moduleName     = Optional.empty();

        private Builder() {
            // intentionally empty
        }

        @Override
        public ModuleImport build() {
            return moduleImport(
                require(sourceLocation, "Source location"),
                require(moduleName, "Module name")
            );
        }

        public Builder withModuleName(String moduleName) {
            this.moduleName = Optional.of(moduleName);
            return this;
        }

        @Override
        public Builder withSourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = Optional.of(sourceLocation);
            return this;
        }
    }
}
