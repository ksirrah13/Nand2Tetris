import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VMCode {

    private static int resume = 0;

    private VMCode () {}

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

    static List<String> getPushPop(VMParser.CommandType command,
                                   String segment,
                                   String index,
                                   String file) {
        List<String> output = new ArrayList<>();

        switch (command) {
            case C_PUSH: {
                output.addAll(getPushValue(segment, index, file));
                // value to push will be in D
                output.add("@SP");
                output.add("A=M");
                output.add("M=D");
                output.add("@SP");
                output.add("M=M+1");
                break;
            }
            case C_POP: {
                output.add("@SP");
                output.add("AM=M-1");
                output.add("D=M");
                output.add("@R15");
                output.add("M=D");
                // value to pop will be in @R15
                output.addAll(popToAddress(segment, index, file));
                break;
            }
        }
        return output;
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

     // incoming value will be available at address @R15
    private static List<String> popToAddress(String segment, String index, String file)
            throws IllegalArgumentException {
        List<String> block = new ArrayList<>();

        if (segment.equals("constant")) {
            return Collections.emptyList();
        }

        if (segment.equals("static")) {
            block.add("@R15");
            block.add("D=M");
            block.add("@" + file + "." + index);
            block.add("M=D");
            return block;
        }

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
