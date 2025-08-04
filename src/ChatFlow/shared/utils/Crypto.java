package ChatFlow.shared.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class Crypto {
    private final KeyPairGenerator keyGen;
    private final KeyFactory keyFactory;
    private final Cipher cipher;
    private static final int RSA_KEY_SIZE = 1024; // RSA key size in bits
    public static final int RSA_BLOCK_SIZE = RSA_KEY_SIZE / 8; //Encrypted data size including padding
    public static final int RSA_MAX_DATA_SIZE = RSA_BLOCK_SIZE - 11; // Max data size for RSA encryption (128 bytes - padding, which is 11 for RSA/ECB/PKCS1Padding)

    public Crypto(int keySize) throws NoSuchPaddingException, NoSuchAlgorithmException {
        keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(keySize);
        keyFactory = KeyFactory.getInstance("RSA");
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    }

    public KeyPair generateKeyPair() {
        return keyGen.generateKeyPair();
    }

    public byte[] encrypt(byte[] data, byte[] publicKey) throws InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
        cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(publicKeySpec));
        return cipher.doFinal(data);
    }

    public byte[] decrypt(byte[] encryptedData, byte[] privateKey) throws InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        PKCS8EncodedKeySpec keySpecPr = new PKCS8EncodedKeySpec(privateKey);
        cipher.init(Cipher.DECRYPT_MODE, keyFactory.generatePrivate(keySpecPr));
        return cipher.doFinal(encryptedData);
    }
}
