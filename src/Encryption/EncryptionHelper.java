package Encryption;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
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
        // Initialize the cipher for encryption with the provided IV
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(initializationVector));
        
        // Encrypt the data
        byte[] encryptedData = cipher.doFinal(plainText);
        
        // Combine IV and encrypted data
        byte[] combined = new byte[initializationVector.length + encryptedData.length];
        System.arraycopy(initializationVector, 0, combined, 0, initializationVector.length);
        System.arraycopy(encryptedData, 0, combined, initializationVector.length, encryptedData.length);
        
        return combined; // Return combined IV and encrypted data
    }

    // Decrypt method
    public byte[] decrypt(byte[] combined) throws Exception {
        // Extract the IV
        byte[] initializationVector = Arrays.copyOfRange(combined, 0, 16);
        
        // Extract the encrypted data
        byte[] encryptedData = Arrays.copyOfRange(combined, 16, combined.length);
        
        // Initialize the cipher for decryption using the extracted IV
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(initializationVector));

        // Decrypt the data
        return cipher.doFinal(encryptedData);
    }

    public char[] decryptToCharArray(byte[] encryptedData, byte[] iv) throws Exception {
        // Ensure the cipher and key are properly set up for decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParams = new IvParameterSpec(iv); // Create IV parameter spec
        cipher.init(Cipher.DECRYPT_MODE, key, ivParams); // Initialize the cipher in decrypt mode

        // Decrypt the data
        byte[] decryptedData = cipher.doFinal(encryptedData); // Decrypt using the cipher
        return new String(decryptedData, StandardCharsets.UTF_8).toCharArray(); // Convert decrypted bytes to char array
    }

    // Method to generate a random Initialization Vector (IV)
    public byte[] generateIV() {
        byte[] iv = new byte[16]; // AES block size
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);
        return iv;
    }
}
