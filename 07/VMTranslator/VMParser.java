import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

class VMParser {

    private Scanner reader;
    private String line = "";

    private List<String> arithmeticOps = Arrays.asList("add","sub","neg","eq","gt","lt","and","or","not");

    private VMParser(Scanner scanner) {
        this.reader = scanner;
    }

    static VMParser get(String in) {
        try {
            Scanner scanner = new Scanner(new File(in));
            return new VMParser(scanner);
        } catch (FileNotFoundException fnf) {
            System.out.println("ERROR: No file " + in);
        }
        return new VMParser(null);
    }

    boolean hasMoreCommands() {
        return reader.hasNext();
    }

    void advance() {
        if (!hasMoreCommands()) {
            throw new UnsupportedOperationException("Can't advance with no more commands");
        }
        line = reader.nextLine();
        removeComments();
        removeWhitespace();
        while (hasMoreCommands() && line.isEmpty()) {
            line = reader.nextLine();
            removeComments();
            removeWhitespace();
        }
    }

    private void removeWhitespace() {
        line = line.trim().replaceAll(" +", " ");
    }

    private void removeComments() {
        if (line.contains("//")) {
            line = line.substring(0, line.indexOf("//"));
        }
    }

    CommandType commandType() {
        if (line.isEmpty()) {
            throw new UnsupportedOperationException("Can't call commandType on empty line");
        }
        String firstWord = nthWord(line, 1);
        if (arithmeticOps.contains(firstWord)) {
            return CommandType.C_ARITHMETIC;
        }
        switch (firstWord) {
            case "push" : return CommandType.C_PUSH;
            case "pop" : return CommandType.C_POP;
            case "label" : return CommandType.C_LABEL;
            case "goto" : return CommandType.C_GOTO;
            case "if-goto" : return CommandType.C_IF;
            case "function" : return CommandType.C_FUNCTION;
            case "call" : return CommandType.C_CALL;
            case "return" : return CommandType.C_RETURN;
            default : throw new UnsupportedOperationException(firstWord + " is invalid syntax");
        }
    }

    String arg1() {
        if (line.isEmpty()) {
            throw new UnsupportedOperationException("Can't call arg1 on empty line");
        }
        if (commandType() == CommandType.C_RETURN) {
            throw new UnsupportedOperationException("Can't call arg1 on return command");
        }
        if (commandType() == CommandType.C_ARITHMETIC) {
            return nthWord(line, 1);
        }
        return nthWord(line, 2);
    }

    String arg2() {
        if (line.isEmpty()) {
            throw new UnsupportedOperationException("Can't call arg2 on empty line");
        }
        if (invalidForArg2(commandType())) {
            throw new UnsupportedOperationException("Invalid arg2 call for command type");
        }
        return nthWord(line, 3);
    }

    private String nthWord(String line, int n) {
        return line.split(" ")[n-1];
    }

    private boolean invalidForArg2(VMParser.CommandType cmd) {
        return Arrays.asList(CommandType.C_ARITHMETIC,
                CommandType.C_LABEL,
                CommandType.C_GOTO,
                CommandType.C_IF,
                CommandType.C_RETURN).contains(cmd);
    }

    enum CommandType {
        C_ARITHMETIC,
        C_PUSH,
        C_POP,
        C_LABEL,
        C_GOTO,
        C_IF,
        C_FUNCTION,
        C_RETURN,
        C_CALL
    }
}
