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

package net.jmb19905.bytethrow.common.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.jmb19905.bytethrow.common.packets.FailPacket;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.jmbnetty.common.packets.registry.Packet;
import net.jmb19905.jmbnetty.server.tcp.TcpServerHandler;
import net.jmb19905.jmbnetty.utility.NetworkUtility;

public class NetworkingUtility {

    public static ChannelFuture sendPacket(Packet packet, Channel channel, Encryption encryption) {
        return NetworkUtility.sendTcp(channel, packet, encryption);
    }

    /**
     * Send a Packet to the client to tell him that something failed
     *
     * @param cause   the cause of the fail e.g. login or register
     * @param message the message displayed to the client
     */
    public static ChannelFuture sendFail(Channel channel, String cause, String message, String extra, TcpServerHandler handler) {
        FailPacket failPacket = new FailPacket();
        failPacket.cause = cause;
        failPacket.message = message;
        failPacket.extra = extra;
        return NetworkingUtility.sendPacket(failPacket, channel, handler.getEncryption());
    }

}
