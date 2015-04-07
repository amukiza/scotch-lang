package scotch.symbol.util;

import java.util.List;
import scotch.symbol.Symbol;
import scotch.symbol.type.VariableType;

public interface SymbolGenerator {

    Symbol reserveSymbol();

    Symbol reserveSymbol(List<String> nestings);

    VariableType reserveType();

    void startTypesAt(int counter);
}
