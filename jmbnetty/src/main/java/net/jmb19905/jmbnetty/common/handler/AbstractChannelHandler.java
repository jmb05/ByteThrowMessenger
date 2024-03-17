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
import net.jmb19905.jmbnetty.common.handler.event.*;
import net.jmb19905.util.crypto.Encryption;
import net.jmb19905.util.events.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;

public abstract class AbstractChannelHandler extends ChannelInboundHandlerAdapter implements IEncryptedHandler {

    private final Encryption encryption;

    protected final EventHandler<HandlerEventContext> eventHandler;

    public AbstractChannelHandler() {
        this.encryption = new Encryption();
        this.eventHandler = new EventHandler<>("handler");
        this.eventHandler.setValid(true);
    }

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) {
        eventHandler.performEvent(new ActiveEvent(HandlerEventContext.create(this)));
    }

    @Override
    public abstract void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg);

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) {
        eventHandler.performEvent(new InactiveEvent(HandlerEventContext.create(this)));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        eventHandler.performEvent(new ExceptionEvent(HandlerEventContext.create(this), cause));
    }

    @Override
    public Encryption getEncryption() {
        return encryption;
    }

    public PublicKey getPublicKey() {
        return encryption.getPublicKey();
    }

    public void setPublicKey(PublicKey key) {
        encryption.setReceiverPublicKey(key);
    }

    public void addActiveEvent(ActiveEventListener listener) {
        eventHandler.addEventListener(listener);
    }

    public void addInactiveEvent(InactiveEventListener listener) {
        eventHandler.addEventListener(listener);
    }

    public void addExceptionEvent(ExceptionEventListener listener) {
        eventHandler.addEventListener(listener);
    }

}