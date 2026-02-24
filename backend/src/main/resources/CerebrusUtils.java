import java.security.SecureRandom;

public final class CerebrusUtils {

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int CODE_LENGTH = 7;
    private static final SecureRandom RNG = new SecureRandom();

    private CerebrusUtils() {}

    public static String generateUniqueCode() {
        char[] code = new char[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            code[i] = ALPHABET[RNG.nextInt(ALPHABET.length)];
        }
        return new String(code);
    }
}
