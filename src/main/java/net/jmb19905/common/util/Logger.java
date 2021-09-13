package net.jmb19905.common.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    /**
     * Color values for ANSI compatible consoles
     */

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001b[37;1m";

    private static Level level = Level.TRACE;

    private static boolean isOnNewLine = true;

    private static BufferedWriter writer;
    private static String side;

    public static void initLogFile(boolean server){
        side = server ? "server" : "client";
        File logFile = new File("logs/latest_" + side + ".log");
        if(logFile.exists()){
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
    }

    public static Level getLevel() {
        return level;
    }

    /**
     * Logs a message to the console
     * @param message the message
     * @param currentLevel the Level of the message
     */
    public static void log(String message, Level currentLevel){
        if(currentLevel.tier >= level.tier) {
            if (!isOnNewLine) {
                System.out.println("\n");
                writeLine("");
            }
            String log = "[" + Util.getCompactDate(true) + "] [" + currentLevel + "] " + message;
            System.out.println(currentLevel.getColor() + log + ANSI_RESET);
            isOnNewLine = true;
            writeLine(log);
        }
    }

    public static void logPart(String message, Level currentLevel){
        if(currentLevel.tier >= level.tier) {
            if (isOnNewLine) {
                String log = "[" + Util.getCompactDate(true) + "] [" + currentLevel + "] " + message;
                System.out.print(currentLevel.getColor() + log + ANSI_RESET);
                isOnNewLine = false;
                writeToFile(log);
            } else {
                System.out.print(currentLevel.getColor() + message + ANSI_RESET);
                writeToFile(message);
            }
        }
    }

    public static void finishLine(){
        System.out.println("\n");
        isOnNewLine = true;
        writeLine("");
    }

    private static void writeToFile(String s){
        if(writer != null){
            try {
                writer.write(s);
                writer.flush();
            } catch (IOException e) {
                Logger.log(e, Level.ERROR);
            }
        }
    }

    private static void writeLine(String s){
        if(writer != null){
            try {
                writer.write(s);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                Logger.log(e, Level.ERROR);
            }
        }
    }

    /**
     * Logs an Exception to the console
     * @param cause the exception
     * @param level the level of the exception
     */
    public static void log(Throwable cause, Level level){
        log(cause.getMessage(), level);
        cause.printStackTrace();
    }

    /**
     * Logs an Exception and a Message to the console
     * @param cause the exception
     * @param message the message
     * @param level the level of the exception and message
     */
    public static void log(Throwable cause, String message, Level level){
        log(message, level);
        cause.printStackTrace();
    }

    public static void close(){
        if(writer != null){
            try {
                writer.close();
                File file = new File("logs/latest_" + side + ".log");
                file.renameTo(new File("logs/" + Util.getCompactDate(false) + "_" + side + ".log"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tells the user the severity of messages and exceptions.
     * Tells the console the color of the message.
     */
    public enum Level{
        TRACE(0, ANSI_BLUE), INFO(1, ANSI_WHITE), DEBUG(2,ANSI_GREEN), WARN(3,ANSI_YELLOW), ERROR(4,ANSI_RED), FATAL(5,ANSI_RED);

        private final int tier;
        /**
         * Color of the message in the console
         */
        private final String color;

        Level(int tier, String color){
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
