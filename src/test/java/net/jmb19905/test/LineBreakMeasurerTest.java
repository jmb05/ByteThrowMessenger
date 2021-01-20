package net.jmb19905.test;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

public final class LineBreakMeasurerTest {
    private static final String TEXT = "1234567890 ABCDEFG HIJKLMN OPQRSTU VWXYZ";
    private final JLabel    lbl1 = new JLabel(TEXT);
    private final JTextArea lbl2 = new JTextArea(TEXT);
    private final JLabel    lbl3 = new WrappingLabel(TEXT);
    public JComponent makeUI() {
        Border b = BorderFactory.createLineBorder(Color.GREEN,  5);
        lbl1.setBorder(BorderFactory.createTitledBorder(b, "JLabel"));
        lbl2.setBorder(BorderFactory.createTitledBorder(b, "JTextArea"));
        lbl3.setBorder(BorderFactory.createTitledBorder(b, "LineBreakMeasurer"));

        lbl2.setFont(lbl1.getFont());
        lbl2.setEditable(false);
        lbl2.setLineWrap(true);
        lbl2.setWrapStyleWord(true);
        lbl2.setBackground(lbl1.getBackground());

        JPanel p = new JPanel(new GridLayout(3, 1));
        p.add(lbl1);
        p.add(lbl2);
        p.add(lbl3);
        return p;
    }

    public static void main(String... args) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        EventQueue.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.getContentPane().add(new LineBreakMeasurerTest().makeUI());
            f.setSize(320, 240);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}

class WrappingLabel extends JLabel {
    //TEST: private AffineTransform at = AffineTransform.getScaleInstance(.8, 1d);
    protected WrappingLabel(String text) {
        super(text);
    }
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2D = (Graphics2D) g.create();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setPaint(getForeground());
        Insets insets = getInsets();
        float x = insets.left;

        float y = insets.top;
        int width = getWidth() - insets.left - insets.right;

        AttributedCharacterIterator attributedCharacterIterator = new AttributedString(getText()).getIterator();
        FontRenderContext fontRenderContext = g2D.getFontRenderContext();
        LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(attributedCharacterIterator, fontRenderContext);

        while (lineBreakMeasurer.getPosition() < attributedCharacterIterator.getEndIndex()) {
            TextLayout textLayout = lineBreakMeasurer.nextLayout(width);
            textLayout.draw(g2D, x, y + textLayout.getAscent());
            y += textLayout.getDescent() + textLayout.getLeading() + textLayout.getAscent();
        }
        g2D.dispose();
    }
}