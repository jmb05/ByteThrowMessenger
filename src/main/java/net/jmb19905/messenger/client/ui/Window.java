package net.jmb19905.messenger.client.ui;

import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.ClientUtils;
import net.jmb19905.messenger.client.MessagingClient;
import net.jmb19905.messenger.client.ui.conversation.ConversationPane;
import net.jmb19905.messenger.client.ui.conversation.MessageFrame;
import net.jmb19905.messenger.client.ui.settings.AccountSettings;
import net.jmb19905.messenger.client.ui.settings.SettingsWindow;
import net.jmb19905.messenger.client.ui.util.component.HintTextField;
import net.jmb19905.messenger.client.ui.util.component.ImagePanel;
import net.jmb19905.messenger.messages.ImageMessage;
import net.jmb19905.messenger.messages.Message;
import net.jmb19905.messenger.messages.TextMessage;
import net.jmb19905.messenger.util.FileUtility;
import net.jmb19905.messenger.util.FormattedImage;
import net.jmb19905.messenger.util.ImageUtility;
import net.jmb19905.messenger.util.Variables;
import net.jmb19905.messenger.util.logging.BTMLogger;
import org.checkerframework.checker.units.qual.A;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Main Window of the Client
 */
public class Window extends JFrame {

    private final JSplitPane pane;
    public final SettingsWindow settingsWindow;
    public AccountSettings accountSettings;
    private JScrollPane conversationScrollPane;
    private ConversationPane conversationPane;
    private JList<String> connectedUsers;
    private DefaultListModel<String> model;
    private JTextField inputField;
    private JButton ellipsisButton;
    private JButton imageButton;
    private JButton sendMessageButton;
    private JButton scrollDown;
    private JPanel glassPane;
    private CardLayout conversationRootLayout;
    private JPanel conversationRoot;

    private boolean conversationShown = false;

    public static boolean closeRequested = false;

    public static final String APPLICATION_NAME = "ByteThrow Messenger " + ByteThrowClient.version;

