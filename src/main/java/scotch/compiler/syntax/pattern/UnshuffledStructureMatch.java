package scotch.compiler.syntax.pattern;

import static scotch.compiler.syntax.builder.BuilderUtil.require;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import scotch.compiler.analyzer.DependencyAccumulator;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.Value;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class UnshuffledStructureMatch extends PatternMatch {

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    private final SourceLocation     sourceLocation;
    @Getter
    private final Type               type;
    private final List<PatternMatch> patternMatches;

    public UnshuffledStructureMatch(SourceLocation sourceLocation, Type type, List<PatternMatch> patternMatches) {
        this.sourceLocation = sourceLocation;
        this.type = type;
        this.patternMatches = ImmutableList.copyOf(patternMatches);
    }

    @Override
    public PatternMatch accumulateDependencies(DependencyAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public PatternMatch accumulateNames(NameAccumulator state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public PatternMatch bind(Value argument, Scope scope) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public PatternMatch bindMethods(TypeChecker state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public PatternMatch bindTypes(TypeChecker state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public PatternMatch checkTypes(TypeChecker state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public PatternMatch qualifyNames(ScopedNameQualifier state) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void reducePatterns(PatternReducer reducer) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public PatternMatch withType(Type type) {
        throw new UnsupportedOperationException(); // TODO
    }

    public static class Builder implements SyntaxBuilder<PatternMatch> {

        private final List<PatternMatch>       patternMatches = new ArrayList<>();
        private       Optional<SourceLocation> sourceLocation = Optional.empty();
        private       Optional<Type>           type           = Optional.empty();

        @Override
        public PatternMatch build() {
            if (patternMatches.size() == 1) {
                return patternMatches.get(0);
            } else {
                return new UnshuffledStructureMatch(
                    require(sourceLocation, "Source location"),
                    require(type, "Type"),
                    patternMatches
                );
            }
        }

        public Builder withPatternMatch(PatternMatch patternMatch) {
            patternMatches.add(patternMatch);
            return this;
        }

        @Override
        public Builder withSourceLocation(SourceLocation sourceLocation) {
            this.sourceLocation = Optional.of(sourceLocation);
            return this;
        }

        public Builder withType(Type type) {
            this.type = Optional.of(type);
            return this;
        }
    }
}
