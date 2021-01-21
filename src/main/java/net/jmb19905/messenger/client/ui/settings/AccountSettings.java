package net.jmb19905.messenger.client.ui.settings;

import javax.swing.*;
import java.awt.*;
import net.jmb19905.messenger.util.FileUtility;

public class AccountSettings extends JDialog {

    public AccountSettings(){
        setTitle("Account Settings");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(450, 600));
        setIconImage(FileUtility.getImageResource("icon.png"));


    }

}
