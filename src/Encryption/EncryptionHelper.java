package Encryption;

import java.security.SecureRandom;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class EncryptionHelper {

    private static final String BOUNCY_CASTLE_PROVIDER = "BC";	
    private Cipher cipher;

    // Key for AES (you should implement secure key management for production use)
    private byte[] keyBytes = new byte[] {
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
    };
    private SecretKey key = new SecretKeySpec(keyBytes, "AES");

    // Constructor to initialize the Bouncy Castle provider and Cipher instance
    public EncryptionHelper() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", BOUNCY_CASTLE_PROVIDER); // AES with CBC and padding
    }

    // Encrypt method: encrypts the plain text using AES/CBC/PKCS5Padding
    public byte[] encrypt(byte[] plainText, byte[] initializationVector) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(initializationVector));
        return cipher.doFinal(plainText);
    }

    // Decrypt method: decrypts the ciphertext back to the original text
    public byte[] decrypt(byte[] cipherText, byte[] initializationVector) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(initializationVector));
        return cipher.doFinal(cipherText);
    }

    // Method to generate a random Initialization Vector (IV)
    public byte[] generateIV() {
        byte[] iv = new byte[16]; // AES block size
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }
}
