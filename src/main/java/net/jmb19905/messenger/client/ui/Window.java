package net.jmb19905.messenger.client.ui;

import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The Main Window of the Client
 */
public class Window extends JFrame {

    private final JTextArea area;
    private final DefaultListModel<String> model;
    private final JTextField inputField;
    private final JButton ellipsisButton;
    private final JButton imageButton;

    public SettingsWindow settingsWindow;

    public static boolean closeRequested = false;

    public Window() {
        setTitle("Encrypted Messenger");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 750));
        setIconImage(Util.getImageResource("icon.png"));
        setLayout(new BorderLayout());

        settingsWindow = new SettingsWindow();

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT) {
            private final int location = 250;

            {
                setDividerLocation(location);
            }

            @Override
            public int getDividerLocation() {
                return location;
            }

            @Override
            public int getLastDividerLocation() {
                return location;
            }
        };
        add(pane, BorderLayout.CENTER);

        JPanel chatChooser = new JPanel(new GridBagLayout());
        pane.setLeftComponent(chatChooser);

        CardLayout conversationRootLayout = new CardLayout();
        JPanel conversationRoot = new JPanel(conversationRootLayout);
        pane.setRightComponent(conversationRoot);

        JPanel blank = new JPanel();
        conversationRoot.add(blank, "blank");

        JPanel conversation = new JPanel(new GridBagLayout());
        conversationRoot.add(conversation, "conversation");

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;

        Font font = new Font("segoeui", Font.PLAIN, 18);

        area = new JTextArea();
        area.setFont(font);
        area.setEditable(false);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 24;
        constraints.weightx = 1;
        constraints.weighty = 1;
        conversation.add(area, constraints);

        model = new DefaultListModel<>();
        JList<String> connectedUsers = new JList<>(model);
        connectedUsers.setPreferredSize(new Dimension(250, 750));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        connectedUsers.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        connectedUsers.addListSelectionListener(e -> conversationRootLayout.show(conversationRoot, "conversation"));
        chatChooser.add(connectedUsers, constraints);

        JButton startConversation = new JButton("Start Conversation");
        startConversation.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(null, "Please input the username of the conversation partner: ", "Start Conversation", JOptionPane.PLAIN_MESSAGE);
            if (username.length() < 3 || username == null) {
                JOptionPane.showMessageDialog(null, "Invalid Username (" + username + ") (has to be at least 3 characters)", "", JOptionPane.ERROR_MESSAGE);
            } else {
                ByteThrowClient.messagingClient.connectWithOtherUser(username);
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        chatChooser.add(startConversation, constraints);

        inputField = new JTextField();
        inputField.setFont(font);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String message = inputField.getText();
                    if (ByteThrowClient.messagingClient.sendToOtherUser(connectedUsers.getSelectedValue(), message)) {
                        appendLine("<" + ByteThrowClient.getUsername() + "> " + message);
                    } else {
                        JOptionPane.showMessageDialog(null, "Error processing message", "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                    inputField.setText("");
                }
            }
        });
        constraints.gridx = 0;
        constraints.gridy = 25;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 0;
        constraints.weighty = 0;
        conversation.add(inputField, constraints);

        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);

        ellipsisButton = new JButton();
        ellipsisButton.setIcon(new ImageIcon(Util.getImageResource("ellipsis" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        ellipsisButton.addActionListener((e) -> settingsWindow.setVisible(true));

        toolBar.add(ellipsisButton);

        toolBar.add(Box.createVerticalGlue());

        imageButton = new JButton();
        imageButton.setIcon(new ImageIcon(Util.getImageResource("image" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));

        toolBar.add(imageButton);
        toolBar.setFloatable(false);

        add(toolBar, BorderLayout.EAST);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeRequested = true;
                ByteThrowClient.messagingClient.stop(0);
            }
        });

        pack();

    }

    /**
     * Append a String to the JTextArea
     * @param s the String
     */
    public void append(String s) {
        area.append(s);
    }

    /**
     * Append a Line to the JTextArea
     * @param s the Line (without \n)
     */
    public void appendLine(String s) {
        append(s + "\n");
    }

    /**
     * Adds a Username to the Connected Users JList
     * @param user the username
     */
    public void addConnectedUser(String user) {
        model.addElement(user);
    }

    /**
     * Removes a Username from the Connected Users JList
     * @param user the username
     */
    public void removeConnectedUser(String user) {
        model.removeElement(user);
    }

    @Override
    public void repaint() {
        super.revalidate();
        ellipsisButton.setIcon(new ImageIcon(Util.getImageResource("ellipsis" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        imageButton.setIcon(new ImageIcon(Util.getImageResource("image" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
    }
}
