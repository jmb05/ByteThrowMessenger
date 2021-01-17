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

    public static HashMap<String, ChatHistory> otherUsers;
    public static final List<String> connectionRequested = new ArrayList<>();
    @Deprecated
    public static final List<String> connectionToBeVerified = new ArrayList<>();

    private final Thread reconnectionThread;

    public MessagingClient(String serverAddress, int port){

        this.serverAddress = serverAddress;
        this.serverPort = port;

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
            Util.saveNodes(otherUsers);
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
        ChatHistory chatHistory = new ChatHistory(username, node);
        otherUsers.put(username, chatHistory);
        connectionRequested.add(username);
    }

    public boolean sendToOtherUser(String username, String message){
        ChatHistory chatHistory = otherUsers.get(username);
        if(chatHistory != null && chatHistory.getNode() != null) {
            if(chatHistory.getNode().getSharedSecret() != null) {
                DataMessage dataMessage = new DataMessage();
                dataMessage.username = Util.encryptString(thisDevice, username);
                dataMessage.encryptedMessage = Util.encryptString(thisDevice, Util.encryptString(chatHistory.getNode(), message));
                client.sendTCP(dataMessage);
                chatHistory.addMessage(EncryptedMessenger.getUsername(), message);
                return true;
            }else{
                EMLogger.warn("MessagingClient", "Cannot send to " + username + ". No SharedSecret Key.");
            }
        }else{
            EMLogger.info("MessagingClient", "Your are not connected with this client - use connect");
        }
        return false;
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
        otherUsers = Util.loadNodes();
        for(String key : otherUsers.keySet()){
            ChatHistory chatHistory = otherUsers.get(key);
            if(key.equals(chatHistory.getName())) {
                addChatHistory(chatHistory);
            }else{
                EMLogger.warn("MessagingClient", "Error parsing ChatHistory -> wrong username");
            }
        }
    }

    private static void addChatHistory(ChatHistory chatHistory) {
        for(String rawMessage : chatHistory.getMessages()){
            String[] parts = rawMessage.split(":");
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=1;i<parts.length;i++){
                stringBuilder.append(parts[i]);
            }
            EncryptedMessenger.window.appendLine("<" + parts[0] + "> " + stringBuilder.toString());
        }
    }

    public void setPublicKey(byte[] encodedKey) {
        try {
            PublicKey publicKey = Util.createPublicKeyFromData(encodedKey);
            EMLogger.trace("MessagingClient", "Received PublicKey " + Arrays.toString(publicKey.getEncoded()));
            if (publicKey != null) {
                MessagingClient.thisDevice.setReceiverPublicKey(publicKey);
            }
        }catch (InvalidKeySpecException | NullPointerException e){
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
