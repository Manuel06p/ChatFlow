package ChatFlow.shared.utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class ObjectEncryption {


    // Method to encrypt the object in chunks using RSA
    public static <T> String encryptObject(Crypto crypto, T object, byte[] publicKey) throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        byte[] serializedObject = ObjectSerialization.serialize(object);
        List<byte[]> encryptedChunks = new ArrayList<>();

        // Split the serialized object into chunks, because RSA cannot encrypt chunks larger than RSA_MAX_DATA_SIZE
        for (int i = 0; i < serializedObject.length; i += Crypto.RSA_MAX_DATA_SIZE) {
            // Extract the chunk of data to encrypt, it needs the object to split in chunk, the starting point (i), and the end point, which is the minimum between the max data size + starting point and the length of the objcet
            byte[] chunk = Arrays.copyOfRange(serializedObject, i, Math.min(i + Crypto.RSA_MAX_DATA_SIZE, serializedObject.length));

            byte[] encryptedChunk = crypto.encrypt(chunk, publicKey);
            encryptedChunks.add(encryptedChunk);
        }

        // That combines the chunks in a unique stream, that will be later converted to a Base64 String
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] chunk : encryptedChunks) {
            outputStream.write(chunk);
        }
        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    public static <T> T decryptObject(Crypto crypto, String encryptedObject, byte[] privateKey, Class<T> classType) throws IOException, ClassNotFoundException, InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // That decode the String into a byte array and defines the input encrypted stream and the output decrypted stream
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedObject);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedBytes);
        ByteArrayOutputStream decryptedOutputStream = new ByteArrayOutputStream();

        // That decrypts each chunk and att it to the decrypted stream
        byte[] buffer = new byte[Crypto.RSA_BLOCK_SIZE];  // Max size for RSA decryption (128 bytes for 1024-bit key)
        int bytesRead;
        // The while condition updates the value of read bytes and check if ti is not -1
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byte[] chunk = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunk, 0, bytesRead);

            // Decrypt this chunk
            byte[] decryptedChunk = crypto.decrypt(chunk, privateKey);
            decryptedOutputStream.write(decryptedChunk);
        }

        // After decryption, it converts the decrypted bytes back into an object
        byte[] decryptedBytes = decryptedOutputStream.toByteArray();
        return ObjectSerialization.deserialize(decryptedBytes, classType);
    }

}
