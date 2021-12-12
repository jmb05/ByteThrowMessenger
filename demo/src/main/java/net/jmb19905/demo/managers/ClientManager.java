package net.jmb19905.demo.managers;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.demo.gui.Window;
import net.jmb19905.demo.gui.events.MessageSendEventListener;
import net.jmb19905.demo.packet.KeyExchangePacket;
import net.jmb19905.demo.packet.MessagePacket;
import net.jmb19905.jmbnetty.client.Client;
import net.jmb19905.jmbnetty.client.tcp.TcpClientConnection;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.utility.NetworkUtility;
import net.jmb19905.util.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ClientManager extends EndpointManager {

    private final Client client;
    private Window window = null;

    public ClientManager(String serverAddress) {
        client = new Client(21212, serverAddress);
        client.addConnectedEventListener(evt -> {
            Logger.info("Connected");
            TcpClientConnection clientConnection = (TcpClientConnection) evt.getSource();
            SocketChannel channel = clientConnection.getChannel();

            KeyExchangePacket packet = new KeyExchangePacket();
            packet.key = clientConnection.getClientHandler().getPublicKey().getEncoded();

            NetworkUtility.sendTcp(channel, packet, null);
        });
        client.addDisconnectedEventListener(evt -> {
            if(window != null){
                window.setFieldEnabled(false);
                window.appendMessage("", "Disconnected");
            }
        });
        client.addErrorEventListener(evt -> JOptionPane.showMessageDialog(window, evt.getCause(), "Error", JOptionPane.ERROR_MESSAGE));
        start();
    }

    protected void start() {
        window = new Window("Client");
        window.addEventListener((MessageSendEventListener) evt -> {
            Encryption encryption = client.getConnection().getClientHandler().getEncryption();
            SocketChannel channel = client.getConnection().getChannel();
            sendMessage(evt.getMessage(), channel, encryption);
        });
        client.start();
    }

    public void stop() {
        client.stop();
    }

    @Nullable
    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public void sendMessage(String message, Channel channel, Encryption encryption) {
        MessagePacket messagePacket = new MessagePacket();
        messagePacket.message = message;

        client.send(messagePacket);
        Logger.info("Sent message: " + message);
    }
}
