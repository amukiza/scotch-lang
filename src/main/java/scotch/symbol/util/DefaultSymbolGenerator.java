package scotch.symbol.util;

import static scotch.symbol.Symbol.unqualified;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import scotch.symbol.Symbol;
import scotch.symbol.type.Types;
import scotch.symbol.type.VariableType;

public class DefaultSymbolGenerator implements SymbolGenerator {

    private final Map<List<String>, AtomicInteger> counters;
    private       int                              nextSymbol;
    private       int                              nextType;

    public DefaultSymbolGenerator() {
        counters = new HashMap<>();
    }

    @Override
    public Symbol reserveSymbol() {
        return unqualified(String.valueOf(nextSymbol++));
    }

    @Override
    public Symbol reserveSymbol(List<String> nestings) {
        List<String> memberNames = new ArrayList<>();
        memberNames.addAll(nestings);
        memberNames.add(String.valueOf(counters.computeIfAbsent(nestings, k -> new AtomicInteger()).getAndIncrement()));
        return unqualified(memberNames);
    }

    @Override
    public VariableType reserveType() {
        return Types.t(nextType++);
    }

    @Override
    public void startTypesAt(int counter) {
        nextType = counter;
    }
}
