package scotch.compiler.ast;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import scotch.lang.Symbol;
import scotch.lang.Symbol.QualifiedSymbol;
import scotch.lang.Symbol.SymbolVisitor;
import scotch.lang.Symbol.UnqualifiedSymbol;
import scotch.lang.Type;

public class Scope {

    public static Scope scope() {
        return new Scope(Optional.empty());
    }

    private final Optional<Scope>          optionalParent;
    private final Map<Symbol, SymbolEntry> symbols;

    private Scope(Optional<Scope> optionalParent) {
        this.optionalParent = optionalParent;
        this.symbols = new HashMap<>();
    }

    public void defineOperator(Symbol symbol, Operator operator) {
        getSymbol(symbol).defineOperator(operator);
    }

    public void defineValue(Symbol symbol, Type type) {
        getSymbol(symbol).defineValue(type);
    }

    public Scope enterScope() {
        return new Scope(Optional.of(this));
    }

    public Operator getOperator(Symbol symbol) {
        return getSymbol(symbol).getOperator();
    }

    public boolean isOperator(Symbol symbol) {
        return getSymbol(symbol).isOperator();
    }

    public Scope leaveScope() {
        return optionalParent.orElseThrow(() -> new IllegalStateException("Can't leave root scope"));
    }

    public Symbol qualify(UnqualifiedSymbol symbol) {
        return getSymbol(symbol).getSymbol();
    }

    private SymbolEntry getSymbol(Symbol symbol) {
        if (!symbols.containsKey(symbol)) {
            symbol.accept(new SymbolVisitor<Void>() {
                @Override
                public Void visit(QualifiedSymbol qualified) {
                    SymbolEntry entry = new SymbolEntry(qualified);
                    symbols.put(qualified, entry);
                    symbols.put(qualified.unqualify(), entry);
                    return null;
                }

                @Override
                public Void visit(UnqualifiedSymbol unqualified) {
                    symbols.put(unqualified, optionalParent
                            .map(parent -> parent.qualify(unqualified))
                            .map(qualified -> {
                                SymbolEntry entry = new SymbolEntry(qualified);
                                symbols.put(qualified, entry);
                                return entry;
                            })
                            .orElseGet(() -> new SymbolEntry(unqualified))
                    );
                    return null;
                }
            });
        }
        return symbols.get(symbol);
    }

    private final class SymbolEntry {

        private final Symbol             symbol;
        private       Optional<Operator> optionalOperator;
        private       Optional<Type>     optionalValue;

        private SymbolEntry(Symbol symbol) {
            this.symbol = symbol;
            this.optionalOperator = Optional.empty();
            this.optionalValue = Optional.empty();
        }

        public void defineOperator(Operator operator) {
            if (optionalOperator.isPresent()) {
                throw new RuntimeException("Operator already defined"); // TODO
            } else {
                optionalOperator = Optional.of(operator);
            }
        }

        public void defineValue(Type type) {
            if (optionalValue.isPresent()) {
                throw new RuntimeException("Value already defined"); // TODO
            } else {
                optionalValue = Optional.of(type);
            }
        }

        public Operator getOperator() {
            return optionalOperator.orElseGet(() -> optionalParent
                    .map(parent -> parent.getOperator(symbol))
                    .orElseThrow(() -> new RuntimeException("Operator not found"))
            );
        }

        public Symbol getSymbol() {
            return symbol;
        }

        public boolean isOperator() {
            return optionalOperator.isPresent() || optionalParent.map(parent -> parent.isOperator(symbol)).orElse(false);
        }
    }
}
