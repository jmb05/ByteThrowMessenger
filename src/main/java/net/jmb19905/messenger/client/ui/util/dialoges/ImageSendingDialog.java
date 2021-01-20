package net.jmb19905.messenger.client.ui.util.dialoges;

import net.jmb19905.messenger.client.ui.util.component.ImagePanel;
import net.jmb19905.messenger.util.FormattedImage;

import javax.swing.*;

public class ImageSendingDialog extends JDialog {

    private ImagePanel panel;

    public ImageSendingDialog(JFrame parent, FormattedImage... images){
        super(parent);
        setModal(true);
        setResizable(false);


    }

}
