package ChatFlow.server;

import ChatFlow.shared.models.ChatModel;
import ChatFlow.shared.models.MessageModel;
import ChatFlow.shared.models.UserModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ChatFlow.server.ServerMain.log;

public class ServerBot implements PropertyChangeListener {
    private final ChatModel chatModel;
    private final UserModel userModel;
    private final ExecutorService executorService;

    CopyOnWriteArraySet<String> usedUsernames;

    ServerBot(ChatModel chatModel, CopyOnWriteArraySet<String> usedUsernames) {
        this.chatModel = chatModel;
        this.userModel = new UserModel("BOT");
        this.usedUsernames = usedUsernames;
        this.usedUsernames.add(userModel.getUsername());
        this.executorService = Executors.newSingleThreadExecutor();
        chatModel.addPropertyChangeListener(this);
    }

    private void receivedMessage(MessageModel receivedMessage) {
        String receivedMessageText = receivedMessage.getMessage();
        if (receivedMessageText.charAt(0) == '/') {
            String botMessage;
            if (receivedMessageText.contains("/date")) {
                botMessage = "Date is: " + new Date().toString();

                log("BOT", "Bot received date command");
            } else if (receivedMessageText.contains("/random")) {
                int random = (int) ((Math.random() * 100) + 1);
                botMessage = "Random number is " + random;

                log("BOT", "Bot received random number command");
            } else if (receivedMessageText.contains("/whoami")) {
                botMessage = "You are: " + receivedMessage.getSenderName();

                log("BOT", "Bot received whoami command");
            } else if (receivedMessageText.contains("/users")) {
                botMessage = "Connected users are:";
                for (String username : usedUsernames) {
                    if (!username.equals("BOT")) {
                        botMessage += "\n" + username;
                    }
                }

                log("BOT", "Bot received users command");
            } else if (receivedMessageText.contains("/coin")) {
                if (Math.random() < 0.5) {
                    botMessage = "The coin is heads";
                } else {
                    botMessage = "The coin is tails";
                }

                log("BOT", "Bot received coin command");
            } else if (receivedMessageText.contains("/size")) {
                botMessage = "Chat size: " + chatModel.getSize();

                log("BOT", "Bot received size command");
            }else if (receivedMessageText.contains("/help")) {
                botMessage = "Available commands:\n" +
                        "/date - Get the current date and time\n" +
                        "/random - Generate a random number (1-100)\n" +
                        "/whoami - Find out your username\n" +
                        "/users - Get a list of connected users\n" +
                        "/coin - Flip a coin (heads or tails)\n" +
                        "/size - Get the chat size\n" +
                        "/help - Show this help message";
                log("BOT", "Bot received help command");
            }
            else {
                botMessage = "Invalid command!";
                log("BOT", "Bot received invalid command");
            }

            chatModel.addMessage(new MessageModel(botMessage, userModel));

        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("message")) {

            executorService.submit(() -> {

                receivedMessage((MessageModel) evt.getNewValue());
            });
        }
    }
}
