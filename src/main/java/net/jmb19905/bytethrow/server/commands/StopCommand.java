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

package net.jmb19905.bytethrow.server.commands;

import net.jmb19905.util.ShutdownManager;
import net.jmb19905.util.commands.registry.CommandHandler;
import net.jmb19905.util.commands.registry.ICommand;

public class StopCommand implements ICommand {

    @Override
    public String getID() {
        return "stop";
    }

    public static class StopCommandHandler extends CommandHandler {
        @Override
        public void handle(ICommand command, String[] args) {
            if (args.length < 1) {
                ShutdownManager.shutdown(0);
            } else {
                ShutdownManager.shutdown(Integer.parseInt(args[0]));
            }

        }
    }

}