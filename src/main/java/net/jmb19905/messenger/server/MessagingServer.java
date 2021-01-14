package net.jmb19905.messenger.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.*;
import net.jmb19905.messenger.server.userdatabase.SQLiteManager;
import net.jmb19905.messenger.util.EMLogger;
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

    private final HashMap<Connection, ClientConnection> clientConnectionKeys = new HashMap<>();

    public MessagingServer(int port) {
        EMLogger.trace("MessagingServer", "Initializing Server");
        this.port = port;
        server = new Server();

        Kryo kryo = server.getKryo();
        kryo.register(LoginPublicKeyMessage.class);
        kryo.register(byte[].class);
        kryo.register(LoginMessage.class);
        kryo.register(RegisterMessage.class);
        kryo.register(UsernameAlreadyExistMessage.class);
        kryo.register(RegisterSuccessfulMessage.class);
        kryo.register(NotRegisteredMessage.class);
        kryo.register(LoginSuccessMessage.class);
        kryo.register(ConnectWithOtherUserMessage.class);
        kryo.register(DataMessage.class);
        kryo.register(LoginFailedMessage.class);
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
        if(o instanceof LoginPublicKeyMessage){
            onPublicKeyReceived(connection, (LoginPublicKeyMessage) o);
        }else if(o instanceof LoginMessage){
            onLoginReceived(connection, (LoginMessage) o);
        }else if(o instanceof RegisterMessage){
            onRegisterReceived(connection, (RegisterMessage) o);
        }else if(o instanceof ConnectWithOtherUserMessage){
            onReceivedUserConnectWithOther(connection, (ConnectWithOtherUserMessage) o);
        }else if(o instanceof DataMessage){
            onReceivedData(connection, (DataMessage) o);
        }
    }

    private void onReceivedData(Connection connection, DataMessage o){
        Node senderNode = clientConnectionKeys.get(connection).getNode();
        if(clientConnectionKeys.get(connection).isLoggedIn()){
            String sender = clientConnectionKeys.get(connection).getUsername();
            String recipient = senderNode.decrypt(o.username);

            for(Connection recipientConnection : clientConnectionKeys.keySet()){
                if(clientConnectionKeys.get(recipientConnection).isLoggedIn()){
                    if(clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)){
                        o.username = sender;
                        recipientConnection.sendTCP(o);
                        EMLogger.trace("MessagingServer","Passed Data from " + sender + " to " + recipient);
                        return;
                    }
                }
            }
            EMLogger.info("MessagingServer","Recipient: " + recipient + " for data from " + sender + " is offline cannot send data");
            //TODO: add to a queue
        }
    }

    private void onReceivedUserConnectWithOther(Connection connection, ConnectWithOtherUserMessage o){
        Node senderNode = clientConnectionKeys.get(connection).getNode();
        if(clientConnectionKeys.get(connection).isLoggedIn()){
            String sender = clientConnectionKeys.get(connection).getUsername();
            String recipient = senderNode.decrypt(o.userName);
            String publicKeyEncoded = senderNode.decrypt(o.publicKeyEncodedEncrypted);
            for(Connection recipientConnection : clientConnectionKeys.keySet()){
                if(clientConnectionKeys.get(recipientConnection).isLoggedIn()){
                    if(clientConnectionKeys.get(recipientConnection).getUsername().equals(recipient)){
                        o.userName = clientConnectionKeys.get(recipientConnection).getNode().encrypt(sender);
                        o.publicKeyEncodedEncrypted = clientConnectionKeys.get(recipientConnection).getNode().encrypt(publicKeyEncoded);
                        recipientConnection.sendTCP(o);
                        EMLogger.trace("MessagingServer","Passed Connection Request from " + sender + " to " + recipient);
                        return;
                    }
                }
            }
            EMLogger.info("MessagingServer","Recipient: " + recipient + " for connection request from " + sender + " is offline cannot send request");
            //TODO: add to a queue
        }
    }

    private void onRegisterReceived(Connection connection, RegisterMessage o) {
        if(!clientConnectionKeys.get(connection).isLoggedIn()) {
            String username = clientConnectionKeys.get(connection).getNode().decrypt(o.username);
            String password = clientConnectionKeys.get(connection).getNode().decrypt(o.password);
            SQLiteManager.UserData user = SQLiteManager.getUserByName(username);
            if (user == null) {
                //User does not exist create a new one
                UUID uuid = createUser(username, password);
                sendRegisterSuccess(connection, username, uuid);
                clientConnectionKeys.get(connection).setLoggedIn(true);
            } else {
                if (BCrypt.hashpw(password,user.salt).equals(user.password)) {
                    EMLogger.trace("MessagingServer", "Client tried to register instead of login -> logging client in");
                    clientConnectionKeys.get(connection).setUsername(user.username);
                    clientConnectionKeys.get(connection).setLoggedIn(true);
                    connection.sendTCP(new LoginSuccessMessage());
                } else {
                    connection.sendTCP(new UsernameAlreadyExistMessage());
                }
            }
        }else{
            EMLogger.warn("MessagingServer", "Already registered client tried to register");
        }
    }

    private void onLoginReceived(Connection connection, LoginMessage o) {
        Node clientConnection = clientConnectionKeys.get(connection).getNode();
        String username = clientConnection.decrypt(o.username);
        String password = clientConnection.decrypt(o.password);

        SQLiteManager.UserData userData = SQLiteManager.getUserByName(username);
        if(userData == null){
            LoginFailedMessage fail = new LoginFailedMessage();
            fail.cause = "name";
            connection.sendTCP(fail);
        }else{
            if(BCrypt.hashpw(password,userData.salt).equals(userData.password)) {
                clientConnectionKeys.get(connection).setUsername(userData.username);
                clientConnectionKeys.get(connection).setLoggedIn(true);
                connection.sendTCP(new LoginSuccessMessage());
            }else{
                LoginFailedMessage fail = new LoginFailedMessage();
                fail.cause = "pw";
                connection.sendTCP(fail);
            }
        }
    }

    private void onPublicKeyReceived(Connection connection, LoginPublicKeyMessage o) {
        EMLogger.trace("MessagingServer", "Received PublicKey");
        sendPublicKey(connection, o);
    }

    private void sendRegisterSuccess(Connection connection, String username, UUID uuid) {
        RegisterSuccessfulMessage message = new RegisterSuccessfulMessage();
        message.username = username;
        message.uuid = uuid.toString();
        connection.sendTCP(message);
    }

    private UUID createUser(String username, String password) {
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

    private void sendPublicKey(Connection connection, LoginPublicKeyMessage o) {
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
