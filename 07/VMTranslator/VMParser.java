import java.util.Arrays;
import java.util.List;

class VMParser extends AbstractParser {

    private List<String> arithmeticOps = Arrays.asList("add","sub","neg","eq","gt","lt","and","or","not");

    private VMParser(String file) {
        super(file);
    }

    static VMParser get(String file) {
       return new VMParser(file);
    }

    @Override
    protected void removeWhitespace() {
        token = token.trim().replaceAll(" +", " ");
    }

    @Override
    protected String nextToken() {
        return reader.nextLine();
    }

    @Override
    protected void removeComments() {
        if (token.contains("//")) {
            token = token.substring(0, token.indexOf("//"));
        }
    }

    CommandType commandType() {
        if (token.isEmpty()) {
            throw new UnsupportedOperationException("Can't call commandType on empty token");
        }
        String firstWord = nthWord(token, 1);
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
        if (token.isEmpty()) {
            throw new UnsupportedOperationException("Can't call arg1 on empty token");
        }
        if (commandType() == CommandType.C_RETURN) {
            throw new UnsupportedOperationException("Can't call arg1 on return command");
        }
        if (commandType() == CommandType.C_ARITHMETIC) {
            return nthWord(token, 1);
        }
        return nthWord(token, 2);
    }

    String arg2() {
        if (token.isEmpty()) {
            throw new UnsupportedOperationException("Can't call arg2 on empty token");
        }
        if (invalidForArg2(commandType())) {
            throw new UnsupportedOperationException("Invalid arg2 call for command type");
        }
        return nthWord(token, 3);
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
