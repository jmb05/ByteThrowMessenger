package net.jmb19905.messenger.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    public static Config loadConfigFile(String configFilePath){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(configFilePath), Config.class);
        } catch (IOException e) {
            EMLogger.warn("ConfigMapper", "Error loading config file. Using Default");
            return new Config();
        }
    }

    public static void saveConfig(Config config, String configFilePath){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(configFilePath), config);
        } catch (IOException e) {
            EMLogger.warn("ConfigMapper", "Error loading config file. Using Default");
        }
    }

    public static class Config{

        public Config(){}

        public String theme = "Darcula";
        public boolean autoLogin = true;

    }

}
