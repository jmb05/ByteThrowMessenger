package net.jmb19905.test;

import net.jmb19905.messenger.util.ConfigManager;

public class Test {

    public static void main(String[] args) {
        ConfigManager.saveConfig(new ConfigManager.Config(), "client_config.json");
    }
}
