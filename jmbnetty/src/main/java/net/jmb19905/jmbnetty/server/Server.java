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

package net.jmb19905.jmbnetty.server;

import net.jmb19905.jmbnetty.common.connection.Endpoint;
import net.jmb19905.jmbnetty.common.connection.event.ConnectedEventListener;
import net.jmb19905.jmbnetty.common.connection.event.DisconnectedEventListener;
import net.jmb19905.jmbnetty.common.connection.event.ErrorEventListener;
import net.jmb19905.jmbnetty.server.tcp.TcpServerConnection;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;

public class Server extends Endpoint {

    private final TcpServerConnection connection;

    public Server(int port) {
        super(port);
        this.connection = new TcpServerConnection(port);
    }

    public TcpServerConnection getConnection() {
        return connection;
    }

    @Override
    public void start() {
        this.connection.start();
    }

    @Override
    public void stop() {
        this.connection.stop();
    }

    public void addConnectedEventListener(ConnectedEventListener listener) {
        connection.addEventListener(listener);
    }

    public void addDisconnectedEventListener(DisconnectedEventListener listener) {
        connection.addEventListener(listener);
    }

    public void addErrorEventListener(ErrorEventListener listener) {
        connection.addEventListener(listener);
    }

    public void removeServerHandler(TcpServerHandler serverHandler) {
        connection.getClientConnections().remove(serverHandler);
    }

}
