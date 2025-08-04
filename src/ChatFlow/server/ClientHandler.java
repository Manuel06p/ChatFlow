package ChatFlow.server;

import ChatFlow.shared.models.ChatModel;
import ChatFlow.shared.models.MessageModel;
import ChatFlow.shared.models.UserModel;
import ChatFlow.shared.utils.Crypto;
import ChatFlow.shared.utils.ObjectEncryption;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ChatFlow.server.ServerMain.log;


public class ClientHandler implements PropertyChangeListener {

    Socket clientSocket;

    private ObjectInputStream clientOIS;
    private ObjectOutputStream clientOOS;
    private ChatModel chatModel;

    private ExecutorService senderExecutorService;
    private ExecutorService receiverExecutorService;
    private String clientID;
    private UserModel userModel = null;
    private CopyOnWriteArraySet<String> usedUsernames;

    private Crypto crypto;
    private byte[] serverPrivateKey;
    private byte[] serverPublicKey;
    private byte[] clientPublicKey;

    ClientHandler(Socket clientSocket, ChatModel chatModel, CopyOnWriteArraySet<String> usedUsernames) {
        try {
            this.chatModel = chatModel;
            this.clientSocket = clientSocket;
            clientID = clientSocket.toString();
            this.usedUsernames = usedUsernames;
            log(clientID, "Client connected");

            try {
                this.clientOOS = new ObjectOutputStream(clientSocket.getOutputStream());
                this.clientOIS = new ObjectInputStream(clientSocket.getInputStream());
            } catch (IOException ex) {
                log(clientID, "Exception during creation of input and output stream, regarding connection with the client: " + ex.getMessage());
                throw new Exception();
            }

            this.senderExecutorService = Executors.newSingleThreadExecutor();
            this.receiverExecutorService = Executors.newSingleThreadExecutor();

            // RSA Key Pair Generation
            try {
                crypto = new Crypto(1024);
                KeyPair keyPair = crypto.generateKeyPair();
                this.serverPrivateKey = keyPair.getPrivate().getEncoded();
                serverPublicKey = keyPair.getPublic().getEncoded();
            } catch (NoSuchPaddingException | NoSuchAlgorithmException ex) {
                log(clientID, "Exception during the creation of the Crypto object, regarding encryption: " + ex.getMessage());
                throw new Exception();
            }


            // Send server's public key to client
            try {
                clientOOS.writeInt(serverPublicKey.length);
                clientOOS.write(serverPublicKey);
                clientOOS.flush();
                log(clientID, "Sent public key to client");
            } catch (IOException ex) {
                log(clientID, "Exception while sending server's public key, regarding connection with the client: " + ex.getMessage());
                throw new Exception();
            }

            // Receive client's public key
            try {
                int clientKeyLength = clientOIS.readInt();
                clientPublicKey = new byte[clientKeyLength];
                clientOIS.readFully(clientPublicKey);
                log(clientID, "Received public key from client");
            } catch (IOException ex) {
                log(clientID, "Exception while receiving client's public key, regarding connection with the client: " + ex.getMessage());
                throw new Exception();
            }


            //Receive Username
            try {
                while (userModel == null) {
                    String encryptedMessage = (String) clientOIS.readObject();
                    if (encryptedMessage != null) {
                        String username = ObjectEncryption.decryptObject(crypto, encryptedMessage, serverPrivateKey, String.class);
                        if (usedUsernames.add(username)) {
                            userModel = new UserModel(username);
                            log(clientID, "Created user " + username + " for client");
                        } else {
                            log(clientID, "User " + username + " already exists");
                        }

                    }

                    //Send UserModel
                    String encryptedUserModel = ObjectEncryption.encryptObject(crypto, userModel, clientPublicKey);
                    clientOOS.writeObject(encryptedUserModel);
                    clientOOS.flush();
                    log(clientID, "Sent user model to client");
                }
            } catch (IOException ex) {
                log(clientID, "Exception during the UserModel's communication, regarding connection with the client: " + ex.getMessage());
                throw new Exception();
            } catch (IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException |
                     InvalidKeyException | ClassNotFoundException ex) {
                log(clientID, "Exception during the UserModel's communication, regarding encryption: " + ex.getMessage());
                throw new Exception();
            }


            // Send current chat to client
            try {
                String encryptedChat = ObjectEncryption.encryptObject(crypto, chatModel, clientPublicKey);
                clientOOS.writeObject(encryptedChat);
                clientOOS.flush();
                log(clientID, userModel.getUsername(), "Sent chat to client");
            } catch (IOException ex) {
                log(clientID, userModel.getUsername(), "Exception while sending the chat, regarding connection with the client: " + ex.getMessage());
                throw new Exception();
            } catch (IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException |
                     InvalidKeyException ex) {
                log(clientID, userModel.getUsername(), "Exception while sending the chat, regarding encryption: " + ex.getMessage());
                throw new Exception();
            }


            chatModel.addPropertyChangeListener(this);

            receiverExecutorService.execute(() -> {
                try {
                    while (true) {
                        String encryptedMessage = (String) clientOIS.readObject();
                        if (encryptedMessage != null) {
                            MessageModel message = ObjectEncryption.decryptObject(crypto, encryptedMessage, serverPrivateKey, MessageModel.class);
                            message.setSender(userModel);
                            chatModel.addMessage(message);
                            log(clientID, userModel.getUsername(), "Received message from client");
                        }
                    }
                } catch (IOException ex) {
                    log(clientID, userModel.getUsername(), "Exception while receiving message, regarding connection with the client: " + ex.getMessage());
                    close();
                } catch (IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException |
                         InvalidKeyException | ClassNotFoundException ex) {
                    log(clientID, userModel.getUsername(), "Exception while receiving message, regarding encryption: " + ex.getMessage());
                    close();
                }
            });
        } catch (Exception e) {
            close();
        }
    }

    public void sendMessage(MessageModel message) {
        try {
            String encryptedMessage = ObjectEncryption.encryptObject(crypto, message, clientPublicKey);
            clientOOS.writeObject(encryptedMessage);
            clientOOS.flush();
            log(clientID, userModel.getUsername(), "Sent message to client");
        } catch (IOException ex) {
            log(clientID, userModel.getUsername(), "Exception while sending message, regarding connection with the client: " + ex.getMessage());
            close();
        } catch (IllegalBlockSizeException | InvalidKeySpecException | BadPaddingException | InvalidKeyException ex) {
            log(clientID, userModel.getUsername(), "Exception while sending message, regarding encryption: " + ex.getMessage());
            close();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("message")) {
            senderExecutorService.submit(() -> {
                sendMessage((MessageModel) evt.getNewValue());
            });
        }
    }

    private void close() {
        senderExecutorService.shutdown();
        receiverExecutorService.shutdown();

        chatModel.removePropertyChangeListener(this);

        if (userModel != null) {
            usedUsernames.remove(userModel.getUsername());
            log(clientID, userModel.getUsername(), "Client disconnected");
        } else {
            log(clientID, "Client disconnected");
        }


    }
}
