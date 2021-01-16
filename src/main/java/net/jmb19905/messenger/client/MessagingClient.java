package net.jmb19905.messenger.client;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import net.jmb19905.messenger.client.ui.OptionPanes;
import net.jmb19905.messenger.client.ui.Window;
import net.jmb19905.messenger.crypto.Node;
import net.jmb19905.messenger.messages.*;
import net.jmb19905.messenger.messages.exception.UnsupportedSideException;
import net.jmb19905.messenger.util.EMLogger;
import net.jmb19905.messenger.util.Util;
import net.jmb19905.messenger.util.Variables;

import javax.swing.*;
import java.io.IOException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class MessagingClient extends Listener{

    private final String serverAddress;
    private final int serverPort;
    public UUID uuid;

    public Client client;
    public static final Node thisDevice = new Node();

    public static HashMap<String, Node> otherUsers;
    public static final List<String> connectionRequested = new ArrayList<>();
    @Deprecated
    public static final List<String> connectionToBeVerified = new ArrayList<>();

    private final Thread reconnectionThread;

    public MessagingClient(String serverAddress){

        this.serverAddress = serverAddress;
        this.serverPort = Variables.DEFAULT_PORT;

        reconnectionThread = new Thread(() -> {
            while (true){
                if(!client.isConnected() && !Window.closeRequested){
                    try {
                        client.reconnect();
                    } catch (IOException e) {
                        EMLogger.error("MessagingClient", "Can't reconnect", e);
                        stop(-1);
                    }
                }else if(Window.closeRequested){
                    break;
                }
            }
        });
        init();
    }

    private void init(){
        EMLogger.trace("MessagingClient", "Initializing Client");
        client = new Client();

        Util.registerMessages(client.getKryo());
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
            JOptionPane.showMessageDialog(null, "Error connecting to server! Please check internet connection.", "Connection Failure", JOptionPane.ERROR_MESSAGE);
            stop(-1);
        }
        reconnectionThread.start();
        EMLogger.info("MessagingClient", "Started Client");
    }

    public void stop(int code){
        EMLogger.trace("MessagingClient", "Stopping Client");
        client.stop();
        EMLogger.info("MessagingClient", "Stopped Client");
        if(!EncryptedMessenger.getUsername().equals("") && EncryptedMessenger.getUsername() != null) {
            Util.saveNodes(otherUsers, "userdata/" + EncryptedMessenger.getUsername() + "/other_users.dat");
        }
        EMLogger.close();
        System.exit(code);
    }

    @Override
    public void connected(Connection connection) {
        EMLogger.info("MessagingClient", "Connection established with: " + connection.getRemoteAddressTCP(), null);
        LoginPublicKeyMessage loginPublicKeyMessage = new LoginPublicKeyMessage();
        loginPublicKeyMessage.encodedKey = thisDevice.getPublicKey().getEncoded();
        connection.sendTCP(loginPublicKeyMessage);
        EMLogger.trace("MessagingClient", "Sent PublicKey " + Arrays.toString(loginPublicKeyMessage.encodedKey));
    }

    @Override
    public void disconnected(Connection connection) {
        EMLogger.info("MessagingClient", "Lost Connection");
        connection.close();
    }

    @Override
    public void received(Connection connection, Object o) {
        if(o instanceof EMMessage) {
            try {
                ((EMMessage) o).handleOnClient(connection);
            } catch (UnsupportedSideException e) {
                EMLogger.warn("MessagingClient", "Message received on wrong side", e);
            }
        }
    }

    public void connectWithOtherUser(String username){
        Node node = new Node();
        ConnectWithOtherUserMessage message = new ConnectWithOtherUserMessage();
        message.username = Util.encryptString(thisDevice, username);
        message.publicKeyEncodedEncrypted = thisDevice.encrypt(node.getPublicKey().getEncoded());
        client.sendTCP(message);
        otherUsers.put(username, node);
        connectionRequested.add(username);
    }

    public void sendToOtherUser(String username, String message){
        System.out.println("Trying to send Message to " + username);
        if(otherUsers.get(username) != null) {
            if(otherUsers.get(username).getSharedSecret() != null) {
                DataMessage dataMessage = new DataMessage();
                dataMessage.username = Util.encryptString(thisDevice, username);
                dataMessage.encryptedMessage = Util.encryptString(thisDevice, Util.encryptString(otherUsers.get(username), message));
                client.sendTCP(dataMessage);
            }else{
                EMLogger.warn("MessagingClient", "Cannot send to " + username + ". No SharedSecret Key.");
            }
        }else{
            EMLogger.info("MessagingClient", "Your are not connected with this client - use connect");
        }
    }

    public void login(Connection connection){
        if(!EncryptedMessenger.getUsername().equals("") && !EncryptedMessenger.getPassword().equals("")){
            LoginMessage loginMessage = new LoginMessage();
            loginMessage.username = Util.encryptString(thisDevice, EncryptedMessenger.getUsername());
            loginMessage.password = Util.encryptString(thisDevice, EncryptedMessenger.getPassword());
            connection.sendTCP(loginMessage);
        }else {
            OptionPanes.OutputValue value = OptionPanes.showLoginDialog((e) -> register(connection));
            if (value.id == OptionPanes.OutputValue.CANCEL_OPTION) {
                System.exit(0);
            } else if (value.id == OptionPanes.OutputValue.CONFIRM_OPTION) {
                LoginMessage loginMessage = new LoginMessage();
                loginMessage.username = Util.encryptString(thisDevice, value.values[0]);
                loginMessage.password = Util.encryptString(thisDevice, value.values[1]);
                connection.sendTCP(loginMessage);
                EncryptedMessenger.setUserData(value.values[0], value.values[1]);
            }
        }
    }

    public void register(Connection connection){
        OptionPanes.OutputValue value = OptionPanes.showRegisterDialog((e) -> login(connection));
        if(value.id == OptionPanes.OutputValue.CANCEL_OPTION){
            stop(0);
        }else if(value.id == OptionPanes.OutputValue.CONFIRM_OPTION) {
            RegisterMessage registerMessage = new RegisterMessage();
            registerMessage.username = Util.encryptString(thisDevice, value.values[0]);
            registerMessage.password = Util.encryptString(thisDevice, value.values[1]);
            connection.sendTCP(registerMessage);
            EMLogger.trace("MessagingClient", "Sent Registering Data... Waiting for response");
        }
    }

    public static void initOtherUsers(){
        otherUsers = Util.loadNodes("userdata/"+EncryptedMessenger.getUsername()+"/other_users.dat");
    }

    public void setPublicKey(byte[] encodedKey) {
        try {
            PublicKey publicKey = Util.createPublicKeyFromData(encodedKey);
            EMLogger.trace("MessagingClient", "Received PublicKey " + Arrays.toString(publicKey.getEncoded()));
            if (publicKey != null) {
                MessagingClient.thisDevice.setReceiverPublicKey(publicKey);
            }
        }catch (InvalidKeySpecException e){
            EMLogger.error("MessagingServer", "Error setting PublicKey. Key is invalid.");
        }
    }

    public void setPublicKey(byte[] decryptedEncodedKey, Node node) {
        try {
            PublicKey publicKey = Util.createPublicKeyFromData(decryptedEncodedKey);
            EMLogger.trace("MessagingClient", "Received PublicKey");
            if(publicKey != null) {
                node.setReceiverPublicKey(publicKey);
            }
        }catch (InvalidKeySpecException e){
            EMLogger.error("MessagingServer", "Error setting PublicKey. Key is invalid.");
        }
    }

}
