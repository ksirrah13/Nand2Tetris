class Parser extends AbstractParser {

    private Parser(String file) {
        super(file);
    }

    static Parser get(String file) {
        return new Parser(file);
    }

    @Override
    protected void removeWhitespace() {
        token = token.replaceAll("\\s", "");
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
        if (token.charAt(0) == '@') {
            return CommandType.A_COMMAND;
        }
        if (token.charAt(0) == '(') {
            return CommandType.L_COMMAND;
        }
        return CommandType.C_COMMAND;
    }

    String symbol() {
        if (commandType() == CommandType.C_COMMAND) {
            throw new UnsupportedOperationException("Can't call symbol on C Commands");
        }
        if (commandType() == CommandType.A_COMMAND) {
            return token.substring(1);
        }
        if (commandType() == CommandType.L_COMMAND) {
            return token.substring(1, token.length() - 1);
        }
        // should never go here
        return "";
    }

    String dest() {
        if (commandType() != CommandType.C_COMMAND) {
            throw new UnsupportedOperationException("dest can only be called on C Commands");
        }
        if (!token.contains("=")) {
            return "";
        }
        return token.substring(0, token.indexOf('='));
    }

    String comp() {
        if (commandType() != CommandType.C_COMMAND) {
            throw new UnsupportedOperationException("comp can only be called on C Commands");
        }
        int start = token.contains("=") ? token.indexOf('=') + 1 : 0;
        int end = token.contains(";") ? token.indexOf(';') : token.length();
        return token.substring(start, end);
    }

    String jump() {
        if (commandType() != CommandType.C_COMMAND) {
            throw new UnsupportedOperationException("jump can only be called on C Commands");
        }
        if (!token.contains(";")) {
            return "";
        }
        return token.substring(token.indexOf(';') + 1);
    }

    enum CommandType {
        A_COMMAND,
        C_COMMAND,
        L_COMMAND
    }
}
