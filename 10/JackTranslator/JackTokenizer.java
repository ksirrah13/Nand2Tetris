import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class JackTokenizer extends AbstractParser {

    private String currentLine = "";
    private Token currentToken = null;
    private boolean insideBlockComment = false;

    private JackTokenizer(String file) {
        super(file);
    }

    static JackTokenizer get(String file) {
        return new JackTokenizer(file);
    }

    private static List<String> keywords =
            Arrays.asList("class", "method", "function", "constructor", "int", "boolean", "char", "void", "var",
                    "static", "field", "let", "do", "if", "else", "while", "return", "true", "false", "null", "this");
    private static String symbolsRegex = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";

    private static String buildRegEx(List<String> list) {
        StringBuilder buffer = new StringBuilder();
        for (String item : list) {
            buffer.append(item);
            buffer.append("|");
        }
        String result = buffer.toString();
        return result.substring(0, result.length() - 1);
    }

    @Override
    protected boolean hasMore() {
        return !currentLine.isEmpty() || reader.hasNext();
    }

    @Override
    protected boolean tokenEmpty() {
        return currentToken == null || currentToken.toString().isEmpty();
    }

    @Override
    protected void next() {
        while (currentLine.isEmpty() && reader.hasNext()) {
            currentLine = reader.nextLine().trim();
            handleBlockComments();
            currentLine = removeLineComments(currentLine);
        }

        if (currentLine.isEmpty()) {
            return;
        }

        for (TokenIdentifier id : TokenIdentifier.values()) {
            Matcher test = id.getPattern().matcher(currentLine);
            if (test.find()) {
                currentToken = new Token(test.group(), id);
                currentLine = currentLine.substring(test.end());
                currentLine = currentLine.trim();
                return;
            }
        }

        throw new IllegalArgumentException("Invalid syntax " + currentLine);
    }

    private void handleBlockComments() {
        // close block
        if (insideBlockComment && closeBlock() >= 0) {
            currentLine = currentLine.substring(closeBlock()+2);
            insideBlockComment = false;
        }

        // open and close block
        if (openBlock() >= 0 && closeBlock() > openBlock()) {
            String first = currentLine.substring(0, openBlock());
            currentLine = first + currentLine.substring(closeBlock()+2);
        }

        // open block
        if (openBlock() >= 0 && closeBlock() == -1) {
            currentLine = currentLine.substring(0, openBlock());
            insideBlockComment = true;
        }

        // commented block
        if (insideBlockComment && closeBlock() == -1) {
            currentLine = "";
        }
    }

    private int closeBlock() {
        return currentLine.indexOf("*/");
    }

    private int openBlock() {
        return currentLine.indexOf("/*");
    }

    TokenIdentifier tokenType() {
        return currentToken.getId();
    }

    String keyword() {
        if (tokenType() != TokenIdentifier.KEYWORD) {
            throw new IllegalArgumentException("Token is not keyword");
        }
        return currentToken.getMatched();
    }

    String symbol() {
        if  (tokenType() != TokenIdentifier.SYMBOL) {
            throw new IllegalArgumentException("Token is not symbol");
        }
        return currentToken.getMatched();
    }

    String identifier() {
        if  (tokenType() != TokenIdentifier.IDENTIFIER) {
            throw new IllegalArgumentException("Token is not identifier");
        }
        return currentToken.getMatched();
    }

    String intVal() {
        if  (tokenType() != TokenIdentifier.INT_CONST) {
            throw new IllegalArgumentException("Token is not integer constant");
        }
        return currentToken.getMatched();
    }

    String stringVal() {
        if  (tokenType() != TokenIdentifier.STRING_CONST) {
            throw new IllegalArgumentException("Token is not string constant");
        }
        String quotedString = currentToken.getMatched();
        return quotedString.substring(1, quotedString.length() -1);
    }

    protected String getToken() {
        return currentToken.getMatched();
    }

    protected String getXml() {
        return currentToken.getId().getXmlName();
    }

    private class Token {

        private String matched;
        private TokenIdentifier id;

        public TokenIdentifier getId() {
            return id;
        }

        private Token(String matched, TokenIdentifier id )
        {
            this.matched = matched;
            this.id = id;
        }

        private String getMatched() {
            return matched;
        }
    }

    enum TokenIdentifier {
        KEYWORD("keyword", buildRegEx(keywords)),
        IDENTIFIER("identifier", "[a-zA-Z_][\\w]*"),
        INT_CONST("integerConstant", "\\d" ),
        STRING_CONST("stringConstant", "\".*\"" ),
        SYMBOL("symbol", symbolsRegex);

        private Pattern pattern;
        private String xmlName;

        TokenIdentifier(String xmlName, String regex) {
            this.xmlName = xmlName;
            pattern = Pattern.compile("^(" + regex + ")");
        }

        Pattern getPattern() {
            return pattern;
        }

        String getXmlName() {
            return xmlName;
        }
    }
}
