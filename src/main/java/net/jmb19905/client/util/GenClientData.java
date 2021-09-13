package net.jmb19905.client.util;

import net.jmb19905.common.util.ConfigManager;

import java.io.File;

public class GenClientData {

    public static void main(String[] args) {
        System.out.print("Generating Log Directory...   ");
        System.out.print((genLogDir() ? "Success" : "Failed") + "\n");
        System.out.print("Generating UserData Directory...  ");
        System.out.print((genUserDataDir() ? "Success" : "Failed") + "\n");
        System.out.print("Generating Config...  ");
        System.out.print((genConfig() ? "Success" : "Failed") + "\n");
    }

    public static boolean genLogDir(){
        File logDir = new File("logs");
        if(!logDir.exists()) {
            return logDir.mkdirs();
        }
        return true;
    }

    public static boolean genUserDataDir(){
        File userDataDir = new File("userdata");
        if(!userDataDir.exists()) {
            return userDataDir.mkdirs();
        }
        return true;
    }

    public static boolean genConfig(){
        String configFilePath = ConfigManager.getConfigPath() + "client_config.json";
        File configFile = new File(configFilePath);
        if(!configFile.exists()) {
            return ConfigManager.saveClientConfig(new ConfigManager.ClientConfig(), configFilePath);
        }
        return true;
    }

}
