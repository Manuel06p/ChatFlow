package ChatFlow.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;

public class LoginView extends JFrame {

    private JTextField usernameField;
    private JButton loginButton;

    public LoginView() {
        initComponents();
    }

    private void initComponents() {
        //
        // MAIN PANEL
        //
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        // Username Label and TextField
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 1, 5, 5));

        JLabel usernameLabel = new JLabel("Enter your username:");
        usernameField = new JTextField();

        inputPanel.add(usernameLabel);
        inputPanel.add(usernameField);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // Login Button
        loginButton = new JButton("Login");
        mainPanel.add(loginButton, BorderLayout.SOUTH);

        //
        // OPTIONS
        //
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setTitle("ChatFlow - Login");
        setSize(300, 150);
        setLocationRelativeTo(null);
    }

    public void addLoginButtonListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public void disableLoginButton() {
        loginButton.setEnabled(false);
    }

    public void showUserExistsWarning() {
        JOptionPane.showMessageDialog(
                this,
                "The username is already taken. Please choose a different one.",
                "User Already Exists",
                JOptionPane.WARNING_MESSAGE
        );
        loginButton.setEnabled(true);
    }

    public void addWindowCloseListener(WindowAdapter windowAdapter) {
        this.addWindowListener(windowAdapter);
    }
}
