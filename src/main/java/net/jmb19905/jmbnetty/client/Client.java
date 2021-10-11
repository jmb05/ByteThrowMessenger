package net.jmb19905.jmbnetty.client;

import net.jmb19905.jmbnetty.client.tcp.TcpClientConnection;
import net.jmb19905.jmbnetty.common.connection.Endpoint;

public class Client extends Endpoint {

    private final TcpClientConnection connection;

    public Client(int port, String address) {
        super(port);
        this.connection = new TcpClientConnection(port, address);
    }

    @Override
    public void start() {
        this.connection.start();
    }

    @Override
    public void stop() {
        this.connection.stop();
    }

    public TcpClientConnection getConnection() {
        return connection;
    }
}
