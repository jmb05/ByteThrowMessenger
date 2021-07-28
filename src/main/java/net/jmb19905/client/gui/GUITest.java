package net.jmb19905.client.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import io.netty.util.HashingStrategy;
import net.jmb19905.common.util.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.security.MessageDigest;
import java.util.Enumeration;

public class GUITest {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
            setUIFont(new FontUIResource("Arial", FontUIResource.BOLD,17));
        } catch (UnsupportedLookAndFeelException e) {
            Logger.log(e, "GUI may not work correctly!", Logger.Level.ERROR);
        }
        //Window window = new Window();
        LoginDialog dialog = new LoginDialog("Username101", "Password101", "Extra");
        dialog.showDialog();
    }

    public static void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource orig) {
                Font font = new Font(f.getFontName(), orig.getStyle(), f.getSize());
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }

}
