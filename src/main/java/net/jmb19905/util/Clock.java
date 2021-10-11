package net.jmb19905.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Clock {

    private static long start;

    public static void init(){
        start = System.currentTimeMillis();
    }

    public static long getStart() {
        return start;
    }

    public static int getTime(){
        return (int) (System.currentTimeMillis() - start);
    }

    public static String getCompactDate(String pattern){
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        Date now = new Date();
        return format.format(now);
    }
}
