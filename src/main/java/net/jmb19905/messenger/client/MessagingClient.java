package net.jmb19905.messenger.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.jmb19905.messenger.client.ui.conversation.ConversationPane;
import net.jmb19905.messenger.client.ui.util.dialoges.LoginDialog;
import net.jmb19905.messenger.client.ui.Window;
import net.jmb19905.messenger.client.ui.util.dialoges.RegisterDialog;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.packages.*;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.*;
import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MessagingClient extends Listener {

    private final String serverAddress;
    private final int serverPort;

    public Client client;
    public static final EncryptedConnection serverConnection = new EncryptedConnection();

    public static HashMap<String, ChatHistory> otherUsers;
    public static final List<String> connectionRequested = new ArrayList<>();
    @Deprecated
    public static final List<String> connectionToBeVerified = new ArrayList<>();

    public MessagingClient(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.serverPort = port;
        init();
    }

    private void init() {
        BTMLogger.trace("MessagingClient", "Initializing Client");
        client = new Client();

        Util.registerPackages(client.getKryo());
        BTMLogger.trace("MessagingClient", "Registered Packages");

        client.addListener(this);
        BTMLogger.trace("MessagingClient", "Added Listener");
        BTMLogger.info("MessagingClient", "Initialized Client");
    }

    /**
     * Starts the Client
     */
    public void start() {
        BTMLogger.trace("MessagingClient", "Starting Client");
        new Thread(client).start();
        try {
            client.connect(5000, serverAddress, serverPort, serverPort + 1);
        } catch (IOException e) {
            BTMLogger.error("MessagingClient", "Error establishing connection", e);
            JOptionPane.showMessageDialog(null, "Error connecting to server! Please check internet connection.", "Connection Failure", JOptionPane.ERROR_MESSAGE);
            stop(-1);
        }

        BTMLogger.info("MessagingClient", "Started Client");
    }

    /**
     * Stops the client
     * @param code the exit code
     */
    public void stop(int code) {
        BTMLogger.trace("MessagingClient", "Stopping Client");
        client.stop();
        BTMLogger.info("MessagingClient", "Stopped Client");
        if (!ByteThrowClient.getUsername().equals("") && ByteThrowClient.getUsername() != null) {
            FileUtility.saveChatHistories(otherUsers);
        }
        BTMLogger.close();
        System.exit(code);
    }

    /**
     * What to do when the Client connects with the Server
     */
    @Override
    public void connected(Connection connection) {
        BTMLogger.info("MessagingClient", "Connection established with: " + connection.getRemoteAddressTCP(), null);
        PublicKeyPacket publicKeyPacket = ClientUtils.createPubicKeyPacket(ByteThrowClient.version, serverConnection);
        connection.sendTCP(publicKeyPacket);
    }

    /**
     * What to do when the Client disconnects from the Server
     */
    @Override
    public void disconnected(Connection connection) {
        BTMLogger.info("MessagingClient", "Lost Connection");
        connection.close();
        if (!Window.closeRequested) {
            client.stop();
            ByteThrowClient.window.dispose();
            Thread reconnectionThread = new Thread(() -> ByteThrowClient.main(ByteThrowClient.arguments));
            reconnectionThread.start();
        }
    }

    /**
     * What to do when the Client receives a Package from the Server
     */
    @Override
    public void received(Connection connection, Object packet) {
        if (packet instanceof BTMPacket) {
            try {
                ((BTMPacket) packet).handleOnClient(connection);
            } catch (UnsupportedSideException e) {
                BTMLogger.warn("MessagingClient", "Package received on wrong side", e);
            }
        }
    }

    /**
     * Connects the User with another user
     * @param username the username of the other user
     */
    public void connectWithOtherUser(String username) {
        EncryptedConnection endToEndConnection = new EncryptedConnection();
        StartEndToEndConnectionPacket connectPacket = ClientUtils.createStartEndToEndConnectionPacket(username, serverConnection, endToEndConnection);
        client.sendTCP(connectPacket);
        ChatHistory chatHistory = new ChatHistory(username, endToEndConnection);
        otherUsers.put(username, chatHistory);
        connectionRequested.add(username);
    }

    /**
     * Send a Message to another user
     * @param username the username of the user
     * @param message the message that will be sent
     * @return if sending succeeded
     */
    public boolean sendToOtherUser(String username, String message) {
        ChatHistory chatHistory = otherUsers.get(username);
        if (chatHistory != null && chatHistory.getNode() != null) {
            if (chatHistory.getNode().getSharedSecret() != null) {
                client.sendTCP(ClientUtils.createDataPacket(username, message, serverConnection, chatHistory.getNode()));
                chatHistory.addMessage(ByteThrowClient.getUsername(), "text", message);
                return true;
            } else {
                BTMLogger.warn("MessagingClient", "Cannot send to " + username + ". No SharedSecret Key.");
            }
        } else {
            BTMLogger.info("MessagingClient", "Your are not connected with this client");
        }
        return false;
    }

    public boolean sendImagesToOtherUser(String username, String caption, FormattedImage... images){
        ChatHistory chatHistory = otherUsers.get(username);
        if (chatHistory != null && chatHistory.getNode() != null) {
            if (chatHistory.getNode().getSharedSecret() != null) {
                StringBuilder imagePathsBuilder = new StringBuilder();
                DataPacket dataPacket = ClientUtils.createDataPacket(username, caption, images, serverConnection, chatHistory.getNode(), imagePathsBuilder);
                client.sendTCP(dataPacket);
                chatHistory.addMessage(ByteThrowClient.getUsername(), "file", imagePathsBuilder.toString());
                return true;
            } else {
                BTMLogger.warn("MessagingClient", "Cannot send to " + username + ". No SharedSecret Key.");
            }
        } else {
            BTMLogger.info("MessagingClient", "Your are not connected with this client");
        }
        return false;
    }

    /**
     * Logs the Client in
     */
    public void login(Connection connection) {
        if (!ByteThrowClient.getUsername().equals("") && !ByteThrowClient.getPassword().equals("")) {
            LoginPacket loginPacket = ClientUtils.createLoginPacket(ByteThrowClient.getUsername(), ByteThrowClient.getPassword(), serverConnection);
            connection.sendTCP(loginPacket);
        } else {
            LoginDialog loginDialog = new LoginDialog();
            loginDialog.addRegisterButtonActionListener(e -> register(connection));
            loginDialog.addConfirmButtonActionListener(e -> {
                LoginPacket loginPacket = ClientUtils.createLoginPacket(loginDialog.getUsername(), loginDialog.getPassword(), serverConnection);
                connection.sendTCP(loginPacket);
                ByteThrowClient.setUserData(loginDialog.getUsername(), loginDialog.getPassword());
            });
            loginDialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    stop(0);
                }
            });
        }
    }

    /**
     * Registers a new account for the Client
     */
    public void register(Connection connection) {
        RegisterDialog registerDialog = new RegisterDialog(true);
        registerDialog.addLoginButtonActionListener(e -> login(connection));
        registerDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stop(0);
            }
        });
        registerDialog.addConfirmButtonActionListener(e -> {
            RegisterPacket registerPacket = ClientUtils.createRegisterPacket(registerDialog.getUsername(), registerDialog.getPassword(), serverConnection);
            client.sendTCP(registerPacket);
            BTMLogger.trace("MessagingClient", "Sent Registering Data... Waiting for response");
        });
    }

    /**
     * Loads the ChatHistory and Nodes of all connected users
     */
    public static void initOtherUsers() {
        otherUsers = FileUtility.loadChatHistories();
        try {
            for (String key : otherUsers.keySet()) {
                ChatHistory chatHistory = otherUsers.get(key);
                if (key.equals(chatHistory.getName())) {
                    addChatHistory(chatHistory);
                } else {
                    BTMLogger.warn("MessagingClient", "Error parsing ChatHistory -> wrong username");
                }
                System.out.println("Setting ChatHistory for: " + key);
            }
        }catch (NullPointerException e){
            BTMLogger.warn("MessagingClient", "Error parsing ChatHistory");
        }
    }

    /**
     * Adds a ChatHistory to the Window
     * @param chatHistory the ChatHistory
     */
    private static void addChatHistory(ChatHistory chatHistory) {
        for (String rawMessage : chatHistory.getMessages()) {
            String[] parts = rawMessage.split(":");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                stringBuilder.append(parts[i]);
            }
            ByteThrowClient.window.addMessage(parts[0], stringBuilder.toString(), (parts[0].equals(ByteThrowClient.getUsername())) ? ConversationPane.RIGHT : ConversationPane.LEFT);
        }
    }

    /**
     * Sets the Public Key for the Client - Server connection
     * @param encodedKey the PublicKey of the Server encoded as byte-array
     */
    public void setPublicKey(byte[] encodedKey) {
        setPublicKey(encodedKey, serverConnection);
    }

    /**
     * Sets the Public Key for a EncryptedConnection
     * @param decryptedEncodedKey the PublicKey of encoded as byte-array
     * @param encryptedConnection the EncryptedConnection that will receive and use the PublicKey
     */
    public void setPublicKey(byte[] decryptedEncodedKey, EncryptedConnection encryptedConnection) {
        try {
            PublicKey publicKey = EncryptionUtility.createPublicKeyFromData(decryptedEncodedKey);
            BTMLogger.trace("MessagingClient", "Received PublicKey");
            if (publicKey != null) {
                encryptedConnection.setReceiverPublicKey(publicKey);
            }
        } catch (InvalidKeySpecException e) {
            BTMLogger.error("MessagingServer", "Error setting PublicKey. Key is invalid.", e);
            JOptionPane.showMessageDialog(null, "There was an error during the Server - Client Key exchange", "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

}
