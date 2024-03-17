/*
 * A simple Messenger written in Java
 * Copyright (C) 2020-2021  Jared M. Bennett
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.jmb19905.bytethrow.client.gui.event;

import io.netty.channel.ChannelHandlerContext;
import net.jmb19905.bytethrow.client.gui.LoginDialog;
import net.jmb19905.net.handler.HandlingContext;
import org.jetbrains.annotations.NotNull;

public class LoginEvent extends GuiEvent{

    public static final String ID = "login";

    private final LoginDialog.LoginData data;

    public LoginEvent(@NotNull GuiEventContext ctx, LoginDialog.LoginData data) {
        super(ctx, ID);
        this.data = data;
    }

    public LoginDialog.LoginData getData() {
        return data;
    }
}
