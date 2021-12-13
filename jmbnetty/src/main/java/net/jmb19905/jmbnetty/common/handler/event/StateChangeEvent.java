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

package net.jmb19905.jmbnetty.common.handler.event;

import org.jetbrains.annotations.NotNull;

public class StateChangeEvent extends HandlerEvent{

    public static final String ID = "state_change";

    private final String stateName;
    private final String stateType;

    public StateChangeEvent(@NotNull HandlerEventContext ctx, String stateName, String stateType) {
        super(ctx, ID);
        this.stateName = stateName;
        this.stateType = stateType;
    }

    public String getStateName() {
        return stateName;
    }

    public String getStateTypeId() {
        return stateType;
    }
}
