import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

abstract class AbstractParser {

    protected Scanner reader = null;
    protected String token = "";

    AbstractParser(String file) {
        try {
            this.reader = new Scanner(new File(file));
        } catch (FileNotFoundException fnf) {
            System.out.println("ERROR: No file " + file);
        }
    }

    boolean hasMore() {
        return reader.hasNext();
    }

    protected void advance() {
        if (!hasMore()) {
            throw new UnsupportedOperationException("Can't advance with no more commands");
        }
        next();
        while (hasMore() && token.isEmpty()) {
            next();
        }
    }

    private void next() {
        token = nextToken();
        removeComments();
        removeWhitespace();
    }

    protected abstract String nextToken();

    protected abstract void removeWhitespace();

    protected abstract void removeComments();
}
