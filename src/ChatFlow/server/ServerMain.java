package ChatFlow.server;

import ChatFlow.shared.models.ChatModel;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import java.util.concurrent.CopyOnWriteArraySet;


public class ServerMain {

    public static void main(String[] args) {
        final int PORT = 8080;
        try {
            log("Welcome to ChatFlow Server!\n");
            ChatModel chatModel = new ChatModel();
            CopyOnWriteArraySet<String> usedUsernames = new CopyOnWriteArraySet<>();

            ServerSocket serverSocket = new ServerSocket(PORT);

            log("Server", "Server listening on " + InetAddress.getLocalHost().toString() + ":" + 8080);


            final ServerBot serverBot = new ServerBot(chatModel, usedUsernames);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, chatModel, usedUsernames);
            }



        } catch (IOException e) {

            log("Server", "Exception regarding the socket creation: " + e.getMessage());

        }
    }

    public static void log(String message) {
        System.out.println(new Date() + ": " + message);
    }

    public static void log(String actor, String message) {
        System.out.println(new Date() + " - " + actor + ": " + message);
    }

    public static void log(String actor, String alias, String message) {
        System.out.println(new Date() + " - " + actor + "/" + alias + ": " + message);
    }
}
