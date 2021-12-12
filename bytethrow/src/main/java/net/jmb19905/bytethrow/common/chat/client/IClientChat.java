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

package net.jmb19905.bytethrow.common.chat.client;

import net.jmb19905.bytethrow.common.chat.IChat;
import net.jmb19905.bytethrow.common.chat.Message;

import java.util.List;

public interface IClientChat<M extends Message> extends IChat {
    List<M> getMessages();
    void addMessage(M message);
    void merge(IClientChat<M> other);
    boolean isValid();
}
