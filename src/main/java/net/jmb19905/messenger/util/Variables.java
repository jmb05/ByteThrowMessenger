package net.jmb19905.messenger.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Variables {

    public static final String currentTime = new SimpleDateFormat("yyyy-MM.dd-HH-mm-ss").format(new Date());
    public static String currentSide = "";

    public static final int DEFAULT_PORT = 10101;

}
