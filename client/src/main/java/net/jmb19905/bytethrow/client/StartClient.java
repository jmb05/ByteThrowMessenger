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

import net.jmb19905.util.Logger;
import net.jmb19905.util.bootstrapping.Bootstrap;
import net.jmb19905.util.bootstrapping.DeployState;
import net.jmb19905.util.bootstrapping.Version;

public class StartClient {

    public static ClientConfig config;

    public static ClientManager manager;
    public static GUIManager guiManager;

    public static Version version;

    public static boolean isDevEnv = false;
    public static DeployState deployState = DeployState.CLIENT;

    /**
     * Starts the Client and it's Window
     */
    public static void main(String[] args) {
        Bootstrap clientBootstrap = new ClientBootstrap(args)
                .useVersion()
                .loggerSignature("client")
                .config("bytethrowmessenger", ClientConfig.class)
                .bootstrap();

        version = clientBootstrap.getVersion();
        isDevEnv = clientBootstrap.getDeployState() == DeployState.DEV;
        config = (ClientConfig) clientBootstrap.getConfig();
        deployState = clientBootstrap.getDeployState();

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
