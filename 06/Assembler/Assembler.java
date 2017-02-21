import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class Assembler {

    private static SymbolTable symbolTable = new SymbolTable();

    public static void main(String[] args) {

        String fileName = args[0];
        List<String> translatedOutput;

        try {
            validateInput(fileName);
            buildSymbolTable(fileName);
            translatedOutput = translateAsmToBinary(fileName);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        writeOutputFile(fileName.replace(".asm", ".hack"), translatedOutput);
    }

    private static void validateInput(String filename) throws IllegalArgumentException {
        if (filename == null) {
            throw new IllegalArgumentException("Usage: Assembler file.asm");
        }
        if (!filename.substring(filename.length() - 4).equals(".asm")) {
            throw new IllegalArgumentException("Please enter a valid file of extension .asm");
        }
    }

    private static void buildSymbolTable(String fileName) throws IllegalArgumentException {
        int currentLine = -1;
        Parser parser = Parser.get(fileName);
        while (parser.hasMoreCommands()) {
            parser.advance();
            currentLine++;
            if (parser.commandType() == Parser.CommandType.L_COMMAND) {
                symbolTable.addEntry(parser.symbol(), currentLine);
                // skips the label line for the count
                currentLine--;
            }
        }
    }

    private static List<String> translateAsmToBinary(String fileName) throws IllegalArgumentException {
        List<String> outputLines = new ArrayList<>();
        Parser parser = Parser.get(fileName);
        while (parser.hasMoreCommands()) {
            parser.advance();
            if (parser.commandType() == Parser.CommandType.L_COMMAND) {
                continue;
            }
            outputLines.add(translatedAsm(parser));
        }
        return outputLines;
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
        if (symbol.matches("^-?\\d+$")) {
            return convertToPaddedBinary(Integer.parseInt(symbol));
        }
        if (!symbolTable.contains(symbol)) {
            symbolTable.addEntry(symbol);
        }
        return convertToPaddedBinary(symbolTable.getAddress(symbol));
    }

    private static String convertToPaddedBinary(int i) {
        return String.format("%15s", Integer.toBinaryString(i)).replace(' ', '0');
    }

    private static void writeOutputFile(String outFileName, List<String> outputLines) {
        Path fileOut = Paths.get(outFileName);

        try {
            Files.write(fileOut, outputLines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.println("Error writing to file " + outFileName);
        }

        System.out.println("New file generated: " + outFileName);
    }
}
