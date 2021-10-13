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

package net.jmb19905.jmbnetty.common.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jmb19905.jmbnetty.common.connection.AbstractConnection;
import net.jmb19905.jmbnetty.common.connection.event.ConnectedEvent;
import net.jmb19905.jmbnetty.common.connection.event.DisconnectedEvent;
import net.jmb19905.jmbnetty.common.connection.event.ErrorEvent;
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.util.Logger;

import java.security.PublicKey;

public abstract class AbstractChannelHandler extends ChannelInboundHandlerAdapter implements IEncryptedHandler {

    private final AbstractConnection connection;
    private final Encryption encryption;

    public AbstractChannelHandler(AbstractConnection connection){
        this.connection = connection;
        this.encryption = new Encryption();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        connection.performEvent("connected", () -> new ConnectedEvent(this));
    }

    @Override
    public abstract void channelRead(ChannelHandlerContext ctx, Object msg);

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        connection.performEvent("disconnected", () -> new DisconnectedEvent(this));
        connection.markClosed();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        connection.performEvent("error", () -> new ErrorEvent(this, cause));
        Logger.error(cause);
    }

    public AbstractConnection getConnection() {
        return connection;
    }

    @Override
    public Encryption getEncryption() {
        return encryption;
    }

    public PublicKey getPublicKey(){
        return encryption.getPublicKey();
    }

    public void setPublicKey(PublicKey key){
        encryption.setReceiverPublicKey(key);
    }
}