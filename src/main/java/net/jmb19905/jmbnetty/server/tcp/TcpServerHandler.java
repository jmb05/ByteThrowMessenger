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

package net.jmb19905.jmbnetty.server.tcp;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.jmbnetty.common.exception.IllegalSideException;
import net.jmb19905.jmbnetty.common.handler.AbstractChannelHandler;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.ServerConnection;
import net.jmb19905.util.Logger;

public class TcpServerHandler extends AbstractChannelHandler {

    public TcpServerHandler(ServerConnection connection) {
        super(connection);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            Packet packet = (Packet) msg;
            packet.getHandler().handleOnServer(ctx, packet, this);
        } catch (IllegalSideException e) {
            Logger.warn(e);
        }
    }

}