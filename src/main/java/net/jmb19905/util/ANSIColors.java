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
