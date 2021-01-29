package net.jmb19905.messenger.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.jmb19905.messenger.client.ui.conversation.ConversationPane;
import net.jmb19905.messenger.client.ui.util.dialoges.LoginDialog;
import net.jmb19905.messenger.client.ui.Window;
import net.jmb19905.messenger.client.ui.util.dialoges.RegisterDialog;
import net.jmb19905.messenger.crypto.EncryptedConnection;
import net.jmb19905.messenger.messages.ImageMessage;
import net.jmb19905.messenger.messages.Message;
import net.jmb19905.messenger.messages.TextMessage;
import net.jmb19905.messenger.packets.*;
import net.jmb19905.messenger.packets.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.*;
import net.jmb19905.messenger.util.logging.BTMLogger;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MessagingClient extends Listener {

    private final String serverAddress;
    private final int serverPort;

    public Client client;
    public static final EncryptedConnection serverConnection = new EncryptedConnection();

    public static HashMap<String, UserConnection> otherUsers = new HashMap<>();
    public static final List<String> connectionRequested = new ArrayList<>();
    @Deprecated
    public static final List<String> connectionToBeVerified = new ArrayList<>();

    private boolean keepAliveRequired = false;
    public final Thread keepAlive = new Thread("KeepAlive"){
        @SuppressWarnings("BusyWait")
        @Override
        public void run() {
            while (isKeepAliveRequired()){
                if(client.isConnected()){
                    client.sendTCP(new KeepAlivePacket());
                }
                try {
                    Thread.sleep(8000);
                } catch (InterruptedException e) {
                    BTMLogger.warn("MessagingClient", "KeepAlive Thread threw InterruptedException", e);
                }
            }
        }
    };

    public MessagingClient(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.serverPort = port;
        init();
    }

    private void init() {
        BTMLogger.trace("MessagingClient", "Initializing Client");
        client = new Client();
        client.setTimeout(60000);

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
            JOptionPane.showMessageDialog(ByteThrowClient.window, "Error connecting to server! Please check internet connection.", "Connection Failure", JOptionPane.ERROR_MESSAGE);
            stop(-1);
        }

        BTMLogger.info("MessagingClient", "Started Client");
        keepAliveRequired = true;
        keepAlive.start();
    }

    /**
     * Stops the client
     * @param code the exit code
     */
    public void stop(int code) {
        BTMLogger.trace("MessagingClient", "Stopping Client");
        for(java.awt.Window w : java.awt.Window.getWindows()){
            w.setVisible(false);
            w.dispose();
        }
        BTMLogger.info("MessagingClient", "Stopped Client");
        if (!ByteThrowClient.getUsername().equals("") && ByteThrowClient.getUsername() != null) {
            FileUtility.saveUserConnections(otherUsers);
        }
        client.stop();
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
        stop(0);
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
        UserConnection userConnection = new UserConnection(username, endToEndConnection);
        otherUsers.put(username, userConnection);
        connectionRequested.add(username);
    }

    public void closeConnectionWithUser(String username){
        try {
            MessagingClient.otherUsers.get(username).close();
        }catch (NullPointerException ex){
            //If There is/are no History/Keys delete the file anyway
            MessagingClient.forceClose(username);
        }
        ByteThrowClient.window.removeConnectedUser(username);
        EncryptedConnection otherUser = otherUsers.get(username).getEncryptedConnection();
        E2EInfoPacket closePacket = ClientUtils.createCloseConnectionPacket(username, otherUser);
        ByteThrowClient.messagingClient.client.sendTCP(closePacket);
        MessagingClient.otherUsers.remove(username);
    }

    public static void forceClose(String username){
        File connectionFile = new File("userdata/" + ByteThrowClient.getUsername() + "/" + username + ".json");
        if(connectionFile.exists()){
            connectionFile.delete();
        }
        //TODO: delete media if wanted
    }

    /**
     * Send a Message to another user
     * @param username the username of the user
     * @param message the message that will be sent
     * @return if sending succeeded
     */
    public boolean sendToOtherUser(String username, String message) {
        UserConnection userConnection = otherUsers.get(username);
        if (userConnection != null && userConnection.getEncryptedConnection() != null) {
            if (userConnection.getEncryptedConnection().getSharedSecret() != null) {
                client.sendTCP(ClientUtils.createDataPacket(username, message, serverConnection, userConnection.getEncryptedConnection()));
                userConnection.addMessage(new TextMessage(ByteThrowClient.getUsername(), message));
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
        UserConnection userConnection = otherUsers.get(username);
        if (userConnection != null && userConnection.getEncryptedConnection() != null) {
            if (userConnection.getEncryptedConnection().getSharedSecret() != null) {
                DataPacket dataPacket = ClientUtils.createDataPacket(username, caption, images, serverConnection, userConnection.getEncryptedConnection());
                client.sendTCP(dataPacket);
                userConnection.addMessage(new ImageMessage(ByteThrowClient.getUsername(), caption, images));
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
    public void login(Connection connection, String username, String password) {
        if (!ByteThrowClient.getUsername().equals("") && !ByteThrowClient.getPassword().equals("")) {
            LoginPacket loginPacket = ClientUtils.createLoginPacket(ByteThrowClient.getUsername(), ByteThrowClient.getPassword(), serverConnection);
            connection.sendTCP(loginPacket);
        } else {
            LoginDialog loginDialog = new LoginDialog(username, password, "");
            loginDialog.addRegisterButtonActionListener(e -> register(connection));
            loginDialog.addConfirmButtonActionListener(e -> {
                LoginPacket loginPacket = ClientUtils.createLoginPacket(loginDialog.getUsername(), loginDialog.getPassword(), serverConnection);
                connection.sendTCP(loginPacket);
                ByteThrowClient.setUserData(loginDialog.getUsername(), loginDialog.getPassword());
            });
            loginDialog.addCancelListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    Window.closeRequested = true;
                    stop(0);
                }
            });
            loginDialog.showDialog();
        }
    }

    /**
     * Registers a new account for the Client
     */
    public void register(Connection connection) {
        RegisterDialog registerDialog = new RegisterDialog(true);
        registerDialog.addLoginButtonActionListener(e -> login(connection, "", ""));
        registerDialog.addCancelListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Window.closeRequested = true;
                stop(0);
            }
        });
        registerDialog.addConfirmButtonActionListener(e -> {
            RegisterPacket registerPacket = ClientUtils.createRegisterPacket(registerDialog.getUsername(), registerDialog.getPassword(), serverConnection);
            client.sendTCP(registerPacket);
            ByteThrowClient.setUserData(registerDialog.getUsername(), registerDialog.getPassword());
            BTMLogger.trace("MessagingClient", "Sent Registering Data... Waiting for response");
        });
        registerDialog.showDialog();
    }

    /**
     * Loads the UserConnection and Nodes of all connected users
     */
    public static void initOtherUsers() {
        otherUsers = FileUtility.loadUserConnections();
        if(ByteThrowClient.window != null){
            for(String username : otherUsers.keySet()){
                ByteThrowClient.window.addConnectedUser(username);
            }
        }
    }

    /**
     * Adds a UserConnection to the Window
     * @param userConnection the UserConnection
     */
    public static void addUserConnectionToConversation(UserConnection userConnection) {
        for (Message message : userConnection.getMessages()) {
            ByteThrowClient.window.addMessage(message, (message.sender.equals(ByteThrowClient.getUsername())) ? ConversationPane.RIGHT : ConversationPane.LEFT);
        }
        System.out.println("Added user: " + userConnection.getName() + " to conversation panel");
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
            JOptionPane.showMessageDialog(ByteThrowClient.window, "There was an error during the Server - Client Key exchange", "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    public boolean isKeepAliveRequired() {
        return keepAliveRequired;
    }

    public void setKeepAliveRequired(boolean keepAliveRequired) {
        this.keepAliveRequired = keepAliveRequired;
    }
}
