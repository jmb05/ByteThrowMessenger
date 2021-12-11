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

package net.jmb19905.jmbnetty.client;

import net.jmb19905.jmbnetty.client.tcp.TcpClientConnection;
import net.jmb19905.jmbnetty.common.connection.Endpoint;
import net.jmb19905.jmbnetty.common.connection.event.ConnectedEventListener;
import net.jmb19905.jmbnetty.common.connection.event.DisconnectedEventListener;
import net.jmb19905.jmbnetty.common.connection.event.ErrorEventListener;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.utility.NetworkUtility;

public class Client extends Endpoint {

    private final TcpClientConnection connection;

    public Client(int port, String address) {
        super(port);
        this.connection = new TcpClientConnection(port, address);
    }

    public TcpClientConnection getConnection() {
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

    public void send(Packet packet) {
        NetworkUtility.sendTcp(connection.getChannel(), packet, connection.getClientHandler().getEncryption());
    }
}
