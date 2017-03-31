class JackTokenizer extends AbstractParser {

    private JackTokenizer(String file) {
        super(file);
    }

    static JackTokenizer get(String file) {
        return new JackTokenizer(file);
    }

    @Override
    protected String nextToken() {
        return null;
    }

    @Override
    protected void removeWhitespace() {

    }

    @Override
    protected void removeComments() {

    }
}
