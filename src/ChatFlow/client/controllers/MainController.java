package ChatFlow.client.controllers;

import ChatFlow.shared.utils.Crypto;
import ChatFlow.shared.models.UserModel;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

public class MainController {

    private Crypto crypto;
    private byte[] serverPublicKey; // The server's public key for encryption
    private byte[] clientPrivateKey; // The client's private key for decryption
    private byte[] clientPublicKey;

    private Socket serverSocket;
    private ObjectOutputStream serverOOS;
    private ObjectInputStream serverOIS;

    public MainController() {
        // Start Socket Communication
        ServerConnectionController serverConnectionController = new ServerConnectionController();
        this.serverSocket = serverConnectionController.getServerSocket();
        this.serverOOS = serverConnectionController.getServerOOS();
        this.serverOIS = serverConnectionController.getServerOIS();


        try {
            // Initialize Client Key Pair
            this.crypto = new Crypto(1024);
            KeyPair keyPair = crypto.generateKeyPair();
            this.clientPrivateKey = keyPair.getPrivate().getEncoded();
            this.clientPublicKey = keyPair.getPublic().getEncoded();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException ex) {
            log("Exception during the creation of the Crypto object, regarding encryption: " + ex.getMessage());
            closeApp();
        }

        try {
            // Receive server's public key
            int serverKeyLength = serverOIS.readInt();
            serverPublicKey = new byte[serverKeyLength];
            serverOIS.readFully(serverPublicKey);
        } catch (IOException ex) {
            log("Exception while receiving server's public key, regarding connection with the server: " + ex.getMessage());
            closeApp();
        }

        try {
            // Send client's public key to the server
            serverOOS.writeInt(clientPublicKey.length);
            serverOOS.write(clientPublicKey);
            serverOOS.flush();
        } catch (IOException ex) {
            log("Exception while sending client's public key, regarding connection with the server: " + ex.getMessage());
            closeApp();
        }

        UserModel userModel;

        LoginController loginController = new LoginController(this::closeApp, serverOOS, serverOIS, crypto, serverPublicKey, clientPrivateKey);
        userModel = loginController.getUserModel();
        new ChatController(this::closeApp, userModel, serverOOS, serverOIS, crypto, serverPublicKey, clientPrivateKey);
    }

    private void closeApp() {
        try {
            serverOOS.close();
            serverOIS.close();
            serverSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.exit(0);
    }

    public static void log(String message) {
        System.out.println(new Date() + ": " + message);
    }
}
