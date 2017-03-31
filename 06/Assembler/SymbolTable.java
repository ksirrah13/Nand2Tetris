import java.util.HashMap;
import java.util.Map;

class SymbolTable {

    private Map<String, Integer> table;
    private int nextAvailable = -1;

    SymbolTable() {
        table = new HashMap<>();
        nextAvailable = 16;
        initialize();
    }

    private void initialize() {
        table.put("SP", 0);
        table.put("LCL", 1);
        table.put("ARG", 2);
        table.put("THIS", 3);
        table.put("THAT", 4);

        for (int i = 0; i <= 15; i++) {
            table.put("R" + i, i);
        }

        table.put("SCREEN", 16384);
        table.put("KBD", 24576);
    }

    void addEntry(String symbol) throws IllegalArgumentException {
        addEntry(symbol, nextAvailable++);
    }

    void addEntry(String symbol, int address) throws IllegalArgumentException {
        if (contains(symbol)) {
            throw new IllegalArgumentException(symbol + " is already assigned to address " + address);
        }

        table.put(symbol, address);
    }

    boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    int getAddress(String symbol) {
        return table.get(symbol);
    }

    void clear() {
        table.clear();
    }
}
