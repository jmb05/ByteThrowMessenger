package net.jmb19905.messenger.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    /**
     * Loads the config for the Client
     * @param configFilePath the path of the config file
     * @return the ClientConfig
     */
    public static ClientConfig loadClientConfigFile(String configFilePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(configFilePath), ClientConfig.class);
        } catch (IOException e) {
            BTMLogger.warn("ConfigMapper", "Error loading config file. Using Default");
            return new ClientConfig();
        }
    }

    /**
     * Saves the config from the Client
     * @param config the Config that will be saved
     * @param configFilePath the path to the config file
     */
    public static void saveClientConfig(ClientConfig config, String configFilePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(configFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
        } catch (IOException e) {
            BTMLogger.warn("ConfigMapper", "Error saving config file.", e);
        }
    }

    /**
     * Loads the config for the Server
     * @param configFilePath the path of the config file
     * @return the ServerConfig
     */
    public static ServerConfig loadServerConfigFile(String configFilePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(configFilePath), ServerConfig.class);
        } catch (IOException e) {
            BTMLogger.warn("ConfigMapper", "Error loading config file. Using Default");
            return new ServerConfig();
        }
    }

    /**
     * Saves the config from the Server
     * @param config the Config that will be saved
     * @param configFilePath the path to the config file
     */
    public static void saveServerConfig(ServerConfig config, String configFilePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(configFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
        } catch (IOException e) {
            BTMLogger.warn("ConfigMapper", "Error saving config file.", e);
        }
    }

    public static class ClientConfig {

        public ClientConfig() {
        }

        public String theme = "Darcula";
        public String server = "93.159.123.161";
        public int port = 10101;
        public boolean autoLogin = true;

    }

    public static class ServerConfig {

        public ServerConfig() {
        }

        public int port = 10101;

    }

}
