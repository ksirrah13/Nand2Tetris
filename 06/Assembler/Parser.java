class Parser extends AbstractParser {

    protected String line;

    private Parser(String file) {
        super(file);
    }

    static Parser get(String file) {
        return new Parser(file);
    }

    @Override
    protected boolean hasMore() {
        return reader.hasNext();
    }

    @Override
    protected boolean tokenEmpty() {
        return line.isEmpty();
    }

    @Override
    protected void next() {
        line = reader.nextLine();
        removeComments();
        removeWhitespace();
    }

    private void removeWhitespace() {
        line = line.replaceAll("\\s", "");
    }

    private void removeComments() {
        line = removeLineComments(line);
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
