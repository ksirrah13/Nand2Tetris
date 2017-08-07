import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CompilationEngine {

    private JackTokenizer tokenizer;
    private JackSymbolTable symbolTable = new JackSymbolTable();
    private String currentClass;

    private int whileCount = 0;
    private int ifCount = 0;

    CompilationEngine(JackTokenizer tokenizer) {
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

    private String cIdentifier(boolean push) {
        return cIdentifier(tokenizer.identifier(), push);
    }

    private String cIdentifier(String id, boolean push) {
        return push
                ? VMWriter.writePush(this.symbolTable.kindOf(id).getName(), this.symbolTable.indexOf(id))
                : VMWriter.writePop(this.symbolTable.kindOf(id).getName(), this.symbolTable.indexOf(id));
    }

    private String describeIdentifier(boolean define) {
        String id = tokenizer.identifier();
        if (this.symbolTable.typeOf(id) == null) {
            return "(not in table) " + tokenizer.identifier();
        }
        String state = define ? "DEFINING: " : "ACCESSING: ";
        return  state + id +
                ", t: " + this.symbolTable.typeOf(id) +
                ", k: " + this.symbolTable.kindOf(id) +
                ", i: " + this.symbolTable.indexOf(id);
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
            return wrap(tokenizer.identifier());
        }
    }

    private String cStringVal() {
        return wrap(tokenizer.stringVal());
    }

    private String cIntVal() {
        return VMWriter.writePush("constant", Integer.parseInt(tokenizer.intVal()));
    }

    private JackSymbolTable.Kind getKind() {
        switch (tokenizer.keyword()) {
            case "static":
                return JackSymbolTable.Kind.STATIC;
            case "field":
                return JackSymbolTable.Kind.FIELD;
            default:
                throw new IllegalArgumentException("invalid kind: " + tokenizer.keyword());
        }
    }

    private String getType() {
        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD) {
            return tokenizer.keyword();
        } else {
            return tokenizer.identifier();
        }
    }

    List<String> compile() {
            validateKeyword("class");
            return compileClass();
    }

    private List<String> compileClass() {
        List<String> output = new ArrayList<>();

        validateIdentifier();
        this.currentClass = tokenizer.identifier();

        validateSymbol("{");

        tokenizer.advance();
        while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                Arrays.asList("static", "field").contains(tokenizer.keyword())) {
            compileClassVarDec();

            tokenizer.advance();
        }

        while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                Arrays.asList("constructor", "function", "method").contains(tokenizer.keyword())) {
            output.addAll(compileSubroutineDec());

            tokenizer.advance();
        }

        validateSymbolNoAdvance("}");

        return output;
    }

    private void compileClassVarDec() {
        JackSymbolTable.Kind kind = getKind();

        validateType();
        String type = getType();

        validateIdentifier();
        this.symbolTable.define(tokenizer.identifier(), type, kind);

        tokenizer.advance();
        while (isComma()) {
            validateIdentifier();
            this.symbolTable.define(tokenizer.identifier(), type, kind);

            tokenizer.advance();
        }

        validateSymbolNoAdvance(";");
    }

    private List<String> compileSubroutineDec() {
        List<String> output = new ArrayList<>();
        this.symbolTable.startSubroutine();

        String keyword = tokenizer.keyword();

        validateTypeAndVoid();

        validateIdentifier();
        String name = tokenizer.identifier();
        validateSymbol("(");

        // if method, first arg is always this
        if (keyword.equals("method")) {
            this.symbolTable.define("this", this.currentClass, JackSymbolTable.Kind.ARG);
        }

        tokenizer.advance();
        compileParameterList();

        validateSymbolNoAdvance(")");

        validateSymbol("{");
        output.addAll(compileSubroutineBody(name, keyword));

        return output;
    }

    private void compileParameterList() {
        if (isType()) {
            JackSymbolTable.Kind kind = JackSymbolTable.Kind.ARG;
            String type = getType();

            validateIdentifier();
            this.symbolTable.define(tokenizer.identifier(), type, kind);

            tokenizer.advance();
            while (isComma()) {
                validateType();
                type = getType();

                validateIdentifier();
                this.symbolTable.define(tokenizer.identifier(), type, kind);

                tokenizer.advance();
            }
        }
    }

    private List<String> compileSubroutineBody(String name, String keyword) {
        List<String> output = new ArrayList<>();
        int localVarCount = 0;

        tokenizer.advance();
        while (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                tokenizer.keyword().equals("var")) {
            localVarCount += compileVarDec();

            tokenizer.advance();
        }

        output.add(VMWriter.writeFunction(this.currentClass + "." + name, localVarCount));

        if (keyword.equals("constructor")) {
            int classFields = symbolTable.varCount(JackSymbolTable.Kind.FIELD);
            output.add(VMWriter.writePush("constant", classFields));
            output.add(VMWriter.writeCall("Memory.alloc", 1));
            output.add(VMWriter.writePop("pointer", 0));
        } else if (keyword.equals("method")) {
            output.add(VMWriter.writePush("argument", 0));
            output.add(VMWriter.writePop("pointer", 0));
        }

        if (isStatement()) {
            output.addAll(compileStatements());
        }

        validateSymbolNoAdvance("}");

        return output;
    }

    private int compileVarDec() {
        JackSymbolTable.Kind kind = JackSymbolTable.Kind.VAR;
        int localVarCount = 0;

        validateType();
        String type = getType();

        validateIdentifier();
        this.symbolTable.define(tokenizer.identifier(), type, kind);
        localVarCount++;

        tokenizer.advance();
        while (isComma()) {
            validateIdentifier();
            this.symbolTable.define(tokenizer.identifier(), type, kind);
            localVarCount++;

            tokenizer.advance();
        }

        validateSymbolNoAdvance(";");
        return localVarCount;
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

        return output;
    }

    private List<String> compileLetStatement() {
        List<String> output = new ArrayList<>();

        validateIdentifier();
        String identifer = tokenizer.identifier();

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

        validateTerm();
        output.addAll(compileExpression());

        validateSymbolNoAdvance(";");
        output.add(cIdentifier(identifer, false));

        return output;
    }

    private List<String> compileExpression() {
        List<String> output = new ArrayList<>();
        output.addAll(compileTerm());

        tokenizer.advance();
        while (isOp()) {
            String op = tokenizer.symbol();

            validateTerm();
            output.addAll(compileTerm());

            output.add(VMWriter.writeArithmetic(op));

            tokenizer.advance();
        }

        return output;
    }

    private List<String> compileTerm() {
        List<String> output = new ArrayList<>();

        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.INT_CONST) {
            output.add(cIntVal());
        } else if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.STRING_CONST) {
            output.add(cStringVal());
        } else if (isKeywordConstant()) {
            output.addAll(VMWriter.writeKeywordConstant(tokenizer.keyword()));
        } else if (isOpenParen()) {
            validateTerm();
            output.addAll(compileExpression());

            validateSymbolNoAdvance(")");
        } else if (isUnaryOp()) {
            String op = tokenizer.symbol();
            validateTerm();
            output.addAll(compileTerm());
            output.add(VMWriter.writeUnaryOp(op));
        } else if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.IDENTIFIER) {
            output.addAll(compileTermIdentifer());
        }

        return output;
    }

    private List<String> compileTermIdentifer() {
        List<String> output = new ArrayList<>();
        String nextToken = tokenizer.peekToken();

        if (nextToken.equals("[")) {
            output.add(cIdentifier(true));

            validateSymbol("[");
            output.add(cSymbol());

            validateTerm();
            output.addAll(compileExpression());

            validateSymbolNoAdvance("]");
            output.add(cSymbol());
        } else if (nextToken.equals("(") || nextToken.equals(".")) {
            output.addAll(compileSubroutineCall());
        } else {
            output.add(cIdentifier(true));
        }

        return output;
    }

    private List<String> compileIfStatement() {
        List<String> output = new ArrayList<>();

        int count = ifCount++;
        output.addAll(compileConditionBlock("IF", count));

        tokenizer.advance();
        if (tokenizer.tokenType() == JackTokenizer.TokenIdentifier.KEYWORD &&
                tokenizer.keyword().equals("else")) {

            output.add(VMWriter.writeGoTo("ELSE_END_" + count));
            output.add(VMWriter.writeLabel("IF_END_" + count));


            validateSymbol("{");
            validateStatement();
            output.addAll(compileStatements());

            validateSymbolNoAdvance("}");

            tokenizer.advance();
            output.add(VMWriter.writeLabel("ELSE_END_" + count));
        }
        else {
            output.add(VMWriter.writeLabel("IF_END_" + count));
        }

        return output;
    }

    private List<String> compileWhileStatement() {
        List<String> output = new ArrayList<>();

        int count = whileCount++;
        output.add(VMWriter.writeLabel("WHILE_START_" + count));

        output.addAll(compileConditionBlock("WHILE", count));

        output.add(VMWriter.writeGoTo("WHILE_START_" + count));
        output.add(VMWriter.writeLabel("WHILE_END_" + count));
        return output;
    }

    private List<String> compileConditionBlock(String conditionType, int count) {
        List<String> output = new ArrayList<>();

        validateSymbol("(");
        validateTerm();
        output.addAll(compileExpression());

        validateSymbolNoAdvance(")");

        output.add(VMWriter.writeUnaryOp("~"));
        output.add(VMWriter.writeIf(conditionType + "_END_" + count));
        validateSymbol("{");

        validateStatement();
        output.addAll(compileStatements());

        validateSymbolNoAdvance("}");

        return output;
    }

    private List<String> compileDoStatement() {
        List<String> output = new ArrayList<>();

        validateIdentifier();
        output.addAll(compileSubroutineCall());

        validateSymbol(";");
        // ignore output
        output.add(VMWriter.writePop("temp", 0));

        return output;
    }

    private List<String> compileReturnStatement() {
        List<String> output = new ArrayList<>();

        tokenizer.advance();
        if (isTerm()) {
            output.addAll(compileExpression());
        }
        else {
            // void statement
            output.add(VMWriter.writePush("constant", 0));
        }

        validateSymbolNoAdvance(";");
        output.add(VMWriter.writeReturn());
        return output;
    }

    private List<String> compileSubroutineCall() {
        List<String> output = new ArrayList<>();
        Tuple<List<String>, Integer> rawOut;
        String functionName;

        String identifier = tokenizer.identifier();
        String functionType = symbolTable.typeOf(identifier);
        int argCount;

        validateSymbols(Arrays.asList(".", "("));
        if (tokenizer.symbol().equals("(")) {
            // method call on same instance
            functionName = currentClass + "." + identifier;

            output.add(VMWriter.writePush("pointer", 0));
            rawOut = compileExpressionList();
            output.addAll(rawOut.getA());
            argCount = rawOut.getB() + 1;

            validateSymbolNoAdvance(")");
        }
        else if (functionType != null) {
            // method call on specified instance
            validateIdentifier();
            functionName = functionType + "." + tokenizer.identifier();

            validateSymbol("(");

            output.add(cIdentifier(identifier, true));
            rawOut = compileExpressionList();
            output.addAll(rawOut.getA());
            argCount = rawOut.getB() + 1;

            validateSymbolNoAdvance(")");
        }
        else {
            // function call
            validateIdentifier();
            functionName = identifier + "." + tokenizer.identifier();

            validateSymbol("(");

            rawOut = compileExpressionList();
            output.addAll(rawOut.getA());
            argCount = rawOut.getB();

            validateSymbolNoAdvance(")");
        }

        output.add(VMWriter.writeCall(functionName, argCount));
        return output;
    }

    private Tuple<List<String>, Integer> compileExpressionList() {
        List<String> output = new ArrayList<>();
        tokenizer.advance();
        int exprCount = 0;

        if (isTerm()) {
            output.addAll(compileExpression());
            exprCount++;

            while (isComma()) {
                validateTerm();
                output.addAll(compileExpression());
                exprCount++;
            }

        }

        return new Tuple<>(output, exprCount);
    }

    class Tuple<X, Y> {
        private final X x;
        private final Y y;
        Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        X getA() {
            return this.x;
        }

        Y getB() {
            return this.y;
        }
    }
}
