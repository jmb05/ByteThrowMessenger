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

import net.jmb19905.bytethrow.common.packets.FailPacket;
import net.jmb19905.net.NetThread;
import net.jmb19905.net.event.ContextFuture;
import net.jmb19905.net.event.NetworkEventContext;
import net.jmb19905.net.handler.HandlingContext;
import net.jmb19905.net.packet.Packet;
import net.jmb19905.util.Logger;

import java.net.SocketAddress;

public class NetworkingUtility {

    public static ContextFuture<NetworkEventContext> sendPacket(Packet packet, NetworkEventContext ctx) {
        Logger.debug("Sending: " + packet);
        return ctx.send(packet);
    }

    public static ContextFuture<HandlingContext> sendPacket(Packet packet, HandlingContext ctx) {
        Logger.debug("Sending: " + packet);
        return ctx.send(packet);
    }

    public static ContextFuture<NetThread> sendPacket(Packet packet, NetThread netThread, SocketAddress address) {
        Logger.debug("Sending: " + packet);
        return net.jmb19905.net.NetworkingUtility.send(netThread, address, packet);
    }

    /**
     * Send a Packet to the client to tell him that something failed
     *
     * @param cause   the cause of the fail e.g. login or register
     * @param message the message displayed to the client
     */
    public static ContextFuture<NetworkEventContext> sendFail(NetworkEventContext ctx, String cause, String message, String extra) {
        FailPacket failPacket = new FailPacket();
        failPacket.cause = cause;
        failPacket.message = message;
        failPacket.extra = extra;
        return ctx.send(failPacket);
    }

    public static ContextFuture<HandlingContext> sendFail(HandlingContext ctx, String cause, String message, String extra) {
        FailPacket failPacket = new FailPacket();
        failPacket.cause = cause;
        failPacket.message = message;
        failPacket.extra = extra;
        return ctx.send(failPacket);
    }

    public static ContextFuture<NetThread> sendFail(NetThread thread, SocketAddress address, String cause, String message, String extra) {
        FailPacket failPacket = new FailPacket();
        failPacket.cause = cause;
        failPacket.message = message;
        failPacket.extra = extra;
        return net.jmb19905.net.NetworkingUtility.send(thread, address, failPacket);
    }

}
