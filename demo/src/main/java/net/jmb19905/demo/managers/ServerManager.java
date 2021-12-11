package net.jmb19905.demo.managers;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import net.jmb19905.demo.Demo;
import net.jmb19905.demo.gui.Window;
import net.jmb19905.demo.gui.events.MessageSendEventListener;
import net.jmb19905.demo.packet.MessagePacket;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.server.Server;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.utility.NetworkUtility;
import net.jmb19905.util.Logger;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ServerManager extends EndpointManager {

    private final Server server;
    private Window window = null;

    public ServerManager() {
        server = new Server(21212);
        server.addConnectedEventListener(evt -> server.getConnection().getClientConnections().keySet().stream().findAny().ifPresent(serverHandler -> {
            Logger.info("Connected");
            SocketChannel channel = ((TcpServerConnection) evt.getSource()).getClientConnections().get(serverHandler);
            Logger.info("Client: \"" + channel.remoteAddress() + "\" is now connected");
        }));
        server.addDisconnectedEventListener(evt -> {
            Window window = Demo.manager.getWindow();
            if(window != null){
                window.setFieldEnabled(false);
                window.appendMessage("", "Disconnected");
            }
        });
        server.addErrorEventListener(evt -> JOptionPane.showMessageDialog(window, evt.getCause(), "Error", JOptionPane.ERROR_MESSAGE));
        start();
    }

    protected void start() {
        this.window = new Window("Server");
        window.addEventListener((MessageSendEventListener) evt ->
        server.getConnection().getClientConnections().keySet().stream().findFirst().ifPresentOrElse(serverHandler -> {
            Encryption encryption = serverHandler.getEncryption();
            SocketChannel channel = server.getConnection().getClientConnections().get(serverHandler);
            sendMessage(evt.getMessage(), channel, encryption);
        }, () -> window.appendMessage("", "Client not online")));
        server.start();
        window.appendMessage("", "Waiting for Client");
    }

    public void stop() {
        server.stop();
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

        NetworkUtility.sendTcp(channel, messagePacket, encryption);
        Logger.info("Sent message: " + message);
    }
}
