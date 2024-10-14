package Encryption;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class EncryptionUtils {

    private static final int IV_SIZE = 16;  // IV size is 16 bytes (128 bits) for AES

    // Converts byte array to char array (for easier handling of text)
    public static char[] toCharArray(byte[] bytes) {
        CharBuffer charBuffer = Charset.defaultCharset().decode(ByteBuffer.wrap(bytes));
        return Arrays.copyOf(charBuffer.array(), charBuffer.limit());
    }

    // Converts char array back to byte array (for encryption)
    public static byte[] toByteArray(char[] chars) {
        return new String(chars).getBytes(Charset.defaultCharset()); // Convert char array to String and then to byte array
    }

    // Generates an initialization vector (IV) based on the provided text, ensuring consistency for the same text
    public static byte[] getInitializationVector(char[] text) {
        char[] iv = new char[IV_SIZE];
        int textPointer = 0;
        int ivPointer = 0;

        while (ivPointer < IV_SIZE) {
            iv[ivPointer++] = text[textPointer++ % text.length]; // Wrap around text if it's shorter than IV_SIZE
        }
        return toByteArray(iv); // Convert char array IV to byte array
    }

    // Prints char array safely (useful for debugging)
    public static void printCharArray(char[] chars) {
        System.out.println(new String(chars)); // Print char array as a String
    }
}
