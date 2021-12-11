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
import net.jmb19905.jmbnetty.common.crypto.Encryption;
import net.jmb19905.util.events.Event;
import net.jmb19905.util.events.EventContext;
import net.jmb19905.util.events.EventHandler;
import net.jmb19905.util.events.EventListener;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;

public abstract class AbstractChannelHandler extends ChannelInboundHandlerAdapter implements IEncryptedHandler {

    private final Encryption encryption;

    private final EventHandler<HandlerEventContext> eventHandler;

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

    private static abstract class HandlerEvent extends Event<HandlerEventContext> {
        public HandlerEvent(@NotNull HandlerEventContext ctx, String id) {
            super(ctx, id);
        }
    }

    public static class HandlerEventContext extends EventContext {
        private final AbstractChannelHandler handler;

        private HandlerEventContext(AbstractChannelHandler handler) {
            super(handler);
            this.handler = handler;
        }

        public AbstractChannelHandler getHandler() {
            return handler;
        }

        public static HandlerEventContext create(AbstractChannelHandler handler) {
            return new HandlerEventContext(handler);
        }
    }

    public static class ActiveEvent extends HandlerEvent {
        private static final String ID = "active";
        public ActiveEvent(HandlerEventContext ctx) {
            super(ctx, ID);
        }
    }

    public interface ActiveEventListener extends EventListener<ActiveEvent> {
        @Override
        default String getId() {
            return ActiveEvent.ID;
        }
    }

    public static class InactiveEvent extends HandlerEvent {
        private static final String ID = "inactive";
        public InactiveEvent(HandlerEventContext ctx) {
            super(ctx, ID);
        }
    }

    public interface InactiveEventListener extends EventListener<InactiveEvent> {
        @Override
        default String getId() {
            return InactiveEvent.ID;
        }
    }

    public static class ExceptionEvent extends HandlerEvent {
        private static final String ID = "exception";
        private final Throwable throwable;

        public ExceptionEvent(HandlerEventContext ctx, Throwable throwable) {
            super(ctx, ID);
            this.throwable = throwable;
        }

        public Throwable getCause() {
            return throwable;
        }
    }

    public interface ExceptionEventListener extends EventListener<ExceptionEvent> {
        @Override
        default String getId() {
            return ExceptionEvent.ID;
        }
    }
}