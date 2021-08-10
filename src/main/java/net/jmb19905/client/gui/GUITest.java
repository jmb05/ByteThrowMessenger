package net.jmb19905.client.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.client.ClientMain;
import net.jmb19905.client.util.Localisation;
import net.jmb19905.common.util.ConfigManager;
import net.jmb19905.common.util.Util;

public class GUITest {

    public static void main(String[] args) {
        ClientMain.version = Util.loadVersion(args[0].equals("dev"));
        ClientMain.config = ConfigManager.loadClientConfigFile("config/client_config.json");
        FlatDarculaLaf.setup();
        Localisation.reload();

        Window window = new Window();
        window.appendLine("This is a GUI Test");

        /*BufferedImage bufferedImage = ResourceUtility.getImageResource("icons/Me.png");
        ImageIcon icon = new ImageIcon(Util.cropImageToCircle(Util.toBufferedImage(bufferedImage.getScaledInstance(128, 128, 0))));
        AccountSettings accountSettings = new AccountSettings(icon, "jmb05");
        accountSettings.setVisible(true);
        */
    }
}
