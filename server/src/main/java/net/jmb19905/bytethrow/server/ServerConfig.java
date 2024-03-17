package net.jmb19905.bytethrow.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import net.jmb19905.util.config.Config;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerConfig extends Config {

    public ServerConfig() {}

    @Override
    public String getName() {
        return "server_config";
    }

    public int port = 10101;
    public boolean securePasswords = true;
    public String loggerLevel = "INFO";

}
