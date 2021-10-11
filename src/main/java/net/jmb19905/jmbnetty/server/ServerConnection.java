package net.jmb19905.jmbnetty.server;

import net.jmb19905.jmbnetty.common.connection.AbstractConnection;

public abstract class ServerConnection extends AbstractConnection {

    public ServerConnection(int port){
        super();
        this.port = port;
    }

}
