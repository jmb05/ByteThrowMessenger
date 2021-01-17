package net.jmb19905.messenger.server;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.EMMessage;
import net.jmb19905.messenger.messages.LoginPublicKeyMessage;
import net.jmb19905.messenger.messages.SuccessMessage;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import java.io.IOException;
import java.net.BindException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.UUID;

public class MessagingServer extends Listener {

    private final int port;
    private final Server server;

    public static final HashMap<Connection, ClientConnection> clientConnectionKeys = new HashMap<>();
    public static final HashMap<String, HashMap<EMMessage, Object[]>> messagesQueue = new HashMap<>();

    public MessagingServer() {
        EMLogger.trace("MessagingServer", "Initializing Server");
        this.port = ServerMain.config.port;
        server = new Server();

        Util.registerMessages(server.getKryo());
        EMLogger.trace("MessagingServer", "Registered Messages");

        server.addListener(this);
        EMLogger.trace("MessagingServer", "Added Listener");
        EMLogger.info("MessagingServer", "Initialized Server");
    }

    /**
     * Starts the Server
     */
    public void start() {
        EMLogger.trace("MessagingServer", "Starting Server");
        new Thread(server).start();
        try {
            server.bind(port, port + 1);
        } catch (BindException e) {
            EMLogger.error("MessagingServer", "Error Binding Server to port: " + port + "! Probably is a server already running!", e);
            System.exit(-1);
        } catch (IOException e) {
            EMLogger.error("MessagingServer", "Error binding server", e);
        }
        EMLogger.info("MessagingServer", "Started Server");
    }

    /**
     * What to do when a Client connects to the Server
     */
    @Override
    public void connected(Connection connection) {
        EMLogger.info("MessagingServer", "Connection established with: " + connection.getRemoteAddressTCP());
    }

    /**
     * What to do when a Client disconnects from the Server
     */
    @Override
    public void disconnected(Connection connection) {
        EMLogger.info("MessagingServer", "Lost Connection to a Client");
        connection.close();
        clientConnectionKeys.remove(connection);
    }

    /**
     * What to do when a Message from a Client is received
     */
    @Override
    public void received(Connection connection, Object o) {
        if (o instanceof EMMessage) {
            try {
                ((EMMessage) o).handleOnServer(connection);
            } catch (UnsupportedSideException e) {
                EMLogger.warn("MessagingServer", "Message received on wrong side", e);
            }
        }
    }

    /**
     * Tells a Client that the registration succeeded
     * @param connection the connection to the Client
     * @param username the username of the Client
     * @param uuid the UUID of the Client
     */
    public void sendRegisterSuccess(Connection connection, String username, UUID uuid) {
        SuccessMessage message = new SuccessMessage();
        message.type = "register";
        connection.sendTCP(message);
    }

    /**
     * Sends the Client a PublicKey
     * @param connection the connection to the Client
     * @param encodedKey the PublicKey encoded as byte-array
     */
    public void sendPublicKey(Connection connection, byte[] encodedKey) {
        Node clientConnection = initNode(connection, encodedKey);
        LoginPublicKeyMessage loginPublicKeyMessage = new LoginPublicKeyMessage();
        loginPublicKeyMessage.encodedKey = clientConnection.getPublicKey().getEncoded();
        connection.sendTCP(loginPublicKeyMessage);
        EMLogger.trace("MessagingServer", "Sent Public Key");
    }

    /**
     * Initializes a Node when the PublicKey of a Client is received
     * @param connection the connection to the Client
     * @param encodedKey the PublicKey from the Client encoded as byte-array
     * @return the Node
     */
    private Node initNode(Connection connection, byte[] encodedKey) {
        try {
            Node clientConnection = new Node();
            PublicKey publicKey = Util.createPublicKeyFromData(encodedKey);
            clientConnection.setReceiverPublicKey(publicKey);
            clientConnectionKeys.put(connection, new ClientConnection(clientConnection, false));
            return clientConnection;
        } catch (InvalidKeySpecException e) {
            EMLogger.error("MessagingServer", "Error initializing Node. Key is invalid.");
            return null;
        }
    }

}