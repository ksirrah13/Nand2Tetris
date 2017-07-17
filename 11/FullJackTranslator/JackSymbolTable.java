import java.util.Arrays;
import java.util.HashMap;

class JackSymbolTable {

    private HashMap<String, SymbolEntry> classScope;
    private HashMap<String, SymbolEntry> subroutineScope;
    private HashMap<String, SymbolEntry> currentScope;

    JackSymbolTable() {
        this.classScope = new HashMap<>();
        this.subroutineScope = new HashMap<>();
    }

    void startSubroutine() {
        this.subroutineScope.clear();
    }

    void define(String name, String type, Kind kind) {
        setScope(kind);
        if (this.currentScope.get(name) != null) throw new IllegalArgumentException("can't redefine " + name);
        this.currentScope.put(name, new SymbolEntry(type, kind));
    }

    private void setScope(Kind kind) {
        if (Arrays.asList(Kind.ARG, Kind.VAR).contains(kind)) {
            this.currentScope = this.subroutineScope;
        }
        else {
            this.currentScope = this.classScope;
        }
    }

    int varCount(Kind kind) {
        setScope(kind);
        return (int) this.currentScope.values().stream().filter(entry -> entry.getKind().equals(kind)).count();
    }

    Kind kindOf(String name) {
        SymbolEntry entry = this.subroutineScope.get(name);
        if (entry == null) {
            entry = this.classScope.get(name);
        }
        return entry == null ? null : entry.getKind();
    }

    String typeOf(String name) {
        SymbolEntry entry = this.subroutineScope.get(name);
        if (entry == null) {
            entry = this.classScope.get(name);
        }
        return entry == null ? null : entry.getType();
    }

    int indexOf(String name) {
        SymbolEntry entry = this.subroutineScope.get(name);
        if (entry == null) {
            entry = this.classScope.get(name);
        }
        return entry == null ? null : entry.getIndex();
    }

    enum Kind {
        STATIC("static"),
        FIELD("this"),
        ARG("argument"),
        VAR("local");

        String name;

        Kind(String name) {
            this.name = name;
        }

        String getName() {
            return this.name;
        }
    }

    class SymbolEntry {
        String type;
        Kind kind;
        int index;

        SymbolEntry(String type, Kind kind) {
            this.type = type;
            this.kind = kind;
            this.index = varCount(kind);
        }

        Kind getKind() {
            return this.kind;
        }

        String getType() {
            return this.type;
        }

        int getIndex() {
            return this.index;
        }
    }

}