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

    private void validateKeyword(String keyword) {
        tokenizer.advance();
        if (tokenizer.tokenType() != JackTokenizer.TokenIdentifier.KEYWORD ||
                !tokenizer.keyword().equals(keyword)) {
            throw new IllegalArgumentException("Syntax Error: Expected keyword " + keyword + " but found: " + tokenizer.keyword());
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

    private void wrapOutput(List<String> output, String tag) {
        output.add(0, start(tag));
        output.add(end(tag));
    }

    private String cKeyword() {
        return wrap(tokenizer.keyword());
    }

    private String cIdentifier() {
        return wrap(tokenizer.identifier());
    }

    private String cSymbol() {
        String symbol = tokenizer.symbol();
        if (symbol.equals("<")) {
            symbol = "lt";
        } else if (symbol.equals(">")) {
            symbol = "gt";
        }
        return wrap(symbol);
    }

    private String cType() {
        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD) {
            return cKeyword();
        } else {
            return cIdentifier();
        }
    }

    private String cStringVal() {
        return wrap(tokenizer.stringVal());
    }

    private String cIntVal() {
        return wrap(tokenizer.intVal());
    }

    List<String> compile() {
            validateKeyword("class");
            return compileClass();
    }

    private List<String> compileClass() {
        List<String> output = new ArrayList<>();
        output.add(cKeyword());

        validateIdentifier();
        output.add(cIdentifier());

        validateSymbol("{");
        output.add(cSymbol());

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
        output.add(cSymbol());

        wrapOutput(output, "class");
        return output;
    }

    private List<String> compileClassVarDec() {
        List<String> output = new ArrayList<>();
        output.add(cKeyword());

        validateType();
        output.add(cType());

        validateIdentifier();
        output.add(cIdentifier());

        tokenizer.advance();
        while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                tokenizer.symbol().equals(",")) {
            output.add(cSymbol());

            validateIdentifier();
            output.add(cIdentifier());

            tokenizer.advance();
        }

        validateSymbolNoAdvance(";");
        output.add(cSymbol());

        wrapOutput(output, "classVarDec");
        return output;
    }

    private List<String> compileSubroutineDec() {
        List<String> output = new ArrayList<>();
        output.add(cKeyword());

        validateTypeAndVoid();
        output.add(cType());

        validateIdentifier();
        output.add(cIdentifier());

        validateSymbol("(");
        output.add(cSymbol());

        tokenizer.advance();
        output.addAll(compileParameterList());

        validateSymbolNoAdvance(")");
        output.add(cSymbol());

        validateSymbol("{");
        output.addAll(compileSubroutineBody());

        wrapOutput(output, "subroutineDec");
        return output;
    }

    private List<String> compileParameterList() {
        List<String> output = new ArrayList<>();
        if (isType()) {
            output.add(cType());

            validateIdentifier();
            output.add(cIdentifier());

            tokenizer.advance();
            while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                    tokenizer.symbol().equals(",")) {
                output.add(cSymbol());

                validateType();
                output.add(cType());

                validateIdentifier();
                output.add(cIdentifier());

                tokenizer.advance();
            }

            wrapOutput(output, "parameterList");
        }
        return output;
    }

    private List<String> compileSubroutineBody() {
        List<String> output = new ArrayList<>();
        output.add(cSymbol());

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
        output.add(cSymbol());

        wrapOutput(output, "subroutineBody");
        return output;
    }

    private List<String> compileVarDec() {
        List<String> output = new ArrayList<>();
        output.add(cKeyword());

        validateType();
        output.add(cType());

        validateIdentifier();
        output.add(cIdentifier());

        tokenizer.advance();
        while (isComma()) {
            output.add(cSymbol());

            validateIdentifier();
            output.add(cIdentifier());

            tokenizer.advance();
        }

        validateSymbolNoAdvance(";");
        output.add(cSymbol());

        wrapOutput(output, "varDec");
        return output;
    }

    private List<String> compileStatements() {
        List<String> output = new ArrayList<>();

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

        wrapOutput(output, "statements");
        return output;
    }

    private List<String> compileLetStatement() {
        List<String> output = new ArrayList<>();
        output.add(cKeyword());

        validateIdentifier();
        output.add(cIdentifier());

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                tokenizer.symbol().equals("[")) {
            output.add(cSymbol());

            validateTerm();
            output.addAll(compileExpression());

            validateSymbolNoAdvance("]");
            output.add(cSymbol());

            tokenizer.advance();
        }

        validateSymbolNoAdvance("=");
        output.add(cSymbol());

        validateTerm();
        output.addAll(compileExpression());

        validateSymbolNoAdvance(";");
        output.add(cSymbol());

        wrapOutput(output, "letStatement");
        return output;
    }

    private List<String> compileExpression() {
        List<String> output = new ArrayList<>();
        output.addAll(compileTerm());

        tokenizer.advance();
        while (isOp()) {
            output.add(cSymbol());

            validateTerm();
            output.addAll(compileTerm());

            tokenizer.advance();
        }

        wrapOutput(output, "expression");
        return output;
    }

    private List<String> compileTerm() {
        List<String> output = new ArrayList<>();

        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.INT_CONST) {
            output.add(cIntVal());
        } else if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.STRING_CONST) {
            output.add(cStringVal());
        } else if (isKeywordConstant()) {
            output.add(cKeyword());
        } else if (isOpenParen()) {
            output.add(cSymbol());

            validateTerm();
            output.addAll(compileExpression());

            validateSymbolNoAdvance(")");
        } else if (isUnaryOp()) {
            output.add(cSymbol());

            validateTerm();
            output.addAll(compileTerm());
        } else if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.IDENTIFIER) {
            output.addAll(compileTermIdentifer());
        }

        wrapOutput(output, "term");
        return output;
    }

    private List<String> compileTermIdentifer() {
        List<String> output = new ArrayList<>();
        String nextToken = tokenizer.peekToken();

        if (nextToken.equals("[")) {
            output.add(cIdentifier());

            validateSymbol("[");
            output.add(cSymbol());

            validateTerm();
            output.addAll(compileExpression());

            validateSymbolNoAdvance("]");
            output.add(cSymbol());
        } else if (nextToken.equals("(") || nextToken.equals(".")) {
            output.addAll(compileSubroutineCall());
        } else {
            output.add(cIdentifier());
        }

        return output;
    }

    private List<String> compileIfStatement() {
        List<String> output = new ArrayList<>();

        output.add(cKeyword());

        output.addAll(compileConditionBlock());

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                tokenizer.keyword().equals("else")) {
            output.add(cKeyword());

            validateSymbol("{");
            output.add(cSymbol());

            validateStatement();
            output.addAll(compileStatements());

            validateSymbolNoAdvance("}");

            tokenizer.advance();
        }

        wrapOutput(output, "ifStatement");
        return output;
    }

    private List<String> compileWhileStatement() {
        List<String> output = new ArrayList<>();

        output.add(cKeyword());

        output.addAll(compileConditionBlock());

        wrapOutput(output, "whileStatement");
        return output;
    }

    private List<String> compileConditionBlock() {
        List<String> output = new ArrayList<>();

        validateSymbol("(");
        output.add(cSymbol());

        validateTerm();
        output.addAll(compileExpression());

        validateSymbolNoAdvance(")");
        output.add(cSymbol());

        validateSymbol("{");
        output.add(cSymbol());

        validateStatement();
        output.addAll(compileStatements());

        validateSymbolNoAdvance("}");
        output.add(cSymbol());

        return output;
    }

    private List<String> compileDoStatement() {
        List<String> output = new ArrayList<>();

        output.add(cKeyword());

        validateIdentifier();
        output.addAll(compileSubroutineCall());

        validateSymbol(";");
        output.add(cSymbol());

        wrapOutput(output, "doStatement");
        return output;
    }

    private List<String> compileReturnStatement() {
        List<String> output = new ArrayList<>();

        output.add(cKeyword());

        tokenizer.advance();
        if (isTerm()) {
            output.addAll(compileExpression());
        }

        validateSymbolNoAdvance(";");
        output.add(cSymbol());

        wrapOutput(output, "returnStatement");
        return output;
    }

    private List<String> compileSubroutineCall() {
        List<String> output = new ArrayList<>();

        output.add(cIdentifier());

        validateSymbols(Arrays.asList(".", "("));
        if (tokenizer.symbol().equals("(")) {
            output.add(cSymbol());

            output.addAll(compileExpressionList());

            validateSymbolNoAdvance(")");
            output.add(cSymbol());
        } else {
            output.add(cSymbol());

            validateIdentifier();
            output.add(cIdentifier());

            validateSymbol("(");
            output.add(cSymbol());

            output.addAll(compileExpressionList());

            validateSymbolNoAdvance(")");
            output.add(cSymbol());
        }

        wrapOutput(output, "subroutineCall");
        return output;
    }

    private List<String> compileExpressionList() {
        List<String> output = new ArrayList<>();
        tokenizer.advance();

        if (isTerm()) {
            output.addAll(compileExpression());

            while (isComma()) {
                output.add(cSymbol());

                validateTerm();
                output.addAll(compileExpression());
            }

            wrapOutput(output, "expressionList");
        }

        return output;
    }

}
