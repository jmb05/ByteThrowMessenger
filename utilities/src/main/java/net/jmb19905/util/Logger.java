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

import java.io.*;
import java.nio.file.*;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Logger {

    /**
     * Color values for ANSI compatible consoles
     */

    public static final String ANSI_RESET = "\u001B\\[0m";
    public static final String ANSI_BLACK = "\u001B\\[30m";
    public static final String ANSI_RED = "\u001B\\[31m";
    public static final String ANSI_GREEN = "\u001B\\[32m";
    public static final String ANSI_YELLOW = "\u001B\\[33m";
    public static final String ANSI_BLUE = "\u001B\\[34m";
    public static final String ANSI_PURPLE = "\u001B\\[35m";
    public static final String ANSI_CYAN = "\u001B\\[36m";
    public static final String ANSI_WHITE = "\u001b\\[37;1m";

    private static Level level = Level.INFO;

    private static boolean isOnNewLine = true;

    private static BufferedWriter writer;
    private static String name;

    private static boolean closed = false;

    public static void initLogFile(String name) {
        if (closed) {
            return;
        }
        Logger.name = name;
        File logFile = new File("logs/latest_" + name + ".log");
        if (logFile.exists()) {
            logFile.delete();
        }
        try {
            logFile.getParentFile().mkdirs();
            logFile.createNewFile();
            writer = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            log(e, Level.ERROR);
        }
    }

    public static void setLevel(Level level) {
        Logger.level = level;
        Logger.info("Set Logger Level to: " + level);
    }

    public static Level getLevel() {
        return level;
    }

    private static void writeLine(String s) {
        if (writer != null && !closed) {
            try {
                writer.write(replaceANSI(s));
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                Logger.log(e, Level.ERROR);
            }
        }
    }

    private static void logRaw(String message) {
        if (closed) {
            return;
        }
        if (!isOnNewLine) {
            System.out.println("\n");
            writeLine("");
        }
        System.out.println(message);
        isOnNewLine = true;
        writeLine(message);
    }

    /**
     * Logs a message to the console
     *
     * @param message      the message
     * @param currentLevel the Level of the message
     */
    public static void log(String message, Level currentLevel) {
        if (currentLevel.tier >= level.tier) {
            String log = "[" + Clock.getCompactDate("dd.MM.yyyy HH:mm:ss") + "] [" + currentLevel + "] " + message;
            logRaw(currentLevel.getColor() + log + ANSIColors.getReset());
        }
    }

    /**
     * Logs an Exception to the console
     *
     * @param cause the exception
     * @param level the level of the exception
     */
    public static void log(Throwable cause, Level level) {
        log(cause, "", level);
    }

    /**
     * Logs an Exception and a Message to the console
     *
     * @param cause   the exception
     * @param message the message
     * @param level   the level of the exception and message
     */
    public static void log(Throwable cause, String message, Level level) {
        logRaw(level.getColor() + message + (message.strip().equals("") ? "" : "\n") + stacktraceAsString(cause) + ANSIColors.getReset());
    }

    public static void trace(String message) {
        log(message, Level.TRACE);
    }

    public static void trace(Throwable cause) {
        log(cause, Level.TRACE);
    }

    public static void trace(Throwable cause, String message) {
        log(cause, message, Level.TRACE);
    }

    public static void debug(String message) {
        log(message, Level.DEBUG);
    }

    public static void debug(Throwable cause) {
        log(cause, Level.DEBUG);
    }

    public static void debug(Throwable cause, String message) {
        log(cause, message, Level.DEBUG);
    }

    public static void info(String message) {
        log(message, Level.INFO);
    }

    public static void info(Throwable cause) {
        log(cause, Level.INFO);
    }

    public static void info(Throwable cause, String message) {
        log(cause, message, Level.INFO);
    }

    public static void warn(String message) {
        log(message, Level.WARN);
    }

    public static void warn(Throwable cause) {
        log(cause, Level.WARN);
    }

    public static void warn(Throwable cause, String message) {
        log(cause, message, Level.WARN);
    }

    public static void error(String message) {
        log(message, Level.ERROR);
    }

    public static void error(Throwable cause) {
        log(cause, Level.ERROR);
    }

    public static void error(Throwable cause, String message) {
        log(cause, message, Level.ERROR);
    }

    public static void fatal(String message) {
        log(message, Level.FATAL);
    }

    public static void fatal(Throwable cause) {
        log(cause, Level.FATAL);
    }

    public static void fatal(Throwable cause, String message) {
        log(cause, message, Level.FATAL);
    }

    public static void close() {
        closed = true;
        if (writer != null) {
            try {
                writer.close();
                renameLogFile(false);
            } catch (FileSystemException e) {
                renameLogFile(true);
            }catch (Exception e) {
                System.out.println(ANSIColors.getRed() + "Logger Error:");
                e.printStackTrace();
                System.out.print(ANSIColors.getReset());
            }
        }
    }

    private static void renameLogFile(boolean secondTry) {
        try {
            Path oldFile = Paths.get("logs/latest_" + name + ".log");
            Path newFile = Paths.get("logs/" + Clock.getCompactDate("dd.MM.yyyy HH.mm").replace(" ", "_") + "_" + name + "-" + (secondTry ? 2 : "") + ".log");
            if (Files.exists(newFile)) {
                Files.delete(newFile);
            }if (Files.exists(oldFile)) {
                Files.move(oldFile, newFile);
            }
        } catch (NoSuchFileException e) {
            System.out.println(ANSIColors.getYellow() + "Logger could not find logfile: file might have been changed by other client");
        }catch (IOException e) {
            System.out.println(ANSIColors.getRed() + "Logger Error:");
            e.printStackTrace();
        }
    }

    private static String stacktraceAsString(Throwable cause) {
        if (closed) {
            return "";
        }
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        cause.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private static String replaceANSI(String in) {
        return in.replaceAll(ANSI_BLACK, "").replaceAll(ANSI_BLUE, "").replaceAll(ANSI_RED, "")
                .replaceAll(ANSI_RESET, "").replaceAll(ANSI_CYAN, "").replaceAll(ANSI_GREEN, "")
                .replaceAll(ANSI_PURPLE, "").replaceAll(ANSI_WHITE, "").replaceAll(ANSI_YELLOW, "");

    }

    /**
     * Tells the user the severity of messages and exceptions.
     * Tells the console the color of the message.
     */
    public enum Level {
        TRACE(0, ANSIColors.getBlue()), DEBUG(1, ANSIColors.getGreen()), INFO(2, ANSIColors.getWhite()), WARN(3, ANSIColors.getYellow()), ERROR(4, ANSIColors.getRed()), FATAL(5, ANSIColors.getRed());

        private final int tier;
        /**
         * Color of the message in the console
         */
        private final String color;

        Level(int tier, String color) {
            this.tier = tier;
            this.color = color;
        }

        public int getTier() {
            return tier;
        }

        public String getColor() {
            return color;
        }
    }

}
