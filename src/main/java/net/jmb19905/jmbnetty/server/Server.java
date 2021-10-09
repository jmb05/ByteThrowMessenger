package net.jmb19905.jmbnetty.server;

import net.jmb19905.jmbnetty.common.connection.Endpoint;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;

public class Server extends Endpoint {

    private final TcpServerConnection connection;

    public Server(int port) {
        super(port);
        this.connection = new TcpServerConnection(port);
    }

    public TcpServerConnection getConnection() {
        return connection;
    }

    @Override
    public void start() {
        this.connection.start();
    }

    @Override
    public void stop() {
        this.connection.stop();
    }
}
