package net.jmb19905.bytethrow.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.jmb19905.util.bootstrapping.DeployState;
import net.jmb19905.util.config.Config;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientConfig extends Config {

    public ClientConfig() {
        this(DeployState.CLIENT);
    }

    @Override
    public String getName() {
        return "client_config";
    }

    public ClientConfig(DeployState state) {
        if (state == DeployState.DEV) {
            server = "localhost";
        } else {
            server = "btm.bennettcraft.com";
        }
    }

    public String theme = "Darcula";
    public String server;
    public int port = 10101;
    public boolean autoLogin = false;
    public String lang = "en_US";
    public String loggerLevel = "INFO";

}
