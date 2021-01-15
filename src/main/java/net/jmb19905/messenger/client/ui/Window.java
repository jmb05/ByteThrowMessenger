package net.jmb19905.messenger.client.ui;

import net.jmb19905.messenger.client.CommandParser;
import net.jmb19905.messenger.client.EncryptedMessenger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Window extends JFrame {

    private final JTextArea area;
    private final DefaultListModel<String> model;
    private final JTextField inputField;
    private final JButton ellipsisButton;

    public SettingsWindow settingsWindow;

    public static boolean closeRequested = false;

    public Window(){
        setTitle("Encrypted Messenger");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 750));
        setIconImage(new ImageIcon("src/main/resources/icon.png").getImage());
        setLayout(new BorderLayout());

        settingsWindow = new SettingsWindow();

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT){
            private final int location = 250;
            {
                setDividerLocation( location );
            }
            @Override
            public int getDividerLocation() {
                return location ;
            }
            @Override
            public int getLastDividerLocation() {
                return location ;
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
            if(username == null){

            }else if(username.length() < 3){
                JOptionPane.showMessageDialog(null, "Invalid Username (has to be at least 3 characters)", "", JOptionPane.ERROR_MESSAGE);
            }else{
                EncryptedMessenger.messagingClient.connectWithOtherUser(username);
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
            if(e.getKeyCode() == KeyEvent.VK_ENTER){
                String command = inputField.getText();
                inputField.setText("");
                CommandParser.parseCommand(command, EncryptedMessenger.messagingClient);
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
        ellipsisButton.setIcon(new ImageIcon("src/main/resources/ellipsis" + (!SettingsWindow.isDark() ? "_dark" : "") + ".png"));
        ellipsisButton.addActionListener((e) -> {
            settingsWindow.setVisible(true);
        });

        toolBar.add(ellipsisButton);
        toolBar.setFloatable(false);

        add(toolBar, BorderLayout.EAST);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeRequested = true;
                EncryptedMessenger.messagingClient.stop(0);
            }
        });

        pack();

    }

    public void append(String s){
        area.append(s);
    }

    public void appendLine(String s){
        area.append(s + "\n");
    }

    public void addConnectedUser(String user){
        model.addElement(user);
    }

    public void removeConnectedUser(String user){
        model.removeElement(user);
    }

    @Override
    public void repaint() {
        super.revalidate();
        ellipsisButton.setIcon(new ImageIcon("src/main/resources/ellipsis" + (!SettingsWindow.isDark() ? "_dark" : "") + ".png"));
    }
}
