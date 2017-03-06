import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VMTranslator {

    public static void main(String[] args) {

        String filePath = args.length != 0 ? args[0] : null;

        boolean addComments = args.length > 1 && args[1].equals("t");

        if (filePath == null) {
            System.out.println("Usage: VMTranslator file.vm or directory");
            return;
        }

        List<String> filePaths = getFilesToProcess(filePath);
        List<String> translatedOutput = new ArrayList<>();
        translatedOutput.addAll(VMCode.getInit());

        for (String path : filePaths) {
            List<String> fileOutput;
            try {
                validateInput(path);
                fileOutput = translateVMtoAsm(path, addComments);
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                return;
            }
            translatedOutput.addAll(fileOutput);
        }

        writeOutputFile(newFileName(filePath), translatedOutput);
    }

    private static List<String> getFilesToProcess(String filePath) {
        List<String> files = new ArrayList<>();
        if (filePath.substring(filePath.length() - 3).equals(".vm")) {
            files.add(filePath);
        } else {
            // path is a directory
            files.addAll(getAllFileNames(filePath));
        }
        return files;
    }

    private static List<String> getAllFileNames(String directory) {
        File dir = new File(directory);
        if (dir.isDirectory() && dir.listFiles() != null ) {
            //noinspection ConstantConditions
            return Arrays.stream(dir.listFiles())
                    .map(File::getName)
                    .map(name -> directory + "/" + name)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static void validateInput(String filePath) throws IllegalArgumentException {
        if (!filePath.substring(filePath.length() - 3).equals(".vm")) {
            throw new IllegalArgumentException("Please enter only valid files of extension .vm");
        }
    }

    private static List<String> translateVMtoAsm(String filePath, boolean addComments) throws IllegalArgumentException {
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

    private static void addDebugComments(boolean addComments, VMParser parser, List<String> outputLines) {
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

    private static String fileNameFromPath(String filePath) {
        String name = new File(filePath).getName();
        return name.substring(0 , name.length() - 3);
    }

    private static String newFileName(String filePath) {
        if (filePath.substring(filePath.length() - 3).equals(".vm")) {
            return filePath.replace(".vm", ".asm");
        }
        return filePath + ".asm";
    }

    private static void writeOutputFile(String outFileName, List<String> outputLines) {
        Path fileOut = Paths.get(outFileName);

        try {
            Files.write(fileOut, outputLines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.println("Error writing to file " + outFileName);
            return;
        }

        System.out.println("New file generated: " + outFileName);
    }
}
