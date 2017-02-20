import java.util.HashMap;
import java.util.Map;

/**
 * Created by kyle.l.harris on 2/20/17.
 */
public class Code {

    private static Code instance = null;

    private Map<String, String> destCode = new HashMap<>();
    private Map<String, String> compCode = new HashMap<>();
    private Map<String, String> jumpCode = new HashMap<>();

    private Code() {}

    public static Code get() {
        if (instance == null) {
            instance = new Code();
            instance.initialize();
        }
        return instance;
    }

    String dest(String dest) throws IllegalArgumentException {
        if (!destCode.containsKey(dest)) {
            throw new IllegalArgumentException(dest + " is not correct syntax");
        }
        return destCode.get(dest);
    }

    String comp(String comp) throws IllegalArgumentException {
        if (!compCode.containsKey(comp)) {
            throw new IllegalArgumentException(comp + " is not correct syntax");
        }
        return compCode.get(comp);
    }

    String jump(String jump) throws IllegalArgumentException {
        if (!jumpCode.containsKey(jump)) {
            throw new IllegalArgumentException(jump + " is not correct syntax");
        }
        return jumpCode.get(jump);
    }

    private void initialize() {
        initializeDest();
        initializeComp();
        initializeJump();
    }

    private void initializeDest() {
        destCode.put("", "000");
        destCode.put("M", "001");
        destCode.put("D", "010");
        destCode.put("MD", "011");
        destCode.put("A", "100");
        destCode.put("AM", "101");
        destCode.put("AD", "110");
        destCode.put("AMD", "111");
    }

    private void initializeComp() {
        // when a = 0, always starts with 0
        compCode.put("0", "0101010");
        compCode.put("1", "0111111");
        compCode.put("-1", "0101010");
        compCode.put("D", "0001100");
        compCode.put("A", "0110000");
        compCode.put("!D", "0001101");
        compCode.put("!A", "0110001");
        compCode.put("-D", "0001111");
        compCode.put("-A", "0110011");
        compCode.put("D+1", "0011111");
        compCode.put("A+1", "0110111");
        compCode.put("D-1", "0001110");
        compCode.put("A-1", "0110010");
        compCode.put("D+A", "0000010");
        compCode.put("D-A", "0010011");
        compCode.put("A-D", "0000111");
        compCode.put("D&A", "0000000");
        compCode.put("D|A", "0010101");

        // when a = 1, always starts with 1
        compCode.put("M", "1110000");
        compCode.put("!M", "1110001");
        compCode.put("-M", "1110011");
        compCode.put("M+1", "1110111");
        compCode.put("M-1", "1110010");
        compCode.put("D+M", "1000010");
        compCode.put("D-M", "1010011");
        compCode.put("M-D", "1000111");
        compCode.put("D&M", "1000000");
        compCode.put("D|M", "1010101");
    }

    private void initializeJump() {
        jumpCode.put("", "000");
        jumpCode.put("JGT", "001");
        jumpCode.put("JEQ", "010");
        jumpCode.put("JGE", "011");
        jumpCode.put("JLT", "100");
        jumpCode.put("JNE", "101");
        jumpCode.put("JLE", "110");
        jumpCode.put("JMP", "111");
    }

}
