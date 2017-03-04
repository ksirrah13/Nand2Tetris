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

        if (filePath == null) {
            System.out.println("Usage: VMTranslator file.vm or directory");
            return;
        }

        List<String> filePaths = getFilesToProcess(filePath);
        List<String> translatedOutput = new ArrayList<>();

        for (String path : filePaths) {
            List<String> fileOutput;
            try {
                validateInput(path);
                fileOutput = translateVMtoAsm(path);
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

    private static List<String> translateVMtoAsm(String filePath) throws IllegalArgumentException {
        List<String> outputLines = new ArrayList<>();
        VMParser parser = VMParser.get(filePath);
        // necessary for static segment
        String name = fileNameFromPath(filePath);
        while (parser.hasMoreCommands()) {
            parser.advance();
            switch (parser.commandType()) {
                case C_ARITHMETIC:
                    outputLines.addAll(VMCode.getArithmetic(parser.arg1()));
                    break;
                case C_PUSH:
                case C_POP:
                    outputLines.addAll(VMCode.getPushPop(parser.commandType(), parser.arg1(), parser.arg2(), name));
                    break;
            }
        }
        return outputLines;
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
