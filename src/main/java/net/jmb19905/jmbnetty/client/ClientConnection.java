package net.jmb19905.jmbnetty.client;

import net.jmb19905.jmbnetty.common.connection.AbstractConnection;

public abstract class ClientConnection extends AbstractConnection {

    private final String remoteAddress;

    public ClientConnection(int port, String remoteAddress){
        super();
        this.port = port;
        this.remoteAddress = remoteAddress;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
}
