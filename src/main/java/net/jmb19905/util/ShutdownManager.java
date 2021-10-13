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

package net.jmb19905.util;

import java.util.ArrayList;
import java.util.List;

public class ShutdownManager {

    private static final List<CleanUpAction> cleanUpActions = new ArrayList<>();
    private static final List<Thread> threads = new ArrayList<>();

    public static void addCleanUp(CleanUpAction cleanUpAction){
        cleanUpActions.add(cleanUpAction);
    }

    public static void addThread(Thread thread){
        threads.add(thread);
    }

    public static void shutdown(int code){
        Logger.info("Stopping...");
        for(CleanUpAction action : cleanUpActions){
            action.cleanUp();
        }
        for(Thread thread : threads){
            while (thread.isAlive()){
                //Do nothing
            }
        }
        Logger.info("Cleaned up");
        System.exit(code);
    }

    public interface CleanUpAction{
        void cleanUp();
    }
}
