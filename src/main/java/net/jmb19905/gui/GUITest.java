package net.jmb19905.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.util.Logger;

import javax.swing.*;

public class GUITest {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            Logger.log(e, "GUI may not work correctly!", Logger.Level.ERROR);
        }
        new Window();
    }

}
