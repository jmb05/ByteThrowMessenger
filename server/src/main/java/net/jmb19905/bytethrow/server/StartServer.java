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

import net.jmb19905.bytethrow.server.util.ClientDataFilesManager;
import net.jmb19905.bytethrow.server.util.ServerBootstrap;
import net.jmb19905.util.bootstrapping.Bootstrap;
import net.jmb19905.util.bootstrapping.DeployState;
import net.jmb19905.util.bootstrapping.Version;

public class StartServer {

    public static Version version;
    public static ServerConfig config;

    public static ServerManager manager;

    public static boolean isDevEnv;

    /**
     * Starts the server
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        Bootstrap bootstrap = new ServerBootstrap(args)
                .useVersion()
                .config("bytethrowmessenger", ServerConfig.class)
                .loggerSignature("server")
                .bootstrap();

        version = bootstrap.getVersion();
        isDevEnv = bootstrap.getDeployState() == DeployState.DEV_DEPLOY;
        config = (ServerConfig) bootstrap.getConfig();

        manager = new ServerManager(config.port);
        ClientDataFilesManager.loadChats();
        manager.start();
    }
}
