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

package net.jmb19905.bytethrow.server;


import net.jmb19905.bytethrow.common.RegistryManager;
import net.jmb19905.bytethrow.common.Version;
import net.jmb19905.bytethrow.common.util.ConfigManager;
import net.jmb19905.bytethrow.common.util.Util;
import net.jmb19905.bytethrow.server.database.DatabaseManager;
import net.jmb19905.bytethrow.server.networking.ServerManager;
import net.jmb19905.bytethrow.server.util.ClientDataFilesManager;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

public class StartServer {

    public static Version version;
    public static ConfigManager.ServerConfig config;

    public static ServerManager manager;

    public static boolean isDevEnv;

    /**
     * Starts the server
     * @param args program arguments
     */
    public static void main(String[] args) {
        isDevEnv = args.length > 0;
        Logger.setLevel(isDevEnv ? Logger.Level.TRACE : Logger.Level.INFO);
        Arrays.stream(args).filter(s -> s.startsWith("Level=")).forEach(s -> Logger.setLevel(Logger.Level.valueOf(s.split("=")[1].toUpperCase(Locale.ROOT))));
        Logger.initLogFile(true);
        version = Util.loadVersion(isDevEnv);
        Logger.info("Starting ByteThrow Messenger Server - Version: " + version);
        if(isDevEnv){
            Logger.info("Is in DEV Environment");
        }
        RegistryManager.registerAll();
        config = ConfigManager.loadServerConfigFile("config/server_config.json");
        Logger.info("Loaded configs");

        DatabaseManager.open();
        ShutdownManager.addCleanUp(DatabaseManager::close);

        manager = new ServerManager(config.port);
        ClientDataFilesManager.loadChats();
        manager.start();
    }
}
