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

package net.jmb19905.bytethrow.client.gui;

import com.formdev.flatlaf.FlatDarculaLaf;
import net.jmb19905.bytethrow.client.StartClient;
import net.jmb19905.bytethrow.client.util.Localisation;
import net.jmb19905.bytethrow.common.util.ConfigManager;
import net.jmb19905.bytethrow.common.util.Util;

public class GUITest {

    public static void main(String[] args) {
        StartClient.version = Util.loadVersion(args[0].equals("dev"));
        ConfigManager.init();
        StartClient.config = ConfigManager.loadClientConfig();

        //On Some Linux Systems Java doesn't automatically use Anti-Aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");

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
