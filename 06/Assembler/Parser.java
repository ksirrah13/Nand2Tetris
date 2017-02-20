import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by kyle.l.harris on 2/19/17.
 */
public class Parser {

    private Scanner reader;
    private String line = "";

    private Parser(Scanner scanner) {
        this.reader = scanner;
    }

    static Parser get(String in) {
        try {
            Scanner scanner = new Scanner(new File(in));
            return new Parser(scanner);
        } catch (FileNotFoundException fnf) {
            System.out.println("ERROR: No file " + in);
        }
        return new Parser(null);
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
        line = line.replaceAll("\\s", "");
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
        if (line.charAt(0) == '@') {
            return CommandType.A_COMMAND;
        }
        if (line.charAt(0) == '(') {
            return CommandType.L_COMMAND;
        }
        return CommandType.C_COMMAND;
    }

    String symbol() {
        if (commandType() == CommandType.C_COMMAND) {
            throw new UnsupportedOperationException("Can't call symbol on C Commands");
        }
        if (commandType() == CommandType.A_COMMAND) {
            return line.substring(1);
        }
        if (commandType() == CommandType.L_COMMAND) {
            return line.substring(1, line.length() - 1);
        }
        // should never go here
        return "";
    }

    String dest() {
        if (commandType() != CommandType.C_COMMAND) {
            throw new UnsupportedOperationException("dest can only be called on C Commands");
        }
        if (!line.contains("=")) {
            return "";
        }
        return line.substring(0, line.indexOf('='));
    }

    String comp() {
        if (commandType() != CommandType.C_COMMAND) {
            throw new UnsupportedOperationException("comp can only be called on C Commands");
        }
        int start = line.contains("=") ? line.indexOf('=') + 1 : 0;
        int end = line.contains(";") ? line.indexOf(';') : line.length();
        return line.substring(start, end);
    }

    String jump() {
        if (commandType() != CommandType.C_COMMAND) {
            throw new UnsupportedOperationException("jump can only be called on C Commands");
        }
        if (!line.contains(";")) {
            return "";
        }
        return line.substring(line.indexOf(';') + 1);
    }

    enum CommandType {
        A_COMMAND,
        C_COMMAND,
        L_COMMAND
    }
}
