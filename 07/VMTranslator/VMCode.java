import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VMCode {

    private static int resume = 0;
    private static int functionCall = 0;

    private VMCode () {}

    static List<String> getInit() {
        List<String> block = new ArrayList<>();
        // initialize @SP to 256
        block.add("@256");
        block.add("D=A");
        block.add("@SP");
        block.add("M=D");
        // jump to Sys.init
        block.add("@Sys.init");
        block.add("0;JMP");
        return block;
    }

    static List<String> getArithmetic(String command) throws IllegalArgumentException {
        List<String> output = new ArrayList<>();

        output.add("@SP");

        switch (command) {
            case "neg": {
                output.add("A=M-1");
                output.add("M=-M");
                return output;
            }
            case "not": {
                output.add("A=M-1");
                output.add("M=!M");
                return output;
            }
        }

        output.add("AM=M-1");
        output.add("D=M");
        output.add("@SP");

        switch (command) {
            case "add": {
                output.add("A=M-1");
                output.add("M=D+M");
                return output;
            }
            case "sub": {
                output.add("A=M-1");
                output.add("M=M-D");
                return output;
            }
            case "and": {
                output.add("A=M-1");
                output.add("M=D&M");
                return output;
            }
            case "or": {
                output.add("A=M-1");
                output.add("M=D|M");
                return output;
            }
        }

        output.add("AM=M-1");
        output.add("MD=M-D");
        output.add("@FALSE" + resume);

        switch (command) {
            case "eq": {
                output.add("D;JNE");
                output.addAll(trueFalseBlock());
                return output;
            }
            case "gt": {
                output.add("D;JLE");
                output.addAll(trueFalseBlock());
                return output;
            }
            case "lt": {
                output.add("D;JGE");
                output.addAll(trueFalseBlock());
                return output;
            }
        }

        throw new IllegalArgumentException(command + " is not a valid arithmetic command");

    }

    static List<String> getPushPop(VMParser.CommandType command,
                                   String segment,
                                   String index,
                                   String file) {
        List<String> output = new ArrayList<>();

        switch (command) {
            case C_PUSH: {
                output.addAll(getPushValue(segment, index, file));
                // value to push will be in D
                output.addAll(pushFromD());
                break;
            }
            case C_POP: {
                output.addAll(popToD());
                // value to pop will be in D
                output.addAll(popToAddress(segment, index, file));
                break;
            }
        }
        return output;
    }

    static List<String> getLabel(String label, String file) {
        List<String> block = new ArrayList<>();
        block.add("(" + file + "$" + label + ")");
        return block;
    }

    static List<String> getGoto(String label, String file) {
        List<String> block = new ArrayList<>();
        block.add("@" + file + "$" + label);
        block.add("0;JMP");
        return block;
    }

    static List<String> getIf(String label, String file) {
        List<String> block = new ArrayList<>();
        block.addAll(popToD());
        block.add("@" + file + "$" + label);
        block.add("D;JNE");
        return block;
    }

    static List<String> getFunction(String function, String numLocals) {
        List<String> block = new ArrayList<>();
        block.add("(" + function + ")");
        int locals = Integer.parseInt(numLocals);
        if (locals > 0) {
            block.addAll(initializeLocals(locals));
        }
        return block;
    }

    static List<String> getCall(String function, String numArgs) {
        List<String> block = new ArrayList<>();
        // push return address
        block.add("@RETURN_FROM_" + function + "_" + functionCall);
        block.add("D=A");
        block.addAll(pushFromD());
        // push saved LCL
        block.add("@LCL");
        block.add("D=M");
        block.addAll(pushFromD());
        // push saved ARG
        block.add("@ARG");
        block.add("D=M");
        block.addAll(pushFromD());
        // push saved THIS
        block.add("@THIS");
        block.add("D=M");
        block.addAll(pushFromD());
        // push saved THAT
        block.add("@THAT");
        block.add("D=M");
        block.addAll(pushFromD());
        // update ARG for function call
        block.add("@" + numArgs);
        block.add("D=A");
        block.add("@5");
        block.add("D=D+A");
        block.add("@SP");
        block.add("D=M-D");
        block.add("@ARG");
        block.add("M=D");
        // update LCL before function
        block.add("@SP");
        block.add("D=M");
        block.add("@LCL");
        block.add("M=D");
        // jump to function
        block.add("@" + function);
        block.add("0;JMP");
        // continue after function
        block.add("(RETURN_FROM_" + function + "_" + functionCall + ")");
        functionCall++;

        return block;
    }

    static List<String> getReturn() {
        List<String> block = new ArrayList<>();

        // store frame in R14
        block.add("@LCL");
        block.add("D=M");
        block.add("@R14");
        block.add("M=D");
        // store return address
        block.addAll(R14MinusIntToD(5));
        block.add("@R13");
        block.add("M=D");
        // replace ARG[0] with top stack return value
        block.addAll(popToD());
        block.add("@ARG");
        block.add("A=M");
        block.add("M=D");
        // reset new SP location as ARG+1
        block.add("@ARG");
        block.add("D=M+1");
        block.add("@SP");
        block.add("M=D");
        // restore THAT
        block.addAll(R14MinusIntToD(1));
        block.add("@THAT");
        block.add("M=D");
        // restore THIS
        block.addAll(R14MinusIntToD(2));
        block.add("@THIS");
        block.add("M=D");
        // restore ARG
        block.addAll(R14MinusIntToD(3));
        block.add("@ARG");
        block.add("M=D");
        // restore LCL
        block.addAll(R14MinusIntToD(4));
        block.add("@LCL");
        block.add("M=D");
        // jump to return address
        block.add("@R13");
        block.add("A=M");
        block.add("0;JMP");

        return block;
    }

    // value to pushFromD must be in D register
    private static List<String> pushFromD() {
        List<String> output = new ArrayList<>();
        output.add("@SP");
        output.add("A=M");
        output.add("M=D");
        output.add("@SP");
        output.add("M=M+1");
        return output;
    }

    // popped value will be in D
    private static List<String> popToD() {
        List<String> output = new ArrayList<>();
        output.add("@SP");
        output.add("AM=M-1");
        output.add("D=M");
        return output;
    }

    private static List<String> initializeLocals(int numLocals) {
        List<String> block = new ArrayList<>();
        while (numLocals > 0) {
            block.add("@SP");
            block.add("A=M");
            block.add("M=0");
            block.add("@SP");
            block.add("M=M+1");
            numLocals--;
        }
        return block;
    }

    // subtracts the minus value from the address in @R14 and returns the stored value to D
    private static List<String> R14MinusIntToD(int minusValue) {
        List<String> block = new ArrayList<>();
        block.add("@" + minusValue);
        block.add("D=A");
        block.add("@R14");
        block.add("A=M-D");
        block.add("D=M");
        return block;
    }


    private static List<String> trueFalseBlock() {
        List<String> block = new ArrayList<>();
        block.add("@SP");
        block.add("A=M");
        block.add("M=-1");
        block.add("@SP");
        block.add("M=M+1");
        block.add("@RESUME" + resume);
        block.add("0;JMP");
        block.add("(FALSE" + resume + ")");
        block.add("@SP");
        block.add("A=M");
        block.add("M=0");
        block.add("@SP");
        block.add("M=M+1");
        block.add("(RESUME" + resume + ")");
        resume++;
        return block;
    }

    // retrieved value must be stored in D register on return
    private static List<String> getPushValue(String segment, String index, String file)
            throws IllegalArgumentException {
        List<String> block = new ArrayList<>();

        if (segment.equals("static")) {
            block.add("@" + file + "." + index);
            block.add("D=M");
            return block;
        }

        block.add("@" + index);
        block.add("D=A");

        if (segment.equals("constant")) {
            return block;
        }

        switch (segment) {
            case "local": {
                block.add("@LCL");
                break;
            }
            case "argument": {
                block.add("@ARG");
                break;
            }
            case "this": {
                block.add("@THIS");
                break;
            }
            case "that": {
                block.add("@THAT");
                break;
            }
            case "temp": {
                block.add("@R5");
                block.add("A=A+D");
                block.add("D=M");
                return block;
            }
            case "pointer": {
                block.add("@THIS");
                block.add("A=A+D");
                block.add("D=M");
                return block;
            }
            default: {
                throw new IllegalArgumentException(segment + " is not a valid segment");
            }
        }

        block.add("A=M+D");
        block.add("D=M");

        return block;
    }

     // incoming value must be available in D
    private static List<String> popToAddress(String segment, String index, String file)
            throws IllegalArgumentException {
        List<String> block = new ArrayList<>();

        if (segment.equals("constant")) {
            return Collections.emptyList();
        }

        if (segment.equals("static")) {
            block.add("@" + file + "." + index);
            block.add("M=D");
            return block;
        }

        block.add("@R15");
        block.add("M=D");
        block.add("@" + index);
        block.add("D=A");

        switch (segment) {
            case "local": {
                block.add("@LCL");
                break;
            }
            case "argument": {
                block.add("@ARG");
                break;
            }
            case "this": {
                block.add("@THIS");
                break;
            }
            case "that": {
                block.add("@THAT");
                break;
            }
            case "temp": {
                block.add("@R5");
                block.add("D=A+D");
                block.addAll(storeAtAddressD());
                return block;
            }
            case "pointer": {
                block.add("@THIS");
                block.add("D=A+D");
                block.addAll(storeAtAddressD());
                return block;
            }
            default: {
                throw new IllegalArgumentException(segment + " is not a valid segment");
            }
        }

        block.add("D=M+D");
        block.addAll(storeAtAddressD());

        return block;
    }

    // takes the value in @R15 and stores it at address value from D
    private static List<String> storeAtAddressD() {
        List<String> block = new ArrayList<>();

        block.add("@R14");
        block.add("M=D");
        block.add("@R15");
        block.add("D=M");
        block.add("@R14");
        block.add("A=M");
        block.add("M=D");

        return block;
    }
}
