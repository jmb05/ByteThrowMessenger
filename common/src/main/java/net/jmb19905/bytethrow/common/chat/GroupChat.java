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

package net.jmb19905.bytethrow.common.chat;

import java.util.UUID;

public class GroupChat extends AbstractChat {

    private final String name;
    private boolean isInitializing = true;

    public GroupChat(String name) {
        this.name = name;
    }

    public GroupChat(String name, UUID uuid){
        super(uuid);
        this.name = name;
    }

    public void finishInitialization() {
        isInitializing = false;
    }

    @Override
    public boolean isValid() {
        return isInitializing || (members.size() > 1 && name != null && uniqueId != null);
    }

    public String getName() {
        return name;
    }
}
