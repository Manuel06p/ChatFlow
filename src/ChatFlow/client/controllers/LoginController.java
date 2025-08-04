package ChatFlow.client.controllers;

import ChatFlow.client.views.*;
import ChatFlow.shared.utils.*;
import ChatFlow.shared.models.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ChatFlow.client.controllers.MainController.log;

public class LoginController {

    private UserModel userModel;
    private LoginView view;
    private final Runnable closeApp;

    private final ExecutorService viewExecutorService;

    private ObjectOutputStream serverOOS;
    private ObjectInputStream serverOIS;

    private final Crypto crypto;
    private byte[] serverPublicKey; // The server's public key for encryption
    private byte[] clientPrivateKey; // The client's private key for decryption

    private ActionListener loginButtonListener;

    public LoginController(Runnable closeApp, ObjectOutputStream serverOOS, ObjectInputStream serverOIS, Crypto crypto, byte[] serverPublicKey, byte[] clientPrivateKey)  {
        this.closeApp = closeApp;
        this.serverOOS = serverOOS;
        this.serverOIS = serverOIS;
        this.crypto = crypto;
        this.serverPublicKey = serverPublicKey;
        this.clientPrivateKey = clientPrivateKey;

        this.view = new LoginView();

        this.view.addWindowCloseListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeApp();
            }
        });

        // Threads
        viewExecutorService = Executors.newSingleThreadExecutor();

        viewExecutorService.submit(() -> {
            this.view.setVisible(true);
        });

        loginButtonListener = (ActionEvent e) -> {
            String username = this.view.getUsername();
            this.view.disableLoginButton();
            try {
                sendUsername(username);
            } catch (IOException ex) {
                String message = "Exception while sending username, regarding communication with server: " + ex.getMessage();
                fatalException(message);
            }
            catch (IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | InvalidKeyException ex) {
                String message = "Exception while sending username, regarding encryption: " + ex.getMessage();
                fatalException(message);
            }
        };
        view.addLoginButtonListener(loginButtonListener);

        try {
            while (this.userModel == null) {
                String encryptedMessage = (String) serverOIS.readObject();
                if (encryptedMessage != null) {
                    UserModel newUserModel = ObjectEncryption.decryptObject(crypto, encryptedMessage, clientPrivateKey, UserModel.class);
                    if (newUserModel != null) {
                        this.userModel = newUserModel;
                        closeView();
                    } else {
                        this.view.showUserExistsWarning();
                    }
                }
            }
        } catch (IOException ex) {
            String message = "Exception while receiving UserModel, regarding communication with server: " + ex.getMessage();
            fatalException(message);
        } catch (IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException |
                 InvalidKeyException | ClassNotFoundException ex) {
            String message = "Exception while receiving UserModel, regarding encryption: " + ex.getMessage();
            fatalException(message);
        }
    }


    private void sendUsername(String username) throws IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, InvalidKeyException, IOException {
        String encryptedMessage = ObjectEncryption.encryptObject(crypto, username, serverPublicKey);
        serverOOS.writeObject(encryptedMessage);
        serverOOS.flush();
    }

    public UserModel getUserModel() {
        return this.userModel;
    }

    private void closeView() {
        viewExecutorService.shutdownNow();
        view.setVisible(false);
        view.dispose();
    }

    private void closeApp() {
        closeView();
        closeApp.run();
    }

    private void fatalException(String message) {
        log(message);
        closeView();
        FatalExceptionView.showErrorDialog(null, message);
        closeApp.run();
    }
}
