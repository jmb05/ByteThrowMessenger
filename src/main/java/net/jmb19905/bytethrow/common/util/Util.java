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

package net.jmb19905.bytethrow.common.util;

import net.jmb19905.bytethrow.common.Version;
import net.jmb19905.util.Logger;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    /**
     * Checks if the provided String is at least 8 characters long, contains at least one Upper and one Lowercase letter, at least one digit and at least one symbol
     *
     * @param password the provided Password as String
     * @return if the password is valid
     */
    public static boolean checkPasswordRules(String password) {
        if (password.length() < 8) {
            return false;
        }
        Pattern pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(password);
        boolean symbolFlag = matcher.find();
        if (!symbolFlag) {
            return false;
        }
        char currentChar;
        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        for (int i = 0; i < password.length(); i++) {
            currentChar = password.charAt(i);
            if (Character.isDigit(currentChar)) {
                numberFlag = true;
            } else if (Character.isUpperCase(currentChar)) {
                capitalFlag = true;
            } else if (Character.isLowerCase(currentChar)) {
                lowerCaseFlag = true;
            }
            if (numberFlag && capitalFlag && lowerCaseFlag) {
                return true;
            }
        }
        return false;
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resizedImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        ((Graphics2D) outputImage.getGraphics()).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        outputImage.getGraphics().drawImage(resizedImage, 0, 0, null);
        return outputImage;
    }

    public static Color getContrastColor(Color color, int smooth) {
        int val = color.getRed() + color.getGreen() + color.getBlue();
        if (val >= 382) {
            return new Color(smooth, smooth, smooth);
        } else {
            return new Color(255 - smooth, 255 - smooth, 255 - smooth);
        }
    }

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static BufferedImage cropImageToCircle(BufferedImage bufferedImage) {
        return makeRoundedCorner(bufferedImage, bufferedImage.getHeight());
    }

    public static BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = output.createGraphics();

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    public static BufferedImage createDefaultAvatar(char character, int imageWidth, int imageHeight, Color color) {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = image.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics2D.setColor(color);
        graphics2D.fill(new Rectangle2D.Double(0, 0, imageWidth, imageHeight));

        graphics2D.setColor(getContrastColor(color, 25));

        Font font = new Font(getSystemFont().getName(), Font.BOLD, 52);
        graphics2D.setFont(font);

        Rectangle2D.Float rectangle2D = (Rectangle2D.Float) graphics2D.getFont().createGlyphVector(graphics2D.getFontRenderContext(), "" + character).getVisualBounds();
        double charWidth = rectangle2D.getWidth();
        double charHeight = rectangle2D.getHeight();
        float x = (float) ((imageWidth - charWidth) / 2);
        float y = (float) (((imageHeight - charHeight) / 2) + Math.abs(rectangle2D.y));

        graphics2D.drawString(character + "", (float) (x - (rectangle2D.getX() / 2)), y);

        graphics2D.dispose();

        return image;
    }

    public static Font getSystemFont() {
        return new JLabel().getFont();
    }

    public static String getCompactDate(boolean seconds) {
        Calendar calendar = new GregorianCalendar();
        String out = calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
        if (seconds) {
            out.concat(":" + calendar.get(Calendar.SECOND));
        }
        return out;
    }

    public static String getCompactDate(Date date, boolean seconds) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        String out = calendar.get(Calendar.DAY_OF_MONTH) + "." + calendar.get(Calendar.MONTH) + "." + calendar.get(Calendar.YEAR) + " " + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
        if (seconds) {
            out.concat(":" + calendar.get(Calendar.SECOND));
        }
        return out;
    }

    public static int getLineCount(JTextComponent textPane) {
        int totalCharacters = textPane.getText().length();
        int lineCount = (totalCharacters == 0) ? 1 : 0;

        try {
            int offset = totalCharacters;
            while (offset > 0) {
                offset = Utilities.getRowStart(textPane, offset) - 1;
                lineCount++;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return lineCount;
    }

    /**
     * Loads the version from the version.properties file if the instance is running in a development environment and
     * from the jars manifest if the instance is running as standalone program
     *
     * @param isDev if the instance is running in a development environment
     * @return the version
     */
    public static Version loadVersion(boolean isDev) {
        String versionAsString;
        if (isDev) {
            try {
                versionAsString = ResourceUtility.readResourceProperties("version.properties").getProperty("version");
            } catch (NullPointerException e) {
                Logger.error(e, "Error read version");
                versionAsString = "0.0.0";
            }
        } else {
            versionAsString = Util.class.getPackage().getImplementationVersion();
        }
        return new Version(versionAsString);
    }

    public static String getUserHome() {
        return System.getProperty("user.home");
    }


    public static List<String> sortStringsAlphabetically(List<String> list) {
        Comparator<String> comparator = (s, t1) -> {
            int length = Math.min(s.length(), t1.length());
            for (int i = 0; i < length; i++) {
                if (s.toLowerCase(Locale.ROOT).charAt(i) > t1.toLowerCase(Locale.ROOT).charAt(i)) {
                    return 1;
                } else if (s.toLowerCase(Locale.ROOT).charAt(i) < t1.toLowerCase(Locale.ROOT).charAt(i)) {
                    return -1;
                }
            }
            return 0;
        };
        list.sort(comparator);
        return list;
    }

}
