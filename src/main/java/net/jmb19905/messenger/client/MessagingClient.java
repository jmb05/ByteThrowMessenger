package net.jmb19905.messenger.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.jmb19905.messenger.client.ui.OptionPanes;
import net.jmb19905.messenger.client.ui.Window;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.packages.*;
import net.jmb19905.messenger.packages.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.BTMLogger;
import net.jmb19905.messenger.util.Util;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MessagingClient extends Listener {

    private final String serverAddress;
    private final int serverPort;

    public Client client;
    public static final Node thisDevice = new Node();

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
            Util.saveChatHistories(otherUsers);
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
        LoginPublicKeyPackage loginPublicKeyPackage = new LoginPublicKeyPackage();
        loginPublicKeyPackage.version = ByteThrowClient.version;
        loginPublicKeyPackage.encodedKey = thisDevice.getPublicKey().getEncoded();
        connection.sendTCP(loginPublicKeyPackage);
        BTMLogger.trace("MessagingClient", "Sent PublicKey");
    }

    /**
     * What to do when the Client disconnects from the Server
     */
    @Override
    public void disconnected(Connection connection) {
        BTMLogger.info("MessagingClient", "Lost Connection");
        connection.close();
        if (!Window.closeRequested) {
            ByteThrowClient.window.dispose();
            Thread reconnectionThread = new Thread(() -> ByteThrowClient.main(ByteThrowClient.arguments));
            reconnectionThread.start();
        }
    }

    /**
     * What to do when the Client receives a Package from the Server
     */
    @Override
    public void received(Connection connection, Object o) {
        if (o instanceof BTMPackage) {
            try {
                ((BTMPackage) o).handleOnClient(connection);
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
        Node node = new Node();
        ConnectWithOtherUserPackage connectPackage = new ConnectWithOtherUserPackage();
        connectPackage.username = Util.encryptString(thisDevice, username);
        connectPackage.publicKeyEncodedEncrypted = thisDevice.encrypt(node.getPublicKey().getEncoded());
        client.sendTCP(connectPackage);
        ChatHistory chatHistory = new ChatHistory(username, node);
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
                DataPackage dataPackage = new DataPackage();
                dataPackage.username = Util.encryptString(thisDevice, username);
                dataPackage.encryptedMessage = thisDevice.encrypt(chatHistory.getNode().encrypt(message.getBytes(StandardCharsets.UTF_8)));
                client.sendTCP(dataPackage);
                chatHistory.addMessage(ByteThrowClient.getUsername(), message);
                return true;
            } else {
                BTMLogger.warn("MessagingClient", "Cannot send to " + username + ". No SharedSecret Key.");
            }
        } else {
            BTMLogger.info("MessagingClient", "Your are not connected with this client - use connect");
        }
        return false;
    }

    /**
     * Logs the Client in
     */
    public void login(Connection connection) {
        if (!ByteThrowClient.getUsername().equals("") && !ByteThrowClient.getPassword().equals("")) {
            LoginPackage loginPackage = new LoginPackage();
            loginPackage.username = Util.encryptString(thisDevice, ByteThrowClient.getUsername());
            loginPackage.password = Util.encryptString(thisDevice, ByteThrowClient.getPassword());
            connection.sendTCP(loginPackage);
        } else {
            OptionPanes.OutputValue value = OptionPanes.showLoginDialog((e) -> register(connection));
            if (value.id == OptionPanes.OutputValue.CANCEL_OPTION) {
                System.exit(0);
            } else if (value.id == OptionPanes.OutputValue.CONFIRM_OPTION) {
                LoginPackage loginPackage = new LoginPackage();
                loginPackage.username = Util.encryptString(thisDevice, value.values[0]);
                loginPackage.password = Util.encryptString(thisDevice, value.values[1]);
                connection.sendTCP(loginPackage);
                ByteThrowClient.setUserData(value.values[0], value.values[1]);
            }
        }
    }

    /**
     * Registers a new account for the Client
     */
    public void register(Connection connection) {
        OptionPanes.OutputValue value = OptionPanes.showRegisterDialog((e) -> login(connection));
        if (value.id == OptionPanes.OutputValue.CANCEL_OPTION) {
            stop(0);
        } else if (value.id == OptionPanes.OutputValue.CONFIRM_OPTION) {
            RegisterPackage registerPackage = new RegisterPackage();
            registerPackage.username = Util.encryptString(thisDevice, value.values[0]);
            registerPackage.password = Util.encryptString(thisDevice, value.values[1]);
            client.sendTCP(registerPackage);
            BTMLogger.trace("MessagingClient", "Sent Registering Data... Waiting for response");
        }
    }

    /**
     * Loads the ChatHistory and Nodes of all connected users
     */
    public static void initOtherUsers() {
        otherUsers = Util.loadChatHistories();
        try {
            for (String key : otherUsers.keySet()) {
                ChatHistory chatHistory = otherUsers.get(key);
                if (key.equals(chatHistory.getName())) {
                    addChatHistory(chatHistory);
                } else {
                    BTMLogger.warn("MessagingClient", "Error parsing ChatHistory -> wrong username");
                }
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
            ByteThrowClient.window.appendLine("<" + parts[0] + "> " + stringBuilder.toString());
        }
    }

    /**
     * Sets the Public Key for the Client - Server connection
     * @param encodedKey the PublicKey of the Server encoded as byte-array
     */
    public void setPublicKey(byte[] encodedKey) {
        setPublicKey(encodedKey, thisDevice);
    }

    /**
     * Sets the Public Key for a Node
     * @param decryptedEncodedKey the PublicKey of encoded as byte-array
     * @param node the Node that will receive and use the PublicKey
     */
    public void setPublicKey(byte[] decryptedEncodedKey, Node node) {
        try {
            PublicKey publicKey = Util.createPublicKeyFromData(decryptedEncodedKey);
            BTMLogger.trace("MessagingClient", "Received PublicKey");
            if (publicKey != null) {
                node.setReceiverPublicKey(publicKey);
            }
        } catch (InvalidKeySpecException e) {
            BTMLogger.error("MessagingServer", "Error setting PublicKey. Key is invalid.", e);
            JOptionPane.showMessageDialog(null, "There was an error during the Server - Client Key exchange", "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

}
