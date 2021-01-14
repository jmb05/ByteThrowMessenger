package net.jmb19905.messenger.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.*;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;
import net.jmb19905.messenger.util.Variables;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.BindException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.UUID;

public class MessagingServer extends Listener{

    private final int port;
    private final Server server;

    public final HashMap<Connection, ClientConnection> clientConnectionKeys = new HashMap<>();

    public MessagingServer() {
        EMLogger.trace("MessagingServer", "Initializing Server");
        this.port = Variables.DEFAULT_PORT;
        server = new Server();

        Util.registerMessages(server.getKryo());
        EMLogger.trace("MessagingServer", "Registered Messages");

        server.addListener(this);
        EMLogger.trace("MessagingServer", "Added Listener");
        EMLogger.info("MessagingServer", "Initialized Server");
    }

    public void start(){
        EMLogger.trace("MessagingServer", "Starting Server");
        new Thread(server).start();
        try {
            server.bind(port, port + 1);
        }catch (BindException e){
            EMLogger.error("MessagingServer", "Error Binding Server to port: " + port + "! Probably is a server already running!", e);
            System.exit(-1);
        } catch (IOException e) {
            EMLogger.error("MessagingServer", "Error binding server", e);
        }
        EMLogger.info("MessagingServer", "Started Server");
    }

    public void stop(){
        server.stop();
    }

    @Override
    public void connected(Connection connection) {
        EMLogger.info("MessagingServer", "Connection established with: " + connection.getRemoteAddressTCP());
    }

    @Override
    public void disconnected(Connection connection) {
        EMLogger.info("MessagingServer", "Lost Connection to a Client");
        connection.close();
        clientConnectionKeys.remove(connection);
    }

    @Override
    public void received(Connection connection, Object o) {
        if(o instanceof EMMessage){
            try {
                ((EMMessage) o).handleOnServer(connection);
            } catch (UnsupportedSideException e) {
                EMLogger.warn("MessagingServer", "Message received on wrong side", e);
            }
        }
    }

    public void sendRegisterSuccess(Connection connection, String username, UUID uuid) {
        RegisterSuccessfulMessage message = new RegisterSuccessfulMessage();
        message.username = username;
        message.uuid = uuid.toString();
        connection.sendTCP(message);
    }

    public UUID createUser(String username, String password) {
        String salt = BCrypt.gensalt();
        UUID uuid = UUID.randomUUID();

        SQLiteManager.UserData userData = new SQLiteManager.UserData();
        userData.username = username;
        userData.salt = salt;
        userData.password = BCrypt.hashpw(password, salt);
        userData.uuid = uuid;

        SQLiteManager.addUser(userData);
        return uuid;
    }

    public void sendPublicKey(Connection connection, LoginPublicKeyMessage o) {
        Node clientConnection = initNode(connection, o);
        LoginPublicKeyMessage loginPublicKeyMessage = new LoginPublicKeyMessage();
        loginPublicKeyMessage.encodedKey = clientConnection.getPublicKey().getEncoded();
        connection.sendTCP(loginPublicKeyMessage);
        EMLogger.trace("MessagingServer", "Sent Public Key");
    }

    private Node initNode(Connection connection, LoginPublicKeyMessage message) {
        Node clientConnection = new Node();
        try {
            PublicKey publicKey = createPublicKeyFromData(message);
            clientConnection.setReceiverPublicKey(publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            EMLogger.error("MessagingServer", "Error using PublicKey from: " + connection.getRemoteAddressTCP(), e);
        }
        clientConnectionKeys.put(connection, new ClientConnection(clientConnection, false));
        return clientConnection;
    }

    private PublicKey createPublicKeyFromData(LoginPublicKeyMessage message) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory factory = KeyFactory.getInstance("EC");
        return factory.generatePublic(new X509EncodedKeySpec(message.encodedKey));
    }
}
