package scotch.compiler.syntax.pattern;

import static lombok.AccessLevel.PACKAGE;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.value.Values.access;
import static scotch.symbol.Symbol.symbol;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import scotch.compiler.analyzer.NameAccumulator;
import scotch.compiler.analyzer.ScopedNameQualifier;
import scotch.compiler.analyzer.TypeChecker;
import scotch.compiler.syntax.builder.SyntaxBuilder;
import scotch.compiler.syntax.scope.Scope;
import scotch.compiler.syntax.value.Value;
import scotch.compiler.text.SourceLocation;
import scotch.symbol.type.Type;

@AllArgsConstructor(access = PACKAGE)
@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class StructField {

    public static Builder builder() {
        return new Builder();
    }

    private final SourceLocation sourceLocation;
    private final String         field;
    private final Type           type;
    private final PatternMatch   patternMatch;

    public StructField accumulateNames(NameAccumulator state) {
        return withPatternMatch(patternMatch.accumulateNames(state));
    }

    public StructField bind(Value argument, Scope scope) {
        return new StructField(
            sourceLocation, field, type,
            patternMatch.bind(access(sourceLocation, argument, field, scope.reserveType(), Optional.empty()), scope)
        );
    }

    public StructField bindMethods(TypeChecker state) {
        return new StructField(sourceLocation, field, type, patternMatch.bindMethods(state));
    }

    public StructField bindTypes(TypeChecker state) {
        return new StructField(sourceLocation, field, state.generate(type), patternMatch.bindTypes(state));
    }

    public StructField checkTypes(TypeChecker state) {
        PatternMatch checkedMatch = patternMatch.checkTypes(state);
        return new StructField(sourceLocation, field, checkedMatch.getType(), checkedMatch);
    }

    public Type getType() {
        return type;
    }

    public StructField qualifyNames(ScopedNameQualifier state) {
        return withPatternMatch(patternMatch.qualifyNames(state));
    }

    public void reducePatterns(PatternReducer reducer) {
        patternMatch.reducePatterns(reducer);
    }

    private StructField withPatternMatch(PatternMatch patternMatch) {
        return new StructField(sourceLocation, field, type, patternMatch);
    }

    public static class Builder implements SyntaxBuilder<StructField> {

        private Optional<SourceLocation> sourceLocation = Optional.empty();
        private Optional<String>         fieldName      = Optional.empty();
        private Optional<Type>           type           = Optional.empty();
        private Optional<PatternMatch>   patternMatch   = Optional.empty();
        private boolean                  implicit       = false;

        private Builder() {
            // intentionally empty
        }

        @Override
        public StructField build() {
            PatternMatch match;
            if (implicit) {
                match = CaptureMatch.builder()
                    .withSourceLocation(require(sourceLocation, "Source location"))
                    .withSymbol(symbol(require(fieldName, "Field name")))
                    .withType(require(type, "Field type"))
                    .build();
            } else {
                match = require(patternMatch, "Field pattern");
            }
            return new StructField(
                require(sourceLocation, "Source location"),
                require(fieldName, "Field name"),
                require(type, "Field type"),
                match
            );
        }

        public Builder withFieldName(String fieldName) {
            this.fieldName = Optional.of(fieldName);
            return this;
        }

        public Builder withImplicitCapture() {
            implicit = true;
            return this;
        }

        public Builder withPatternMatch(PatternMatch patternMatch) {
            this.patternMatch = Optional.of(patternMatch);
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
