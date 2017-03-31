import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


class Assembler extends AbstractTranslator {

    private static SymbolTable symbolTable = new SymbolTable();
    private static Assembler INSTANCE = new Assembler();

    private Assembler() {
    }

    public static Assembler get() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        get().run(args);
    }

    @Override
    protected String getNewExtension() {
        return ".hack";
    }

    @Override
    protected String getOldExtension() {
        return ".asm";
    }

    @Override
    protected List<String> preTranslation() {
        return Collections.emptyList();
    }

    @Override
    protected String getFileNullMessage() {
        return "Usage: Assembler file.asm or directory";
    }

    @Override
    protected List<String> translate(String path, boolean addComments) {
        List<String> outputLines = new ArrayList<>();
        Parser parser = Parser.get(path);
        while (parser.hasMore()) {
            parser.advance();
            if (parser.commandType() == Parser.CommandType.L_COMMAND) {
                continue;
            }
            outputLines.add(translatedAsm(parser));
        }
        return outputLines;
    }

    @Override
    protected boolean combineOutput() {
        return false;
    }

    @Override
    protected void preFileProcessing(String filename) throws IllegalArgumentException {
        symbolTable.clear();
        buildSymbolTable(filename);
    }

    private static void buildSymbolTable(String fileName) throws IllegalArgumentException {
        int currentLine = -1;
        Parser parser = Parser.get(fileName);
        while (parser.hasMore()) {
            parser.advance();
            currentLine++;
            if (parser.commandType() == Parser.CommandType.L_COMMAND) {
                symbolTable.addEntry(parser.symbol(), currentLine);
                // skips the label token for the count
                currentLine--;
            }
        }
    }

    private static String translatedAsm(Parser parser) throws IllegalArgumentException {
        StringBuilder result = new StringBuilder();
        if (parser.commandType() == Parser.CommandType.A_COMMAND) {
            result.append("0");
            result.append(getSymbolValueOrConstant(parser.symbol()));
        } else if (parser.commandType() == Parser.CommandType.C_COMMAND) {
            result.append("111");
            result.append(Code.get().comp(parser.comp()));
            result.append(Code.get().dest(parser.dest()));
            result.append(Code.get().jump(parser.jump()));
        }
        return result.toString();
    }

    private static String getSymbolValueOrConstant(String symbol) {
        // must be non-negative if constant
        if (symbol.matches("^\\d+$")) {
            return convertToPaddedBinary(Integer.parseInt(symbol));
        }
        if (!symbolTable.contains(symbol)) {
            symbolTable.addEntry(symbol);
        }
        return convertToPaddedBinary(symbolTable.getAddress(symbol));
    }

    private static String convertToPaddedBinary(int i) {
        // always non-negative values
        return String.format("%15s", Integer.toBinaryString(i)).replace(' ', '0');
    }
}
