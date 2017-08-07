import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class VMWriter {

    private VMWriter() {}

    static String writePush(String seg, int index) {
        return "push " + seg + " " + index;
    }

    static String writePop(String seg, int index) {
        return "pop " + seg + " " + index;
    }

    static String writeArithmetic(String command) {
        switch (command) {
            case "+": return "add";
            case "-": return "sub";
            case "*": return "call Math.multiply 2";
            case "/": return "call Math.divide 2";
            case "=": return "eq";
            case ">": return "gt";
            case "<": return "lt";
            case "&": return "and";
            case "|": return "or";
            default: throw new IllegalArgumentException("invalid op " + command);
        }
    }

    static String writeLabel(String label) {
        return "label " + label;
    }

    static String writeGoTo(String label) {
        return "goto " + label;
    }

    static String writeIf(String label) {
        return "if-goto " + label;
    }

    static String writeCall(String name, int nArgs) {
        return "call " + name + " " + nArgs;
    }

    static String writeFunction(String name, int nLocals) {
        return "function " + name + " " + nLocals;
    }

    static String writeReturn() {
        return "return";
    }

    static String writeUnaryOp(String op) {
        if (op.equals("-")) {
            return "neg";
        }
        else {
            return "not";
        }
    }

    static List<String> writeKeywordConstant(String constant) {
        switch (constant) {
            case "null":
            case "false": return Collections.singletonList(writePush("constant", 0));
            case "true": {
                List<String> output = new ArrayList<>();
                output.add(writePush("constant", 1));
                output.add(writeUnaryOp("-"));
                return output;
            }
            case "this": return Collections.singletonList(writePush("pointer", 0));
            default: throw new IllegalArgumentException("invalid constant " + constant);
        }
    }
}

