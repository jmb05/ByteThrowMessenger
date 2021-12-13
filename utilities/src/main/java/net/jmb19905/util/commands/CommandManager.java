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

package net.jmb19905.util.commands;

import net.jmb19905.util.commands.registry.CommandRegistry;
import net.jmb19905.util.commands.registry.CommandType;
import net.jmb19905.util.commands.registry.ICommand;

import java.util.Arrays;
import java.util.Scanner;

public class CommandManager {

    private static CommandThread thread;

    public static void init() {
        thread = new CommandThread();
        thread.start();
    }

    public static void close() {
        thread.stop();
    }

    private static class CommandThread implements Runnable {

        private Thread thread;

        public void start() {
            this.thread = new Thread(this);
            this.thread.start();
        }

        public void stop() {
            this.thread.interrupt();
        }

        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (!thread.isInterrupted()) {
                String line = scanner.nextLine();
                String[] parts = line.split(" ");
                String id = parts[0];
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);
                CommandType<? extends ICommand> type = CommandRegistry.getInstance().getCommandType(line.split(" ")[0]);
                type.getHandler().handle(type.newCommandInstance(), args);
            }
        }
    }

}