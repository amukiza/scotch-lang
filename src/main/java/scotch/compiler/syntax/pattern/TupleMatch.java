package scotch.compiler.syntax.pattern;

import static java.util.stream.Collectors.toList;
import static scotch.compiler.syntax.builder.BuilderUtil.require;
import static scotch.compiler.syntax.value.Values.isConstructor;
import static scotch.compiler.text.TextUtil.repeat;
import static scotch.symbol.type.Types.sum;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
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
import scotch.symbol.Symbol;
import scotch.symbol.type.Type;

@EqualsAndHashCode(callSuper = false)
@ToString(exclude = "sourceLocation")
public class TupleMatch extends PatternMatch {

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    private final SourceLocation   sourceLocation;
    private final Optional<Value>  argument;
    private final Symbol           constructor;
    @Getter
    private final Type             type;
    private final List<TupleField> fields;

    TupleMatch(SourceLocation sourceLocation, Optional<Value> argument, Symbol constructor, Type type, List<TupleField> fields) {
        this.sourceLocation = sourceLocation;
        this.argument = argument;
        this.constructor = constructor;
        this.type = type;
        this.fields = ImmutableList.copyOf(fields);
    }

    @Override
    public PatternMatch accumulateDependencies(DependencyAccumulator state) {
        return this;
    }

    @Override
    public PatternMatch accumulateNames(NameAccumulator state) {
        return map((field, ordinal) -> field.accumulateNames(state));
    }

    @Override
    public PatternMatch bind(Value argument, Scope scope) {
        return withArgument(argument).map((field, ordinal) -> field.bind(argument, ordinal, scope));
    }

    @Override
    public PatternMatch bindMethods(TypeChecker state) {
        return map((field, ordinal) -> field.bindMethods(state));
    }

    @Override
    public PatternMatch bindTypes(TypeChecker state) {
        return map((field, ordinal) -> field.bindTypes(state)).withType(state.generate(type)).withArgument(argument
            .orElseThrow(IllegalStateException::new)
            .bindTypes(state));
    }

    @Override
    public PatternMatch checkTypes(TypeChecker state) {
        return map((field, ordinal) -> field.checkTypes(state)).bindType(state);
    }

    @Override
    public PatternMatch qualifyNames(ScopedNameQualifier state) {
        return this;
    }

    @Override
    public void reducePatterns(PatternReducer reducer) {
        Value taggedArgument = argument.orElseThrow(IllegalStateException::new).withTag(constructor);
        reducer.addTaggedArgument(taggedArgument);
        reducer.addCondition(isConstructor(sourceLocation, reducer.getTaggedArgument(taggedArgument), constructor));
        fields.forEach(field -> field.reducePatterns(reducer));
    }

    @Override
    public TupleMatch withType(Type type) {
        return new TupleMatch(sourceLocation, argument, constructor, type, fields);
    }

    private TupleMatch bindType(TypeChecker state) {
        Type type = sum(
            "scotch.data.tuple.(" + repeat(",", fields.size() - 1) + ")",
            fields.stream()
                .map(TupleField::getType)
                .collect(toList()));
        argument.map(arg -> type.unify(arg.getType(), state));
        return withType(type);
    }

    private TupleMatch map(BiFunction<TupleField, Integer, TupleField> mapper) {
        AtomicInteger counter = new AtomicInteger();
        return new TupleMatch(
            sourceLocation, argument, constructor, type,
            fields.stream()
                .map(field -> mapper.apply(field, counter.getAndIncrement()))
                .collect(toList())
        );
    }

    private TupleMatch withArgument(Value argument) {
        return new TupleMatch(sourceLocation, Optional.of(argument), constructor, type, fields);
    }

    public static class Builder implements SyntaxBuilder<TupleMatch> {

        private Optional<SourceLocation> sourceLocation = Optional.empty();
        private List<TupleField>         fields         = new ArrayList<>();
        private Optional<Symbol>         constructor    = Optional.empty();
        private Optional<Type>           type           = Optional.empty();

        @Override
        public TupleMatch build() {
            return new TupleMatch(
                require(sourceLocation, "Source location"),
                Optional.empty(),
                require(constructor, "Constructor"),
                require(type, "Type"),
                fields
            );
        }

        public Builder withConstructor(Symbol constructor) {
            this.constructor = Optional.of(constructor);
            return this;
        }

        public Builder withField(TupleField field) {
            fields.add(field);
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
