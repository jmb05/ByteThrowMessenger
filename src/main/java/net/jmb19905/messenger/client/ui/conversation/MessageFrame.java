package net.jmb19905.messenger.client.ui.conversation;

import net.jmb19905.messenger.client.ByteThrowClient;
import net.jmb19905.messenger.client.ui.util.component.ImagePanel;
import net.jmb19905.messenger.messages.ImageMessage;
import net.jmb19905.messenger.messages.Message;
import net.jmb19905.messenger.messages.TextMessage;
import net.jmb19905.messenger.util.FormattedImage;
import net.jmb19905.messenger.util.Variables;

import javax.swing.*;
import javax.swing.plaf.InternalFrameUI;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageFrame extends JInternalFrame {

    private String title;
    private String text;
    private final FormattedImage[] images;

    private JLabel titleLabel;
    private JTextArea area;
    private final List<ImagePanel> imagePanels = new ArrayList<>();

    private Color color;

    public MessageFrame(Message message){
        if(message instanceof TextMessage){
            this.title = message.sender;
            this.text = ((TextMessage) message).text;
            this.images = new FormattedImage[0];
        }else if(message instanceof ImageMessage){
            this.title = message.sender;
            this.text = ((ImageMessage) message).caption;
            this.images = ((ImageMessage) message).images;
        }else{
            this.title = "";
            this.text = "";
            this.images = new FormattedImage[0];
        }
        initGUI();
    }

    public MessageFrame(String text) {
        this("", text);
    }

    public MessageFrame(String title, String text) {
        this(title, text, new FormattedImage[0]);
    }


    public MessageFrame(String title, String text, FormattedImage... images) {
        this.title = title;
        this.text = text;
        this.images = images;
        initGUI();
    }

    private void initGUI(){
        int width = 10;
        int height = 10;

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;

        if(!title.equals("")){
            titleLabel = new JLabel(title);
            constraints.gridx = 0;
            constraints.gridy = 0;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            titleLabel.setFont(Variables.boldFont);
            add(titleLabel, constraints);
            height += titleLabel.getPreferredSize().height;
        }

        area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(Variables.defaultFont);
        constraints.gridx = 0;
        constraints.gridy = 1;
        add(area, constraints);

        if(titleLabel != null) {
            width += Math.max(titleLabel.getPreferredSize().width, area.getPreferredSize().width);
        }else{
            width += area.getPreferredSize().width + 10;
        }
        System.out.println(area.getPreferredSize());
        height += area.getPreferredSize().height;

        for(FormattedImage image : images){
            int panelWidth = width - 10;
            if(getParent() != null) {
                panelWidth = (getParent().getWidth() / 3) - 10;
            }
            if(panelWidth > width){
                width = panelWidth;
            }
            int panelHeight = image.image.getHeight() / image.image.getWidth() * panelWidth;
            ImagePanel panel = new ImagePanel(image.image, panelWidth, panelHeight);
            constraints.gridx = 0;
            constraints.gridy = GridBagConstraints.RELATIVE;
            add(panel, constraints);
            imagePanels.add(panel);

            panel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().open(new File("userdata/" + ByteThrowClient.getUsername() + "/media/" + image.name + "." + image.format));
                    } catch (IOException ioException) {
                        JOptionPane.showMessageDialog(null, "Could not open file", "", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            height += (panelHeight + 10);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPopupMenu menu = new JPopupMenu();
                JMenuItem delete = new JMenuItem("Delete");
                delete.addActionListener(ae -> remove());
                menu.add(delete);
                //menu.show((Component) e.getSource(), e.getX(), e.getY());
            }
        });

        getContentPane().setPreferredSize(new Dimension(width, height));
        pack();
    }

    private void remove(){
        Container parent = this.getParent();
        if(parent != null){
            parent.remove(this);
            parent.repaint();
            if(parent instanceof ConversationPane){
                parent.remove(this);
            }
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    //Removes the title bar
    @Override
    public void setUI(InternalFrameUI ui) {
        super.setUI(ui);
        BasicInternalFrameUI frameUI = (BasicInternalFrameUI) getUI();
        if (frameUI != null) frameUI.setNorthPane(null);
    }

    @Override
    public void repaint() {
        super.repaint();
        setBackground(color);
        if(titleLabel != null) {
            titleLabel.setBackground(color);
        }
        if(area != null) {
            area.setBackground(color);
        }
        try {
            titleLabel.setFont(Variables.boldFont);
            area.setFont(Variables.defaultFont);
        }catch (NullPointerException ignored){}
        /*if(getParent() != null && getWidth() > 0) {
            int width = getWidth();
            int height = getHeight();
            if (width > getParent().getWidth() / 2){
                height -= area.getPreferredSize().height;
                String firstPart = "";
                String secondPart = "";
                for(int i=0;i<100;i++){
                    try {
                        if (text.charAt((text.length() / 2) - i) == ' ') {
                            firstPart = text.substring(0, (text.length() / 2) - i);
                            secondPart = text.substring((text.length() / 2) - i);
                            break;
                        } else if (text.charAt((text.length() / 2) + i) == ' ') {
                            firstPart = text.substring(0, (text.length() / 2) + i);
                            secondPart = text.substring((text.length() / 2) + i);
                            break;
                        }
                    }catch (StringIndexOutOfBoundsException e){
                        break;
                    }
                }
                area.setText(firstPart + "\n" + secondPart);
                height += area.getPreferredSize().height;
                width = Math.max(area.getPreferredSize().width, width);
            }
            setPreferredSize(new Dimension(width, height));
        }*/
    }
}
