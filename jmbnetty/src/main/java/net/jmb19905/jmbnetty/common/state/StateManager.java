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

import java.util.HashMap;
import java.util.Map;

public class StateManager {

    private final Map<String, State> states = new HashMap<>();

    public <S extends State> boolean addState(String id, S state) {
        if(states.containsKey(id)) return false;
        states.put(id, state);
        return true;
    }

    public <S extends State> void overwriteState(String id, S state) {
        states.put(id, state);
    }

    @SuppressWarnings("unchecked")
    public <S extends State> S getState(String id) {
        return (S) states.get(id);
    }

    public boolean hasState(String id) {
        return states.containsKey(id);
    }

    public void removeState(String id) {
        states.remove(id);
    }

    public Map<String, State> getStates() {
        return states;
    }
}