    public Window() {
        setTitle(APPLICATION_NAME);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1000, 750));
        setIconImage(FileUtility.getImageResource("icon.png"));
        setLayout(new BorderLayout());

        settingsWindow = new SettingsWindow();

        pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setDividerSize(0);
        getContentPane().add(pane, BorderLayout.CENTER);

        initChatChooser();
        initConversation();
        initToolBar();
        initGlassPane();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeRequested = true;
                ByteThrowClient.messagingClient.stop(0);
            }
        });
        pack();
    }

    private void initChatChooser(){
        JPanel chatChooser = new JPanel(new GridBagLayout());
        pane.setLeftComponent(chatChooser);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        model = new DefaultListModel<>();
        connectedUsers = new JList<>(model);
        connectedUsers.setFont(Variables.defaultFont);
        connectedUsers.setPreferredSize(new Dimension(250, 750));
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        connectedUsers.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        connectedUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selectedIndex = connectedUsers.locationToIndex(e.getPoint());
                if (SwingUtilities.isRightMouseButton(e)) {
                    connectedUsers.setSelectedIndex(selectedIndex);
                    JPopupMenu popupMenu = new JPopupMenu();
                    JMenuItem closeConnectionMenuItem = new JMenuItem("Close Conversation");
                    popupMenu.add(closeConnectionMenuItem);
                    closeConnectionMenuItem.addActionListener(ae -> ByteThrowClient.messagingClient.closeConnectionWithUser(connectedUsers.getSelectedValue()));

                    JMenuItem requestRefreshMenuItem = new JMenuItem();
                    popupMenu.add(requestRefreshMenuItem);
                    requestRefreshMenuItem.addActionListener(ae -> ByteThrowClient.messagingClient.client.sendTCP(ClientUtils.createHistoryRequest(MessagingClient.serverConnection, connectedUsers.getSelectedValue())));
                    popupMenu.show(connectedUsers, e.getX(), e.getY());
                }else{
                    if(connectedUsers.getSelectedValue() != null) {
                        conversationPane.clear();
                        MessagingClient.addUserConnectionToConversation(MessagingClient.otherUsers.get(connectedUsers.getSelectedValue()));
                        conversationRootLayout.show(conversationRoot, "conversation");
                        conversationShown = true;
                        JScrollBar verticalBar = conversationScrollPane.getVerticalScrollBar();
                        verticalBar.setValue(verticalBar.getMaximum());
                        repaint();
                    }else {
                        conversationPane.clear();
                        conversationRootLayout.show(conversationRoot, "blank");
                    }
                }
            }
        });
        chatChooser.add(connectedUsers, constraints);

        JButton startConversation = new JButton("Start Conversation");
        startConversation.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Please input the username of the conversation partner: ", "Start Conversation", JOptionPane.PLAIN_MESSAGE);
            if (username.length() < 3 || username == null) {
                JOptionPane.showMessageDialog(this, "Invalid Username (" + username + ") (has to be at least 3 characters)", "", JOptionPane.ERROR_MESSAGE);
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
    }

    private void initConversation(){
        conversationRootLayout = new CardLayout();
        conversationRoot = new JPanel(conversationRootLayout);
        pane.setRightComponent(conversationRoot);

        JPanel blank = new JPanel();
        conversationRoot.add(blank, "blank");

        JPanel conversation = new JPanel(new GridBagLayout());
        conversationRoot.add(conversation, "conversation");

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;

        conversationScrollPane = new JScrollPane(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 24;
        constraints.weightx = 1;
        constraints.weighty = 1;
        conversation.add(conversationScrollPane, constraints);

        conversationScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        conversationPane = new ConversationPane(750, 720);
        conversationScrollPane.setViewportView(conversationPane);
        conversationPane.setBackground(new Color(60, 63, 65));

        inputField = new HintTextField("Send Message...");
        inputField.setFont(Variables.defaultFont);
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    send();
                    inputField.requestFocus();
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
    }

    private void initToolBar(){
        JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);

        ellipsisButton = new JButton();
        ellipsisButton.setIcon(new ImageIcon(FileUtility.getImageResource("ellipsis" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        ellipsisButton.addActionListener(e -> {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem settings = new JMenuItem("Settings");
            settings.addActionListener(ae -> settingsWindow.setVisible(true));
            menu.add(settings);
            JMenuItem account = new JMenuItem("My Account");
            account.addActionListener(ae -> {
                accountSettings = new AccountSettings(new ImageIcon(ImageUtility.resizeImage(FileUtility.getImageResource("icon.png"), 150, 150)), ByteThrowClient.getUsername());
                accountSettings.showDialog();
            });
            menu.add(account);
            menu.show((Component) e.getSource(), 15,15);
        });

        toolBar.add(ellipsisButton);

        toolBar.add(Box.createVerticalGlue());

        scrollDown = new JButton();
        scrollDown.setIcon(new ImageIcon(FileUtility.getImageResource("arrow" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        scrollDown.setToolTipText("Scroll to the bottom");
        scrollDown.addActionListener(e -> {
            JScrollBar verticalBar = conversationScrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
        toolBar.add(scrollDown);
        scrollDown.setVisible(conversationShown);

        imageButton = new JButton();
        imageButton.setIcon(new ImageIcon(FileUtility.getImageResource("image" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        imageButton.setToolTipText("Send Images");
        imageButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            File picturesDirectory = new File(System.getProperty("user.home") + "/Pictures/");
            if(picturesDirectory.exists() && picturesDirectory.isDirectory()){
                chooser = new JFileChooser(picturesDirectory);
            }
            FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter("Supported Image Files", "png", "jpg");
            chooser.setFileFilter(fileNameExtensionFilter);
            if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                File[] selectedFiles = chooser.getSelectedFiles();
                List<FormattedImage> imageList = new ArrayList<>();
                for(File file : selectedFiles){
                    if(file.getName().endsWith(".png") || file.getName().endsWith(".jpg")){
                        try {
                            imageList.add(FormattedImage.read(file));
                        }catch (IOException ioe){
                            BTMLogger.warn("MessagingClient", "Error loading Image File: " + file);
                        }
                    }
                }
                FormattedImage[] images = new FormattedImage[imageList.size()];
                for(int i=0;i<images.length;i++){
                    images[i] = imageList.get(i);
                }

            }
        });
        //toolBar.add(imageButton);
        imageButton.setVisible(conversationShown);

        sendMessageButton = new JButton();
        sendMessageButton.setIcon(new ImageIcon(FileUtility.getImageResource("send" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        sendMessageButton.setToolTipText("Send Message");
        sendMessageButton.addActionListener(e -> {
            send();
            inputField.requestFocus();
        });
        toolBar.add(sendMessageButton);
        sendMessageButton.setVisible(conversationShown);

        toolBar.setFloatable(false);

        getContentPane().add(toolBar, BorderLayout.EAST);
    }

    private void initGlassPane(){
        glassPane = new JPanel(new GridBagLayout()){
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics2D = (Graphics2D) g;
                graphics2D.setColor(new Color(100, 100, 100, 127));
                graphics2D.fillRect(0,0, glassPane.getWidth(), glassPane.getHeight());
            }
        };
        glassPane.setOpaque(false);
        setGlassPane(glassPane);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
                    if(glassPane.isVisible()) {
                        hideGlassPane();
                    }
                }
            }
        });
    }

    private void send(){
        String message = inputField.getText();
        if (ByteThrowClient.messagingClient.sendToOtherUser(connectedUsers.getSelectedValue(), message)) {
            addMessage(new TextMessage(ByteThrowClient.getUsername(), message), ConversationPane.RIGHT);
        } else {
            JOptionPane.showMessageDialog(this, "Error processing message", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
        inputField.setText("");
        System.out.println("Sent Message");
    }

    private void sendImages(String caption, FormattedImage... images){
        if(ByteThrowClient.messagingClient.sendImagesToOtherUser(connectedUsers.getSelectedValue(), caption, images)){
            addMessage(new ImageMessage(ByteThrowClient.getUsername(), caption, images), ConversationPane.RIGHT);
        }else{
            JOptionPane.showMessageDialog(this, "Error processing message", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showGlassPaneWithImage(BufferedImage image){
        BufferedImage resizedImage = ImageUtility.resizeImageUniformlyX(image, getContentPane().getWidth() / 3 * 2);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(20, 20, 20, 20);
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        glassPane.add(new ImagePanel(resizedImage, resizedImage.getWidth(), resizedImage.getHeight()), constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.NONE;
        JButton closeButton = new JButton();
        closeButton.setIcon(new ImageIcon(FileUtility.getImageResource("close" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        closeButton.addActionListener(e -> hideGlassPane());
        glassPane.add(closeButton, constraints);
        glassPane.setVisible(true);
        enableAllSub(getContentPane(), false);
    }

    public void hideGlassPane(){
        glassPane.removeAll();
        glassPane.setVisible(false);
        enableAllSub(getContentPane(), true);
    }

    private void enableAllSub(Container container, boolean aFlag){
        for(Component c : container.getComponents()){
            c.setEnabled(aFlag);
            if(c instanceof Container){
                enableAllSub((Container) c, aFlag);
            }
        }
    }

    /**
     * Adds a Message to the ConversationPane
     */
    public void addMessage(Message message, int alignment) {
        conversationPane.addMessage(new MessageFrame(message), alignment == ConversationPane.LEFT ? new Color(69, 73, 74) : new Color(65, 83, 88), alignment);
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = conversationScrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
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
        ellipsisButton.setIcon(new ImageIcon(FileUtility.getImageResource("ellipsis" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        imageButton.setIcon(new ImageIcon(FileUtility.getImageResource("image" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        sendMessageButton.setIcon(new ImageIcon(FileUtility.getImageResource("send" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        scrollDown.setIcon(new ImageIcon(FileUtility.getImageResource("arrow" + (SettingsWindow.isLight() ? "_dark" : "") + ".png")));
        scrollDown.setVisible(conversationShown);
        imageButton.setVisible(conversationShown);
        sendMessageButton.setVisible(conversationShown);
        inputField.setFont(Variables.defaultFont);
        connectedUsers.setFont(Variables.defaultFont);
        conversationPane.setBackground(SettingsWindow.isLight() ? Color.white : new Color(60, 63, 65));
    }
}
