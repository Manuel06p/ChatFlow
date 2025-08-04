package ChatFlow.client.views;

import ChatFlow.shared.models.MessageModel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.util.LinkedList;

public class ChatView extends JFrame {

    private JTextArea sendMessageField;
    private JScrollPane sendMessageFieldScrollPane;
    private JButton sendMessageButton;

    private JPanel messagesPanel;

    private JPanel bodyPanel;
    private JScrollPane bodyScrollPane;

    private JPanel bottomPanel;

    public ChatView(String username) {
        initComponents(username);
    }

    private void initComponents(String username) {
        //
        // BODY PANEL
        //
        bodyPanel = new JPanel();
        bodyPanel.setLayout(new BorderLayout());
        bodyScrollPane = new JScrollPane(bodyPanel);
        bodyScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(bodyScrollPane, BorderLayout.CENTER);

        //messagesPanel
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(messagesPanel, BorderLayout.NORTH);


        //
        //BOTTOM PANEL
        //
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        add(bottomPanel, BorderLayout.SOUTH);

        //sendMessageField
        sendMessageField = new JTextArea();
        sendMessageField.setRows(2);
        sendMessageField.setLineWrap(true);
        sendMessageField.setWrapStyleWord(true);
        sendMessageField.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        sendMessageField.setBackground(Color.decode("#FFFFFF"));

        sendMessageFieldScrollPane = new JScrollPane(sendMessageField);
        sendMessageFieldScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sendMessageFieldScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sendMessageFieldScrollPane.setBorder(null);
        bottomPanel.add(sendMessageFieldScrollPane, BorderLayout.CENTER);

        //sendMessageButton
        sendMessageButton = new JButton("Send");
        bottomPanel.add(sendMessageButton, BorderLayout.EAST);
        bottomPanel.setBorder(new LineBorder(Color.DARK_GRAY, 2));

        //
        //OPTIONS
        //
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setTitle("ChatFlow - " + username);
        setSize(400, 500);
        setLocationRelativeTo(null);
    }

    public void updateMessages(LinkedList<MessageModel> messages) {
        messagesPanel.removeAll();

        addMessagesPanels(messages);

        revalidate();
        repaint();
    }



    public void addMessagesPanels(LinkedList<MessageModel> messages) {
        messagesPanel.removeAll();

        for (MessageModel message : messages) {
            MessagePanel messagePanel = new MessagePanel(message);

            messagesPanel.add(messagePanel);
        }


    }

    public void addMessage(MessageModel message) {
        messagesPanel.add(new MessagePanel(message), 0);

        revalidate();
        repaint();
    }

    public void clearSendMessage() {
        sendMessageField.setText("");
    }

    public void addWindowCloseListener(WindowAdapter windowAdapter) {
        this.addWindowListener(windowAdapter);
    }

private class MessagePanel extends JPanel {
    private final JLabel messageLabel;
    private final JTextArea messageTextArea;

    public MessagePanel(MessageModel messageModel) {
        setLayout(new BorderLayout());

        messageLabel = new JLabel(messageModel.getTitle());
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        messageTextArea = new JTextArea(messageModel.getMessage());
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        messageTextArea.setEditable(false);
        messageTextArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 12, 8));
        messageTextArea.setBackground(Color.decode("#FFF3E0"));

        add(messageLabel, BorderLayout.NORTH);
        add(messageTextArea, BorderLayout.CENTER);
    }
}


    //Listener
    public void addSendMessageButtonListener(ActionListener listener) {
        sendMessageButton.addActionListener(listener);
    }

    public String getSendMessage() {
        return sendMessageField.getText();
    }

}
