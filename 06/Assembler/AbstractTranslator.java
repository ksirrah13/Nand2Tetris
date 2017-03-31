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

abstract class AbstractTranslator {

    protected void run(String[] args) {
        String filePath = args.length != 0 ? args[0] : null;

        boolean debug = args.length > 1 && args[1].equals("t");

        if (filePath == null) {
            System.out.println(getFileNullMessage());
            return;
        }

        List<String> filePaths = getFilesToProcess(filePath);

        if (filePaths.isEmpty()) {
            System.out.println("No valid files to process");
            return;
        }

        List<String> translatedOutput = new ArrayList<>();
        translatedOutput.addAll(preTranslation());

        for (String path : filePaths) {
            List<String> fileOutput;
            try {
                preFileValidation(path);
                preFileProcessing(path);
                fileOutput = translate(path, debug);
            } catch (IllegalArgumentException e) {
                System.out.println("File: " + path + "\n---Error: " + e.getMessage());
                return;
            }
            translatedOutput.addAll(fileOutput);

            if (!combineOutput()) {
                writeOutputFile(newFileName(path), translatedOutput);
                translatedOutput.clear();
            }
        }

        if (combineOutput()) {
            writeOutputFile(newFileName(filePath), translatedOutput);
        }
    }

    private String newFileName(String filePath) {
        if (hasValidExtension(filePath)) {
            return filePath.replace(getOldExtension(), getNewExtension());
        }
        return filePath + getNewExtension();
    }

    private void writeOutputFile(String outFileName, List<String> outputLines) {
        Path fileOut = Paths.get(outFileName);

        try {
            Files.write(fileOut, outputLines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.out.println("Error writing to file " + outFileName);
        }

        System.out.println("New file generated: " + outFileName);
    }

    private List<String> getFilesToProcess(String filePath) {
        List<String> files = new ArrayList<>();
        if (hasValidExtension(filePath)) {
            files.add(filePath);
        } else {
            // path is a directory
            files.addAll(getAllFileNames(filePath));
        }
        return files;
    }

    private boolean hasValidExtension(String filePath) {
        int start = filePath.length() - getOldExtension().length();
        return filePath.substring(start > 0 ? start : 0).equals(getOldExtension());
    }

    private List<String> getAllFileNames(String directory) {
        File dir = new File(directory);
        if (dir.isDirectory() && dir.listFiles() != null) {
            //noinspection ConstantConditions
            return Arrays.stream(dir.listFiles())
                    .map(File::getName)
                    .filter(this::hasValidExtension)
                    .map(name -> dir.getName() + "/" + name)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void preFileValidation(String filePath) throws IllegalArgumentException {
        if (!new File(filePath).exists()) {
            throw new IllegalArgumentException(filePath + " not found!");
        }
    }

    protected abstract boolean combineOutput();

    protected abstract void preFileProcessing(String filePath);

    protected abstract String getNewExtension();

    protected abstract String getOldExtension();

    protected abstract List<String> preTranslation();

    protected abstract String getFileNullMessage();

    protected abstract List<String> translate(String path, boolean addComments);

}
