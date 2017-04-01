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

    private void validateTokenType(JackTokenizer.TokenIdentifier id) {
        tokenizer.advance();
        if (tokenizer.tokenType() != id) {
            throw new IllegalArgumentException("Syntax Error: Expected type " + id.getXmlName() + " but found " + tokenizer.tokenType().getXmlName());
        }
    }

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

        validateSymbol(";");
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

        tokenizer.advance();
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
            while(tokenizer.tokenType() == JackTokenizer.TokenIdentifier.SYMBOL &&
                    tokenizer.symbol().equals(",")) {
                output.add(wrap(tokenizer.symbol()));

                validateType();
                output.add(wrappedType());

                validateIdentifier();
                output.add(tokenizer.identifier());

                tokenizer.advance();
            }

            output.add(end("parameterList"));
        }
        return output;
    }

    private List<String> compileSubroutineBody() {
        List<String> output = new ArrayList<>();
        output.add(start("subroutineBody"));

        output.add(end("subroutineBody"));

        return output;

    }
}
