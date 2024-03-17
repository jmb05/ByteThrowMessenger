package net.jmb19905.bytethrow.server.util;

import net.jmb19905.bytethrow.server.ServerConfig;
import net.jmb19905.bytethrow.server.ServerRegistryManager;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;
import net.jmb19905.util.bootstrapping.Bootstrap;
import net.jmb19905.util.bootstrapping.DeployState;
import net.jmb19905.util.commands.CommandManager;

public class ServerBootstrap extends Bootstrap {
    public ServerBootstrap(String[] args) {
        super(args);
    }

    @Override
    public ServerBootstrap bootstrap() {
        super.bootstrap();

        if (deployState == DeployState.CLIENT) {
            Logger.fatal("Invalid deploy state: " + deployState);
            return null;
        }

        Logger.info("Starting ByteThrow Messenger Server - Version: " + version);
        Logger.info("""
                ByteThrow Messenger  Copyright (C) 2020-2023  Jared M. Bennett
                This program comes with ABSOLUTELY NO WARRANTY; for details type `help --warranty'.
                This is free software, and you are welcome to redistribute it
                under certain conditions; type `help --conditions' for details.
                """);

        if (deployState == DeployState.DEV) {
            Logger.info("Is in local DEV Environment");
        } else if (deployState == DeployState.DEV_DEPLOY) {
            Logger.info("Is in remote DEV Environment");
        }

        ServerRegistryManager.registerPackets();
        ServerRegistryManager.registerStates();
        ServerRegistryManager.registerCommands();

        Logger.setLevel(deployState == DeployState.DEV_DEPLOY ? Logger.Level.TRACE : Logger.Level.valueOf(((ServerConfig) config).loggerLevel));

        CommandManager.init();
        ShutdownManager.addCleanupLast(CommandManager::close);

        DatabaseManager.open();
        ShutdownManager.addCleanupLast(DatabaseManager::close);

        return this;
    }
}
