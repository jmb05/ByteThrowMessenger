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

package net.jmb19905.jmbnetty.common.connection;

import net.jmb19905.jmbnetty.common.connection.event.NetworkEventContext;
import net.jmb19905.util.Logger;
import net.jmb19905.util.events.Event;
import net.jmb19905.util.events.EventHandler;
import net.jmb19905.util.events.EventListener;

public abstract class AbstractConnection implements IConnection, Runnable {

    protected int port;
    protected Thread thread;

    protected boolean closed = false;

    private final EventHandler<NetworkEventContext> eventHandler;

    public AbstractConnection() {
        this.thread = new Thread(this);
        this.eventHandler = new EventHandler<>("network");
    }

    @Override
    public void start() {
        this.thread.start();
        eventHandler.setValid(true);
    }

    @Override
    public void stop() {
        this.thread.interrupt();
    }

    public void addEventListener(EventListener<? extends Event<? extends NetworkEventContext>> listener) {
        eventHandler.addEventListener(listener);
    }

    public void performEvent(Event<NetworkEventContext> evt) {
        eventHandler.performEvent(evt);
    }

    @Override
    public int getPort() {
        return port;
    }

    public void markClosed() {
        closed = true;
        Logger.info("Connection marked as closed");
    }

    public boolean isClosed() {
        return closed;
    }
}