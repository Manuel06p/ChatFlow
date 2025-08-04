package ChatFlow.client.controllers;

import ChatFlow.client.views.ChatView;
import ChatFlow.client.views.FatalExceptionView;
import ChatFlow.shared.models.ChatModel;
import ChatFlow.shared.models.MessageModel;
import ChatFlow.shared.models.UserModel;
import ChatFlow.shared.utils.Crypto;
import ChatFlow.shared.utils.ObjectEncryption;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ChatFlow.client.controllers.MainController.log;

public class ChatController implements PropertyChangeListener {

    private ChatModel model = null;
    private final ChatView view;
    private final Runnable closeApp;

    private ObjectOutputStream serverOOS;
    private ObjectInputStream serverOIS;
    private ActionListener sendButtonListener;
    private final ExecutorService viewExecutorService;
    private final ExecutorService senderExecutorService;
    private final ExecutorService receiverExecutorService;
    private final UserModel userModel;

    private final Crypto crypto;
    private byte[] serverPublicKey; // The server's public key for encryption
    private byte[] clientPrivateKey; // The client's private key for decryption

    public ChatController(Runnable closeApp, UserModel userModel, ObjectOutputStream serverOOS, ObjectInputStream serverOIS, Crypto crypto, byte[] serverPublicKey, byte[] clientPrivateKey) {
        this.closeApp = closeApp;
        this.userModel = userModel;
        this.serverOOS = serverOOS;
        this.serverOIS = serverOIS;
        this.crypto = crypto;
        this.serverPublicKey = serverPublicKey;
        this.clientPrivateKey = clientPrivateKey;

        this.view = new ChatView(userModel.getUsername());
        this.view.addWindowCloseListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeApp();
            }
        });

        // Threads
        viewExecutorService = Executors.newSingleThreadExecutor();
        senderExecutorService = Executors.newSingleThreadExecutor();
        receiverExecutorService = Executors.newSingleThreadExecutor();

        viewExecutorService.submit(() -> {
            this.view.setVisible(true);
        });

        model = new ChatModel();
        model.addPropertyChangeListener(this);

        sendButtonListener = (ActionEvent e) -> {
            String messageText = view.getSendMessage();
            if (messageText.length() > 0) {
                senderExecutorService.submit(() -> {
                    try {
                        sendMessage(messageText);
                    } catch (IOException ex) {
                        String message = "Exception while sending message, regarding communication with server: " + ex.getMessage();
                        fatalException(message);
                    }
                    catch (IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | InvalidKeyException ex) {
                        String message = "Exception while sending message, regarding encryption: " + ex.getMessage();
                        fatalException(message);
                    }
                });
            }
            view.clearSendMessage();
        };

        view.addSendMessageButtonListener(sendButtonListener);

        try {
            // Chat initialization
            String encryptedChat = (String) serverOIS.readObject();
            if (encryptedChat != null) {
                ChatModel serverChat = ObjectEncryption.decryptObject(crypto, encryptedChat, clientPrivateKey, ChatModel.class);
                model.setMessages(serverChat.getMessages());
            }
        } catch (IOException ex) {
            String message = "Exception while receiving the chat, regarding communication with server: " + ex.getMessage();
            fatalException(message);
        } catch (IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException |
                 InvalidKeyException | ClassNotFoundException ex) {
            String message = "Exception while receiving the chat, regarding encryption: " + ex.getMessage();
            fatalException(message);
        }

        receiverExecutorService.submit(() -> {
            try {
                receiveMessage();
            } catch (IOException ex) {
                String message = "Exception while receiving message, regarding communication with server: " + ex.getMessage();
                fatalException(message);
            } catch (IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException |
                     InvalidKeyException | ClassNotFoundException ex) {
                String message = "Exception while receiving message, regarding encryption: " + ex.getMessage();
                fatalException(message);
            }
        });
    }


    private void receiveMessage() throws IOException, ClassNotFoundException, IllegalBlockSizeException, InvalidKeySpecException, BadPaddingException, InvalidKeyException {
        while (true) {
            String encryptedMessage = (String) serverOIS.readObject();
            if (encryptedMessage != null) {
                MessageModel message = ObjectEncryption.decryptObject(crypto, encryptedMessage, clientPrivateKey, MessageModel.class);
                model.addMessage(message);
            }
        }
    
    }

    private void sendMessage(String messageText) throws IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeySpecException, InvalidKeyException {
        MessageModel message = new MessageModel(messageText);
        String encryptedMessage = ObjectEncryption.encryptObject(crypto, message, serverPublicKey);
        serverOOS.writeObject(encryptedMessage);
        serverOOS.flush();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("messages")) {
            this.view.updateMessages((LinkedList<MessageModel>) evt.getNewValue());
        } else if (evt.getPropertyName().equals("message")) {
            this.view.addMessage((MessageModel) evt.getNewValue());
        }
    }

    private void closeView() {
        receiverExecutorService.shutdownNow();
        senderExecutorService.shutdownNow();
        viewExecutorService.shutdownNow();
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