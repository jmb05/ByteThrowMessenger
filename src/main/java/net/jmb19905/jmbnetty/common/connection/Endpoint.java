package net.jmb19905.jmbnetty.common.connection;

import net.jmb19905.util.ShutdownManager;

public abstract class Endpoint {

    protected final int port;

    public Endpoint(int port){
        this.port = port;
        ShutdownManager.addCleanUp(this::stop);
    }

    public abstract void start();

    public abstract void stop();

}
