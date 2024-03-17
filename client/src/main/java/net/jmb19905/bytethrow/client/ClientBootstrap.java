package net.jmb19905.bytethrow.client;

import net.jmb19905.bytethrow.client.util.ThemeManager;
import net.jmb19905.util.Localisation;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;
import net.jmb19905.util.bootstrapping.Bootstrap;
import net.jmb19905.util.bootstrapping.DeployState;
import net.jmb19905.util.config.ConfigManager;

public class ClientBootstrap extends Bootstrap {
    public ClientBootstrap(String[] args) {
        super(args);
    }

    @Override
    public ClientBootstrap bootstrap() {
        super.bootstrap();

        if (deployState == DeployState.DEPLOY || deployState == DeployState.DEV_DEPLOY) {
            Logger.fatal("Invalid deploy state: " + deployState);
            return null;
        }

        Logger.info("Starting ByteThrow Messenger Client - Version: " + version);
        if(deployState == DeployState.DEV) {
            Logger.info("Is in DEV Environment");
            Logger.setLevel(Logger.Level.TRACE);
        } else Logger.setLevel(Logger.Level.valueOf(((ClientConfig) config).loggerLevel));

        ClientRegistryManager.registerPackets();
        ClientRegistryManager.registerStates();

        Localisation.reload(((ClientConfig) config).lang);

        //On Some Linux Systems Java doesn't automatically use Anti-Aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        ThemeManager.init((ClientConfig) config);

        ShutdownManager.addCleanupLast(() -> ConfigManager.saveConfigFile(config));

        return this;
    }

}
