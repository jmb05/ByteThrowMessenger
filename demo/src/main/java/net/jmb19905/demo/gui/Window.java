package net.jmb19905.demo.gui;

import net.jmb19905.demo.gui.events.DemoGuiEvent;
import net.jmb19905.demo.gui.events.DemoGuiEventContext;
import net.jmb19905.demo.gui.events.MessageSendEvent;
import net.jmb19905.util.ShutdownManager;
import net.jmb19905.util.events.EventHandler;
import net.jmb19905.util.events.EventListener;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The basic chat window
 */
public class Window extends JFrame {

    private final StyledDocument document;

    private final JTextField field = new JTextField();

    private final EventHandler<DemoGuiEventContext> eventHandler;

    public Window(String title) {
        //On Some Linux Systems Java doesn't automatically use Anti-Aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");

        this.eventHandler = new EventHandler<>("gui");

        setLayout(new GridBagLayout());
        setTitle("Diffie-Hellman Key Exchange Demo - " + title);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                ShutdownManager.shutdown(0);
            }
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.insets = new Insets(5,5,5,5);

        JTextPane area = new JTextPane();
        document = area.getStyledDocument();

        area.setPreferredSize(new Dimension(500, 400));
        area.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, constraints);

        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        setFieldEnabled(false);
        Window instance = this;
        field.addActionListener(l -> {
            String message = field.getText();
            appendMessage("You", message);
            eventHandler.performEvent(new MessageSendEvent(DemoGuiEventContext.create(instance), message));
            field.setText("");
        });
        add(field, constraints);

        pack();
        setVisible(true);
        eventHandler.setValid(true);
    }

    /**
     * enables/disables the text field
     */
    public void setFieldEnabled(boolean e) {
        field.setEnabled(e);
    }

    /**
     * Prints a message on the screen
     * @param sender the name of the person who sent the message
     * @param message the message that is added
     */
    public void appendMessage(String sender, String message) {
        try {
            if (sender.isEmpty()) {
                document.insertString(document.getLength(), message + "\n", null);
            } else {
                document.insertString(document.getLength(), "<" + sender + "> " + message + "\n", null);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an listener for a GUI Event
     * @param listener the listener for the GUI Event
     */
    public void addEventListener(EventListener<? extends DemoGuiEvent> listener) {
        eventHandler.addEventListener(listener);
    }

}