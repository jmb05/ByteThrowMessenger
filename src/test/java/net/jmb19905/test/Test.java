package net.jmb19905.test;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;

/**
 * To test stuff
 */
public class Test {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        JOptionPane.showMessageDialog(null, "Password does not meet the right criteria\nPassword should have:\n   - at least eight characters\n   - at least one upper and one lowercase letter\n   - at least one digit\n   - at least one symbol (e.g.: . ! # - _)", "", JOptionPane.ERROR_MESSAGE);

        /*JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JDesktopPane pane = new JDesktopPane();
        pane.setPreferredSize(new Dimension(500, 500));
        frame.add(pane);

        JInternalFrame internalFrame = new JInternalFrame();

        ImagePanel panel = null;
        try {
            panel = new ImagePanel(ImageIO.read(new File("src/main/resources/icon.png")), 200, 200);
            panel.setBackground(Color.white);
        } catch (IOException e) {
            e.printStackTrace();
        }

        internalFrame.add(panel);
        internalFrame.setResizable(true);
        pane.add(internalFrame);
        internalFrame.setVisible(true);
        internalFrame.getContentPane().setSize(new Dimension(200, 200));
        internalFrame.pack();
        internalFrame.show();

        frame.setVisible(true);
        frame.pack();*/
    }
}
