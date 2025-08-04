package ChatFlow.shared.models;

import java.io.Serializable;
import java.util.Date;

public class MessageModel implements Serializable {
    String message;
    UserModel sender;
    Date send_date;

    public MessageModel(String message) {
        this.message = message;
        this.send_date = new Date();
    }

    public MessageModel(String message, UserModel sender) {
        this.message = message;
        this.sender = sender;
        this.send_date = new Date();
    }

    public String getTitle() {
        return sender.getUsername() + " - " + send_date;
    }

    public String getMessage() {
        return message;
    }

    public void setSender(UserModel sender) {
        this.sender = sender;
    }

    public String getSenderName() {
        return sender.getUsername();
    }
}
