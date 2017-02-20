import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kyle.l.harris on 2/19/17.
 */
public class Assembler {

    public static void main(String[] args) {
        if (args[0] == null) {
            System.out.println("Usage: Assembler file.asm");
            return;
        }

        String fileName = args[0];

        if (!fileName.substring(fileName.length() - 4).equals(".asm")) {
            System.out.println("Please enter a valid file of extension .asm");
            return;
        }

        Parser parser = Parser.get(fileName);

        List<String> outputLines = new ArrayList<>();

        while (parser.hasMoreCommands()) {
            parser.advance();
            try {
                outputLines.add(translatedAsm(parser));
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }
        }

        writeOutputFile(fileName.replace(".asm", ".hack"), outputLines);

        System.out.println("New file generated: " + fileName.replace(".asm", ".hack"));
    }

    private static String translatedAsm(Parser parser) throws IllegalArgumentException {
        StringBuilder result = new StringBuilder();
        if (parser.commandType() == Parser.CommandType.A_COMMAND) {
            result.append("0");
            result.append(convertToPaddedBinary(Integer.parseInt(parser.symbol())));
        } else if (parser.commandType() == Parser.CommandType.C_COMMAND) {
            result.append("111");
            result.append(Code.get().comp(parser.comp()));
            result.append(Code.get().dest(parser.dest()));
            result.append(Code.get().jump(parser.jump()));
        } else {
            result.append("L_COMMAND");
        }
        return result.toString();
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
    }
}
