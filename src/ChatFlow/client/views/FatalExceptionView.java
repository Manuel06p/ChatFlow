package ChatFlow.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FatalExceptionView extends JDialog {

    public FatalExceptionView(Frame parent, String errorMessage) {
        super(parent, "Fatal Error", true); // Modal dialog

        // Create components
        JLabel messageLabel = new JLabel("<html><p style='width: 300px;'>" + errorMessage + "</p></html>");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> dispose());

        // Layout setup
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        pack();
        setLocationRelativeTo(parent); // Center the dialog relative to the parent
        setAlwaysOnTop(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static void showErrorDialog(Frame parent, String errorMessage) {
        FatalExceptionView dialog = new FatalExceptionView(parent, errorMessage);
        dialog.setVisible(true);
    }
}
