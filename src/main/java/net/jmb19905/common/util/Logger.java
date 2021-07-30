package net.jmb19905.common.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

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
    public static final String ANSI_WHITE = "\u001B[37m";

    private Level level = Level.TRACE;

    public void setLevel(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    /**
     * Logs a message to the console
     * @param message the message
     * @param level the Level of the message
     */
    public static void log(String message, Level level){
        Calendar calendar = new GregorianCalendar();
        String compactDate = "[" + calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + "] ";

        System.out.println(level.getColor() + compactDate + "[" + level + "] " + message + ANSI_RESET);
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

    /**
     * Tells the user the severity of messages and exceptions.
     * Tells the console the color of the message.
     */
    public enum Level{
        TRACE(ANSI_WHITE), INFO(ANSI_BLUE), DEBUG(ANSI_GREEN), WARN(ANSI_YELLOW), ERROR(ANSI_RED), FATAL(ANSI_RED);

        /**
         * Color of the message in the console
         */
        private final String color;

        Level(String color){
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

}
