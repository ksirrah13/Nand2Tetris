import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VMTranslator extends AbstractTranslator {

    private static VMTranslator INSTANCE = new VMTranslator();

    private VMTranslator() {
    }

    public static VMTranslator get() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        get().run(args);
    }

    @Override
    protected boolean combineOutput() {
        return true;
    }

    @Override
    protected void preFileProcessing(String filePath) {}

    @Override
    protected String getNewExtension() {
        return ".asm";
    }

    @Override
    protected String getOldExtension() {
        return ".vm";
    }

    @Override
    protected List<String> preTranslation() {
        return VMCode.getInit();
    }

    @Override
    protected String getFileNullMessage() {
        return "Usage: VMTranslator file.vm or directory";
    }

    @Override
    protected List<String> translate(String filePath, boolean addComments) throws IllegalArgumentException {
        List<String> outputLines = new ArrayList<>();
        VMParser parser = VMParser.get(filePath);
        // necessary for static segment
        String name = fileNameFromPath(filePath);
        if (addComments) {
            outputLines.add("// --------------------");
            outputLines.add("// file: " + name);
            outputLines.add("// --------------------");
        }
        while (parser.hasMoreCommands()) {
            parser.advance();
            addDebugComments(addComments, parser, outputLines);
            switch (parser.commandType()) {
                case C_ARITHMETIC: {
                    outputLines.addAll(VMCode.getArithmetic(parser.arg1()));
                    break;
                }
                case C_PUSH: {
                    outputLines.addAll(VMCode.getPushPop(parser.commandType(), parser.arg1(), parser.arg2(), name));
                    break;
                }
                case C_POP: {
                    outputLines.addAll(VMCode.getPushPop(parser.commandType(), parser.arg1(), parser.arg2(), name));
                    break;
                }
                case C_LABEL: {
                    outputLines.addAll(VMCode.getLabel(parser.arg1(), name));
                    break;
                }
                case C_GOTO: {
                    outputLines.addAll(VMCode.getGoto(parser.arg1(), name));
                    break;
                }
                case C_IF: {
                    outputLines.addAll(VMCode.getIf(parser.arg1(), name));
                    break;
                }
                case C_FUNCTION: {
                    outputLines.addAll(VMCode.getFunction(parser.arg1(), parser.arg2()));
                    break;
                }
                case C_CALL: {
                    outputLines.addAll(VMCode.getCall(parser.arg1(), parser.arg2()));
                    break;
                }
                case C_RETURN: {
                    outputLines.addAll(VMCode.getReturn());
                    break;
                }
            }
        }
        return outputLines;
    }

    private void addDebugComments(boolean addComments, VMParser parser, List<String> outputLines) {
        if (!addComments) {
            return;
        }

        switch (parser.commandType()) {
            case C_ARITHMETIC: {
                outputLines.add("// " + parser.arg1());
                break;
            }
            case C_PUSH: {
                outputLines.add("// push " + parser.arg1() + " " + parser.arg2());
                break;
            }
            case C_POP: {
                outputLines.add("// pop " + parser.arg1() + " " + parser.arg2());
                break;
            }
            case C_LABEL: {
                outputLines.add("// label " + parser.arg1());
                break;
            }
            case C_GOTO: {
                outputLines.add("// goto " + parser.arg1());
                break;
            }
            case C_IF: {
                outputLines.add("// if-goto " + parser.arg1());
                break;
            }
            case C_FUNCTION: {
                outputLines.add("// function " + parser.arg1() + " " + parser.arg2());
                break;
            }
            case C_CALL: {
                outputLines.add("// call " + parser.arg1() + " " + parser.arg2());
                break;
            }
            case C_RETURN: {
                outputLines.add("// return");
                break;
            }
        }
    }

    private String fileNameFromPath(String filePath) {
        String name = new File(filePath).getName();
        return name.substring(0, name.length() - 3);
    }
}
