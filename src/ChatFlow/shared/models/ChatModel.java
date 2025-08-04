package ChatFlow.shared.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.LinkedList;

public class ChatModel implements Serializable {
    LinkedList<MessageModel> messages;
    private transient PropertyChangeSupport propertyChangeSupport;

    public ChatModel() {
        this.messages = new LinkedList<>();
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public synchronized LinkedList<MessageModel> getMessages() {
        return messages;
    }

    public synchronized void setMessages(LinkedList<MessageModel> messages) {
        this.messages = messages;
        this.propertyChangeSupport.firePropertyChange("messages", null, this.messages);
    }

    public synchronized void addMessage(MessageModel message) {
        messages.addFirst(message);
        propertyChangeSupport.firePropertyChange("message", null, message);
    }


    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public int getSize() {
        return messages.size();
    }

}
