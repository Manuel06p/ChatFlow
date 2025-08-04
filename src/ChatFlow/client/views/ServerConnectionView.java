package ChatFlow.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerConnectionView extends JFrame {

    private JTextField ipAddressField;
    private JTextField portField;
    private JButton connectButton;

    public ServerConnectionView() {
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 1, 5, 5));

        // IP Address Field
        JLabel ipAddressLabel = new JLabel("Enter Server IP Address:");
        ipAddressField = new JTextField();
        inputPanel.add(ipAddressLabel);
        inputPanel.add(ipAddressField);

        // Port Field
        JLabel portLabel = new JLabel("Enter Server Port:");
        portField = new JTextField();
        inputPanel.add(portLabel);
        inputPanel.add(portField);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // Connect Button
        connectButton = new JButton("Connect");
        mainPanel.add(connectButton, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE); // To intercept window closing
        setResizable(false);
        setTitle("ChatFlow - Server Connection");
        setSize(300, 200);
        setLocationRelativeTo(null);

    }


    public void addConnectButtonListener(java.awt.event.ActionListener listener) {
        connectButton.addActionListener(listener);
    }

    public String getIpAddress() {
        return ipAddressField.getText();
    }

    public String getPort() {
        return portField.getText();
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
