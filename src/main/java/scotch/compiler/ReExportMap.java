package scotch.compiler;

import static scotch.symbol.Symbol.qualified;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import scotch.symbol.Symbol;
import scotch.symbol.Symbol.QualifiedSymbol;

public class ReExportMap {

    private final Map<String, Map<String, String>> reExports;

    public ReExportMap() {
        reExports = new LinkedHashMap<>();
    }

    public void addReExports(String moduleName, Map<String, String> reExports) {
        reExports.forEach((memberName, targetModule) -> addReExport(moduleName, memberName, targetModule));
    }

    public Optional<Symbol> qualify(QualifiedSymbol symbol) {
        if (reExports.containsKey(symbol.getModuleName()) && reExports.get(symbol.getModuleName()).containsKey(symbol.getMemberName())) {
            return Optional.of(qualified(reExports.get(symbol.getModuleName()).get(symbol.getMemberName()), symbol.getMemberName()));
        } else {
            return Optional.empty();
        }
    }

    private void addReExport(String moduleName, String memberName, String targetModule) {
        reExports.computeIfAbsent(moduleName, k -> new LinkedHashMap<>()).put(memberName, targetModule);
    }
}
