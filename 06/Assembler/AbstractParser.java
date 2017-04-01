import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

abstract class AbstractParser {

    protected Scanner reader = null;

    AbstractParser(String file) {
        try {
            this.reader = new Scanner(new File(file));
        } catch (FileNotFoundException fnf) {
            System.out.println("ERROR: No file " + file);
        }
    }

    protected void advance() {
        if (!hasMore()) {
            throw new UnsupportedOperationException("Can't advance with no more tokens");
        }
        next();
        while (hasMore() && tokenEmpty()) {
            next();
        }
    }

    protected String removeLineComments(String line) {
        if (line.contains("//")) {
            return line.substring(0, line.indexOf("//"));
        }
        return line;
    }

    protected abstract void next();

    protected abstract boolean hasMore();

    protected abstract boolean tokenEmpty();
}
