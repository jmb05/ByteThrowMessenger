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

package net.jmb19905.jmbnetty.common.state;

import net.jmb19905.util.Logger;
import net.jmb19905.util.registry.Registry;

public class StateRegistry extends Registry {

    private static final StateRegistry instance = new StateRegistry();

    public <S extends State, SCP extends StateChangePacket<S>> void register(String id, Class<SCP> stateChangePacketClass) {
        super.register(id, new StateType<>(stateChangePacketClass));
    }

    public StateType<? extends State, ? extends StateChangePacket<? extends State>> getStateType(String id) {
        try {
            return (StateType<? extends State, ? extends StateChangePacket<? extends State>>) getRegistry(id);
        } catch (NullPointerException e) {
            Logger.error("No such StateType registered");
            return null;
        }
    }

    public static StateRegistry getInstance() {
        return instance;
    }
}
