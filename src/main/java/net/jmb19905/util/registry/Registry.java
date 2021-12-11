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

package net.jmb19905.util.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Registry {

    private final Map<String, Type<?>> registries = new ConcurrentHashMap<>();

    public void register(String id, Type<?> type) {
        this.registries.put(id, type);
    }

    public Type<?> getRegistry(String id) throws NullPointerException {
        Type<?> type = registries.get(id);
        if (type == null) {
            throw new NullPointerException("Could not find Registry Entry: " + id);
        }
        return type;
    }

}
