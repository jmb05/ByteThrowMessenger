/*
    A simple Messenger written in Java
    Copyright (C) 2020-2021  Jared M. Bennett

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package net.jmb19905.bytethrow.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.util.Logger;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private static String configPath;

    public static void init(){
        if(!StartClient.isDevEnv){
            if(System.getProperty("os.name").equals("Linux")){
                configPath = Util.getUserHome() + "/.config/bytethrowmessenger/";
                return;
            }
        }
        configPath = "config/";
    }

    public static void setConfigPath(String configPath) {
        ConfigManager.configPath = configPath;
    }

    public static String getConfigPath(){
        return configPath;
    }

    public static ClientConfig loadClientConfig(){
        return loadClientConfigFile(getConfigPath() + "client_config.json");
    }

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
            Logger.warn(e, "Error reading config... writing new one");
            if(saveClientConfig(new ClientConfig(), configFilePath)){
                return loadClientConfigFile(configFilePath);
            }
        }
        return new ClientConfig();
    }

    public static void saveClientConfig(){
        saveClientConfig(StartClient.config, getConfigPath() + "client_config.json");
    }

    /**
     * Saves the config from the Client
     * @param config the Config that will be saved
     * @param configFilePath the path to the config file
     */
    public static boolean saveClientConfig(ClientConfig config, String configFilePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(configFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
        } catch (IOException e) {
            Logger.warn(e, "Error saving config file.");
            return false;
        } catch (NullPointerException e){
            Logger.warn(e, "Error saving config file");
        }
        return true;
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
            if(saveServerConfig(new ServerConfig(), configFilePath)){
                return loadServerConfigFile(configFilePath);
            }
        }
        return new ServerConfig();
    }

    /**
     * Saves the config from the Server
     * @param config the Config that will be saved
     * @param configFilePath the path to the config file
     */
    public static boolean saveServerConfig(ServerConfig config, String configFilePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            File file = new File(configFilePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
        }catch (IOException e) {
            Logger.warn(e, "Error saving config file: " + configFilePath);
            return false;
        }
        return true;
    }

    public static class ClientConfig {

        public ClientConfig() {
            server = StartClient.isDevEnv ? "localhost" : "btm.bennettcraft.com";
        }

        public String theme = "Darcula";
        public String server;
        public int port = 10101;
        public boolean autoLogin = false;
        public String lang = "en_US";

    }

    public static class ServerConfig {

        public ServerConfig() {
        }

        public int port = 10101;
        public boolean securePasswords = true;

    }

}
