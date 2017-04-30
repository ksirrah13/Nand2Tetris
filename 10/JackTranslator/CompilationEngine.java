import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CompilationEngine {

    private JackTokenizer tokenizer;

    private CompilationEngine(JackTokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    static CompilationEngine get(JackTokenizer tokenizer) {
        return new CompilationEngine(tokenizer);
    }

    private String start(String input) {
        return "<" + input + ">";
    }

    private String end(String input) {
        return "</" + input + ">";
    }

    private String wrap(String inner) {
        return start(tokenizer.getXml()) + " " + inner + " " + end(tokenizer.getXml());
    }

    private String wrappedType() {
        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD) {
            return wrap(tokenizer.keyword());
        } else {
            return wrap(tokenizer.identifier());
        }
    }

    private void validateKeyword(String keyword) {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenIdentifier.KEYWORD ||
                !tokenizer.keyword().equals(keyword)) {
            throw new IllegalArgumentException("Syntax Error: Expected keyword " + keyword + " but found: " + tokenizer.getToken());
        }
    }

    private void validateIdentifier() {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenIdentifier.IDENTIFIER) {
            throw new IllegalArgumentException("Syntax Error: Expected identifier but found " + tokenizer.tokenType().getXmlName());
        }
    }

    private void validateSymbols(List<String> symbols) {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenIdentifier.SYMBOL ||
                !symbols.contains(tokenizer.symbol())) {
            throw new IllegalArgumentException("Syntax Error: Expected one of symbols " + symbols.toString() + " but found " + tokenizer.symbol());
        }    }

    private void validateSymbol(String symbol) {
        tokenizer.advance();
        validateSymbolNoAdvance(symbol);
    }

    private void validateSymbolNoAdvance(String symbol) {
        if (tokenizer.tokenType() != JackTokenizer.TokenIdentifier.SYMBOL ||
                !tokenizer.symbol().equals(symbol)) {
            throw new IllegalArgumentException("Syntax Error: Expected symbol " + symbol + " but found " + tokenizer.symbol());
        }
    }

    private void validateType() {
        tokenizer.advance();
        if (!isType()) {
            throw new IllegalArgumentException("Syntax Error: Expected type int, char, boolean or identifier");
        }
    }

    private void validateTypeAndVoid() {
        tokenizer.advance();
        if (!isType() && !isVoid()) {
            throw new IllegalArgumentException("Syntax Error: Expected type int, char, boolean, void or identifier");
        }
    }

    private boolean isType() {
        return (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                Arrays.asList("int", "char", "boolean").contains(tokenizer.keyword())) ||
                tokenizer.tokenType() == JackTokenizer.TokenIdentifier.IDENTIFIER;
    }

    private boolean isVoid() {
        return tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                tokenizer.keyword().equals("void");
    }

    private void validateStatement() {
        tokenizer.advance();
        if (!isStatement()) {
            throw new IllegalArgumentException("Syntax Error: Expected let, if , while, do or return");
        }
    }

    private boolean isStatement() {
        return tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                Arrays.asList("let", "if", "while", "do", "return").contains(tokenizer.keyword());
    }

    private void validateTerm() {
        tokenizer.advance();
        if (!isTerm()) {
            throw new IllegalArgumentException("Syntax Error: Expected term");
        }
    }

    private boolean isTerm() {
        return isKeywordConstant() || isUnaryOp() || isOpenParen() ||
                tokenizer.tokenType() == JackTokenizer.TokenIdentifier.IDENTIFIER ||
                tokenizer.tokenType() == JackTokenizer.TokenIdentifier.INT_CONST ||
                tokenizer.tokenType() == JackTokenizer.TokenIdentifier.STRING_CONST;
    }

    private boolean isComma() {
        return tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                tokenizer.symbol().equals(",");
    }

    private boolean isKeywordConstant() {
        return tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                Arrays.asList("true", "false", "null", "this").contains(tokenizer.keyword());
    }

    private boolean isUnaryOp() {
        return tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                Arrays.asList("-", "~").contains(tokenizer.symbol());
    }

    private boolean isOp() {
        return tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                Arrays.asList("+", "-", "*", "/", "&", "|", "<", ">", "=").contains(tokenizer.symbol());
    }

    private boolean isOpenParen() {
        return tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                tokenizer.symbol().equals("(");
    }

    List<String> compile() {
            validateKeyword("class");
            return compileClass();
    }

    private List<String> compileClass() {
        List<String> output = new ArrayList<>();
        output.add(start("class"));
        output.add(wrap(tokenizer.keyword()));

        validateIdentifier();
        output.add(wrap(tokenizer.identifier()));

        validateSymbol("{");
        output.add(wrap(tokenizer.symbol()));

        tokenizer.advance();
        while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                Arrays.asList("static", "field").contains(tokenizer.keyword())) {
            output.addAll(compileClassVarDec());

            tokenizer.advance();
        }

        while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                Arrays.asList("constructor", "function", "method").contains(tokenizer.keyword())) {
            output.addAll(compileSubroutineDec());

            tokenizer.advance();
        }

        validateSymbolNoAdvance("}");

        output.add(end("class"));
        return output;
    }

    private List<String> compileClassVarDec() {
        List<String> output = new ArrayList<>();
        output.add(start("classVarDec"));
        output.add(wrap(tokenizer.keyword()));

        validateType();
        output.add(wrappedType());

        validateIdentifier();
        output.add(wrap(tokenizer.identifier()));

        tokenizer.advance();
        while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                tokenizer.symbol().equals(",")) {
            output.add(wrap(tokenizer.symbol()));

            validateIdentifier();
            output.add(wrap(tokenizer.identifier()));

            tokenizer.advance();
        }

        validateSymbolNoAdvance(";");
        output.add(wrap(tokenizer.symbol()));

        output.add(end("classVarDec"));
        return output;
    }

    private List<String> compileSubroutineDec() {
        List<String> output = new ArrayList<>();
        output.add(start("subroutineDec"));
        output.add(wrap(tokenizer.keyword()));

        validateTypeAndVoid();
        output.add(wrappedType());

        validateIdentifier();
        output.add(wrap(tokenizer.identifier()));

        validateSymbol("(");
        output.add(wrap(tokenizer.symbol()));

        tokenizer.advance();
        output.addAll(compileParameterList());

        validateSymbolNoAdvance(")");
        output.add(wrap(tokenizer.symbol()));

        validateSymbol("{");
        output.addAll(compileSubroutineBody());

        return output;
    }

    private List<String> compileParameterList() {
        List<String> output = new ArrayList<>();
        if (isType()) {
            output.add(start("parameterList"));

            output.add(wrappedType());

            validateIdentifier();
            output.add(wrap(tokenizer.identifier()));

            tokenizer.advance();
            while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                    tokenizer.symbol().equals(",")) {
                output.add(wrap(tokenizer.symbol()));

                validateType();
                output.add(wrappedType());

                validateIdentifier();
                output.add(wrap(tokenizer.identifier()));

                tokenizer.advance();
            }

            output.add(end("parameterList"));
        }
        return output;
    }

    private List<String> compileSubroutineBody() {
        List<String> output = new ArrayList<>();
        output.add(start("subroutineBody"));
        output.add(wrap(tokenizer.symbol()));

        tokenizer.advance();
        while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                tokenizer.keyword().equals("var")) {
            output.addAll(compileVarDec());

            tokenizer.advance();
        }

        if (isStatement()) {
            output.addAll(compileStatements());
        }

        validateSymbolNoAdvance("}");
        output.add(wrap(tokenizer.symbol()));

        output.add(end("subroutineBody"));
        return output;
    }

    private List<String> compileVarDec() {
        List<String> output = new ArrayList<>();
        output.add(start("varDec"));
        output.add(wrap(tokenizer.keyword()));

        validateType();
        output.add(wrappedType());

        validateIdentifier();
        output.add(wrap(tokenizer.identifier()));

        tokenizer.advance();
        while (isComma()) {
            output.add(wrap(tokenizer.symbol()));

            validateIdentifier();
            output.add(wrap(tokenizer.identifier()));

            tokenizer.advance();
        }

        validateSymbolNoAdvance(";");
        output.add(wrap(tokenizer.symbol()));

        output.add(end("varDec"));
        return output;
    }

    private List<String> compileStatements() {
        List<String> output = new ArrayList<>();
        output.add(start("statements"));

        while (isStatement()) {
            switch (tokenizer.keyword()) {
                case "let": {
                    output.addAll(compileLetStatement());
                    tokenizer.advance();
                    break;
                }
                case "if": {
                    output.addAll(compileIfStatement());
                    break;
                }
                case "while": {
                    output.addAll(compileWhileStatement());
                    tokenizer.advance();
                    break;
                }
                case "do": {
                    output.addAll(compileDoStatement());
                    tokenizer.advance();
                    break;
                }
                case "return": {
                    output.addAll(compileReturnStatement());
                    tokenizer.advance();
                    break;
                }
            }
        }

        output.add(end("statements"));
        return output;
    }

    private List<String> compileLetStatement() {
        List<String> output = new ArrayList<>();
        output.add(start("letStatement"));
        output.add(wrap(tokenizer.keyword()));

        validateIdentifier();
        output.add(wrap(tokenizer.identifier()));

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                tokenizer.symbol().equals("[")) {
            output.add(wrap(tokenizer.symbol()));

            validateTerm();
            output.addAll(compileExpression());

            validateSymbolNoAdvance("]");
            output.add(wrap(tokenizer.symbol()));

            tokenizer.advance();
        }

        validateSymbolNoAdvance("=");
        output.add(wrap(tokenizer.symbol()));

        validateTerm();
        output.addAll(compileExpression());

        validateSymbolNoAdvance(";");
        output.add(wrap(tokenizer.symbol()));

        output.add(end("letStatement"));
        return output;
    }

    private List<String> compileExpression() {
        List<String> output = new ArrayList<>();
        output.add(start("expression"));
        output.addAll(compileTerm());

        tokenizer.advance();
        while (isOp()) {
            output.add(wrap(tokenizer.symbol()));

            validateTerm();
            output.addAll(compileTerm());

            tokenizer.advance();
        }

        output.add(end("expression"));
        return output;
    }

    private List<String> compileTerm() {
        List<String> output = new ArrayList<>();
        output.add(start("term"));

        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.INT_CONST) {
            output.add(wrap(tokenizer.intVal()));
        } else if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.STRING_CONST) {
            output.add(wrap(tokenizer.stringVal()));
        } else if (isKeywordConstant()) {
            output.add(wrap(tokenizer.keyword()));
        } else if (isOpenParen()) {
            output.add(wrap(tokenizer.symbol()));

            validateTerm();
            output.addAll(compileExpression());

            validateSymbolNoAdvance(")");
        } else if (isUnaryOp()) {
            output.add(wrap(tokenizer.symbol()));

            validateTerm();
            output.addAll(compileTerm());
        } else if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.IDENTIFIER) {

            //todo look ahead

        }
        output.add(end("term"));
        return output;
    }

    private List<String> compileIfStatement() {
        List<String> output = new ArrayList<>();
        output.add(start("ifStatement"));

        output.add(wrap(tokenizer.keyword()));

        output.addAll(compileConditionBlock());

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                tokenizer.keyword().equals("else")) {
            output.add(wrap(tokenizer.keyword()));

            validateSymbol("{");
            output.add(tokenizer.symbol());

            validateStatement();
            output.addAll(compileStatements());

            validateSymbolNoAdvance("}");

            tokenizer.advance();
        }

        output.add(end("ifStatement"));
        return output;
    }

    private List<String> compileWhileStatement() {
        List<String> output = new ArrayList<>();
        output.add(start("whileStatement"));

        output.add(wrap(tokenizer.keyword()));

        output.addAll(compileConditionBlock());

        output.add(end("whileStatement"));
        return output;
    }

    private List<String> compileConditionBlock() {
        List<String> output = new ArrayList<>();

        validateSymbol("(");
        output.add(wrap(tokenizer.symbol()));

        validateTerm();
        output.addAll(compileExpression());

        validateSymbol(")");
        output.add(wrap(tokenizer.symbol()));

        validateSymbol("{");
        output.add(wrap(tokenizer.symbol()));

        validateStatement();
        output.addAll(compileStatements());

        validateSymbolNoAdvance("}");
        output.add(wrap(tokenizer.symbol()));

        return output;
    }

    private List<String> compileDoStatement() {
        List<String> output = new ArrayList<>();
        output.add(start("doStatement"));

        output.add(wrap(tokenizer.keyword()));

        validateIdentifier();
        output.addAll(compileSubroutineCall());

        validateSymbol(";");
        output.add(wrap(tokenizer.symbol()));

        output.add(end("doStatement"));
        return output;
    }

    private List<String> compileReturnStatement() {
        List<String> output = new ArrayList<>();
        output.add(start("returnStatement"));

        output.add(wrap(tokenizer.keyword()));

        tokenizer.advance();
        if (isTerm()) {
            output.addAll(compileExpression());
        }

        validateSymbolNoAdvance(";");
        output.add(wrap(tokenizer.symbol()));

        output.add(end("returnStatement"));
        return output;
    }

    private List<String> compileSubroutineCall() {
        List<String> output = new ArrayList<>();
        output.add(start("subroutineCall"));

        output.add(wrap(tokenizer.identifier()));

        validateSymbols(Arrays.asList(".", "("));
        if (tokenizer.symbol().equals("(")) {
            output.add(wrap(tokenizer.symbol()));

            output.addAll(compileExpressionList());

            validateSymbolNoAdvance(")");
            output.add(wrap(tokenizer.symbol()));
        } else {
            output.add(wrap(tokenizer.symbol()));

            validateIdentifier();
            output.add(wrap(tokenizer.identifier()));

            validateSymbol("(");
            output.add(wrap(tokenizer.symbol()));

            output.addAll(compileExpressionList());

            validateSymbolNoAdvance(")");
            output.add(wrap(tokenizer.symbol()));
        }

        output.add(end("subroutineCall"));
        return output;
    }

    private List<String> compileExpressionList() {
        List<String> output = new ArrayList<>();
        tokenizer.advance();

        if (isTerm()) {
            output.add(start("expressionList"));

            output.addAll(compileExpression());

            tokenizer.advance();
            while (isComma()) {
                output.add(wrap(tokenizer.symbol()));

                validateTerm();
                output.addAll(compileExpression());

                tokenizer.advance();
            }

            output.add(start("expressionList"));
        }

        return output;
    }

}
