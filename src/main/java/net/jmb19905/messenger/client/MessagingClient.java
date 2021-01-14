package net.jmb19905.messenger.client;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.jmb19905.messenger.client.ui.OptionPanes;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.*;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;

import javax.swing.*;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MessagingClient extends Listener{

    private final String serverAddress;
    private final int serverPort;
    private UUID uuid;

    private Client client;
    private static final Node thisDevice = new Node();

    private static HashMap<String, Node> otherUsers;
    private static List<String> connectionRequested = new ArrayList<>();

    public MessagingClient(String serverAddress, int serverPort){
        otherUsers = Util.loadNodes("other_users.dat");
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;

        init();
    }

    private void init(){
        EMLogger.trace("MessagingClient", "Initializing Client");
        client = new Client();

        Kryo kryo = client.getKryo();
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
        EMLogger.trace("MessagingClient", "Registered Messages");

        client.addListener(this);
        EMLogger.trace("MessagingClient", "Added Listener");
        EMLogger.info("MessagingClient", "Initialized Client");
    }

    public void start(){
        EMLogger.trace("MessagingClient", "Starting Client");
        new Thread(client).start();
        try {
            client.connect(5000, serverAddress, serverPort, serverPort + 1);
        } catch (IOException e) {
            EMLogger.error("MessagingClient", "Error establishing connection", e);
            System.exit(-1);
        }
        EMLogger.info("MessagingClient", "Started Client");
    }

    public void stop(){
        EMLogger.trace("MessagingClient", "Stopping Client");
        client.stop();
        EMLogger.info("MessagingClient", "Stopped Client");
        Util.saveNodes(otherUsers, "other_users.dat");
        EMLogger.close();
        System.exit(0);
    }

    @Override
    public void connected(Connection connection) {
        EMLogger.info("MessagingClient", "Connection established with: " + connection.getRemoteAddressTCP(), null);
        LoginPublicKeyMessage loginPublicKeyMessage = new LoginPublicKeyMessage();
        loginPublicKeyMessage.encodedKey = thisDevice.getPublicKey().getEncoded();
        connection.sendTCP(loginPublicKeyMessage);
        EMLogger.trace("MessagingClient", "Sent PublicKey");
    }

    @Override
    public void disconnected(Connection connection) {
        EMLogger.info("MessagingClient", "Lost Connection");
        connection.close();
        client.stop();
        start();
    }

    @Override
    public void received(Connection connection, Object o) {
        if(o instanceof LoginPublicKeyMessage){
            onPublicKeyReceived(connection, (LoginPublicKeyMessage) o);
        }else if(o instanceof UsernameAlreadyExistMessage){
            onUsernameAlreadyExists(connection);
        }else if(o instanceof RegisterSuccessfulMessage){
            onRegisterSuccess((RegisterSuccessfulMessage) o);
        }else if(o instanceof NotRegisteredMessage){
            onNotRegistered(connection);
        }else if(o instanceof LoginSuccessMessage){
            onLoginSuccess();
        }else if(o instanceof ConnectWithOtherUserMessage){
            EMLogger.trace("MessagingClient", "Received ConnectWithOtherUserMessage");
            onReceivedOtherUserConnect((ConnectWithOtherUserMessage) o);
        }else if(o instanceof DataMessage){
            onReceivedDataMessage((DataMessage) o);
        }else if(o instanceof LoginFailedMessage){
            onLoginFailed(connection, ((LoginFailedMessage) o).cause);
        }
    }

    private void onLoginFailed(Connection connection, String cause){
        JOptionPane.showMessageDialog(null, "Could not log in! " + (cause.equals("pw") ? "Password " : "Username ") + "was incorrect.", "Wrong credentials", JOptionPane.ERROR_MESSAGE);
        login(connection);
    }

    private void onReceivedDataMessage(DataMessage o){
        String sender = thisDevice.decrypt(o.username);
        if(otherUsers.get(sender).getSharedSecret() != null){
            String message = thisDevice.decrypt(otherUsers.get(sender).decrypt(o.encryptedMessage));
            System.out.println(sender + " : " + message);
        }else {
            EMLogger.warn("MessagingClient", "Received Message from unconnected client");
        }
    }

    private void onReceivedOtherUserConnect(ConnectWithOtherUserMessage o){
        String username = o.userName;
        if(otherUsers.get(username) != null){
            EMLogger.trace("MessagingClient", username + " is already connected");
        }else{
            Node node = new Node();
            setPublicKey(o, node);
            otherUsers.put(username, node);
            EMLogger.trace("MessagingClient", "Added " + username + " to connected users");
            if(connectionRequested.contains(username)){
                EMLogger.info("MessagingClient", username + " has responded");
                connectionRequested.remove(username);
            }else{
                ConnectWithOtherUserMessage message = new ConnectWithOtherUserMessage();
                message.userName = thisDevice.encrypt(username);
                message.publicKeyEncodedEncrypted = thisDevice.encrypt(new String(thisDevice.getPublicKey().getEncoded()));
                client.sendTCP(message);
            }
        }
    }

    private void onLoginSuccess() {
        EMLogger.info("MessagingClient", "Logged in successfully");
        EncryptedMessenger.writeUserData();
        EncryptedMessenger.setLoggedIn(true);
        EncryptedMessenger.window.appendLine("Logged in as " + EncryptedMessenger.getUsername());
    }

    private void onPublicKeyReceived(Connection connection, LoginPublicKeyMessage o) {
        EMLogger.trace("MessagingClient", "received Server login response");
        setPublicKey(o);
        login(connection);
    }

    private void onUsernameAlreadyExists(Connection connection) {
        EncryptedMessenger.setUserData("", "");
        EncryptedMessenger.setLoggedIn(false);
        register(connection);
    }

    private void onRegisterSuccess(RegisterSuccessfulMessage o) {
        uuid = UUID.fromString(o.uuid);
        EncryptedMessenger.writeUserData();
        EncryptedMessenger.setLoggedIn(true);
        EMLogger.info("MessagingClient", "Registered Successful");
    }

    public void connectWithOtherUser(String username){
        Node node = new Node();
        ConnectWithOtherUserMessage message = new ConnectWithOtherUserMessage();
        message.userName = thisDevice.encrypt(username);
        message.publicKeyEncodedEncrypted = thisDevice.encrypt(new String(node.getPublicKey().getEncoded()));
        client.sendTCP(message);
        otherUsers.put(username, node);
        connectionRequested.add(username);
    }

    public void sendToOtherUser(String username, String message){
        if(otherUsers.get(username) != null) {
            if(otherUsers.get(username).getSharedSecret() != null) {
                DataMessage dataMessage = new DataMessage();
                dataMessage.username = thisDevice.encrypt(username);
                dataMessage.encryptedMessage = thisDevice.encrypt(otherUsers.get(username).encrypt(message));
            }else{
                EMLogger.warn("MessagingClient", "Cannot send to " + username + ". No SharedSecret Key.");
            }
        }else{
            EMLogger.info("MessagingClient", "Your are not connected with this client - use connect");
        }
    }

    private void onNotRegistered(Connection connection) {
        int jop = JOptionPane.showConfirmDialog(null, "Login failed. If you have no account you have to register.\nDo you want to register?", "Login failed", JOptionPane.YES_NO_CANCEL_OPTION);
        if(jop == JOptionPane.YES_OPTION){
            register(connection);
        }else if(jop == JOptionPane.NO_OPTION){
            login(connection);
        }else{
            System.exit(0);
        }
    }

    private void login(Connection connection){
        if(!EncryptedMessenger.getUsername().equals("") && !EncryptedMessenger.getPassword().equals("")){
            LoginMessage loginMessage = new LoginMessage();
            loginMessage.username = thisDevice.encrypt(EncryptedMessenger.getUsername());
            loginMessage.password = thisDevice.encrypt(EncryptedMessenger.getPassword());
            connection.sendTCP(loginMessage);
        }else {
            OptionPanes.OutputValue value = OptionPanes.showLoginDialog((e) -> {
                register(connection);
            });
            if (value.id == OptionPanes.OutputValue.CANCEL_OPTION) {
                System.exit(0);
            } else if (value.id == OptionPanes.OutputValue.CONFIRM_OPTION) {
                LoginMessage loginMessage = new LoginMessage();
                loginMessage.username = thisDevice.encrypt(value.values[0]);
                loginMessage.password = thisDevice.encrypt(value.values[1]);
                connection.sendTCP(loginMessage);
                EncryptedMessenger.setUserData(value.values[0], value.values[1]);
            }
        }
    }

    private void register(Connection connection){
        OptionPanes.OutputValue value = OptionPanes.showRegisterDialog((e) -> {
            login(connection);
        });
        if(value.id == OptionPanes.OutputValue.CANCEL_OPTION){
            System.exit(0);
        }else if(value.id == OptionPanes.OutputValue.CONFIRM_OPTION) {
            RegisterMessage registerMessage = new RegisterMessage();
            registerMessage.username = thisDevice.encrypt(value.values[0]);
            registerMessage.password = thisDevice.encrypt(value.values[1]);
            connection.sendTCP(registerMessage);
            EMLogger.trace("MessagingClient", "Sent Registering Data... Waiting for response");
        }
    }

    private void setPublicKey(LoginPublicKeyMessage o) {
        PublicKey publicKey = Util.createPublicKeyFromData(o.encodedKey);
        EMLogger.trace("MessagingClient", "Received PublicKey");
        if(publicKey != null) {
            MessagingClient.thisDevice.setReceiverPublicKey(publicKey);
        }
    }

    private void setPublicKey(ConnectWithOtherUserMessage o, Node node) {
        PublicKey publicKey = Util.createPublicKeyFromData(thisDevice.decrypt(o.publicKeyEncodedEncrypted).getBytes());
        EMLogger.trace("MessagingClient", "Received PublicKey");
        if(publicKey != null) {
            node.setReceiverPublicKey(publicKey);
        }
    }

}
