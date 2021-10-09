package net.jmb19905.util;

public class ANSIColors {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001b[37;1m";

    public static String getBlack() {
        return ANSI_BLACK;
    }

    public static String getBlue() {
        return ANSI_BLUE;
    }

    public static String getGreen() {
        return ANSI_GREEN;
    }

    public static String getCyan() {
        return ANSI_CYAN;
    }

    public static String getPurple() {
        return ANSI_PURPLE;
    }

    public static String getRed() {
        return ANSI_RED;
    }

    public static String getReset() {
        return ANSI_RESET;
    }

    public static String getWhite() {
        return ANSI_WHITE;
    }

    public static String getYellow() {
        return ANSI_YELLOW;
    }
}
