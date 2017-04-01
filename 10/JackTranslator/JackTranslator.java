import java.util.Collections;
import java.util.List;

class JackTranslator extends AbstractTranslator {

    private JackTranslator() {}

    private static JackTranslator INSTANCE = new JackTranslator();

    public static JackTranslator get() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        get().run(args);
    }

    @Override
    protected String getNewExtension() {
        return ".vm";
    }

    @Override
    protected String getOldExtension() {
        return ".jack";
    }

    @Override
    protected List<String> preTranslation() {
        return Collections.emptyList();
    }

    @Override
    protected String getFileNullMessage() {
        return "Usage: JackTranslator file.jack or directory";
    }

    @Override
    protected List<String> translate(String path, boolean addComments) {
        JackTokenizer tokenizer = JackTokenizer.get(path);
        CompilationEngine compiler = CompilationEngine.get(tokenizer);
        return compiler.compile();
    }

    @Override
    protected boolean combineOutput() {
        return false;
    }

    @Override
    protected void preFileProcessing(String filePath) {

    }

}
