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

package net.jmb19905.bytethrow.client;

import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.bytethrow.client.util.ThemeManager;
import net.jmb19905.bytethrow.common.RegistryManager;
import net.jmb19905.bytethrow.common.Version;
import net.jmb19905.bytethrow.common.util.ConfigManager;
import net.jmb19905.bytethrow.common.util.Util;
import net.jmb19905.util.Logger;
import net.jmb19905.util.ShutdownManager;

import java.util.Arrays;

public class StartClient {

    public static ConfigManager.ClientConfig config;

    public static ClientManager manager;
    public static GUIManager guiManager;

    public static Version version;

    public static boolean isDevEnv = false;

    /**
     * Starts the Client and it's Window
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            isDevEnv = Arrays.asList(args).contains("dev");
        }

        Logger.initLogFile(false);
        version = Util.loadVersion(isDevEnv);
        Logger.info("Starting ByteThrow Messenger Client - Version: " + version);
        if (isDevEnv) {
            Logger.info("Is in DEV Environment");
        }
        ShutdownManager.addCleanUp(() -> {
            ConfigManager.saveClientConfig();
            Logger.close();
        });
        RegistryManager.registerPackets();
        ConfigManager.init();
        config = ConfigManager.loadClientConfig();
        Logger.info("Loaded configs from: " + ConfigManager.getConfigPath());
        Logger.setLevel(isDevEnv ? Logger.Level.TRACE : Logger.Level.valueOf(config.loggerLevel));
        Localisation.reload();
        //On Some Linux Systems Java doesn't automatically use Anti-Aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        ThemeManager.init();
        try {
            manager = new ClientManager(config.server, config.port);
            guiManager = new GUIManager();
            manager.initGuiListeners();
            guiManager.showLoading(true);
            manager.start();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

}
