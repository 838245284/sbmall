package cn.wu1588.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cxf on 2018/7/19.
 */

public class DateFormatUtil {

    private static SimpleDateFormat sFormat;
    private static SimpleDateFormat sFormat2;
    private static SimpleDateFormat sFormat3;

    static {
        sFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        sFormat2 = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        sFormat3 = new SimpleDateFormat("MM.dd-HH:mm:ss");
    }


    public static String getCurTimeString() {
        return sFormat.format(new Date());
    }

    public static String getVideoCurTimeString() {
        return sFormat2.format(new Date());
    }

    public static String getCurTimeString2() {
        return sFormat3.format(new Date());
    }


    public static String FormatRunTime(long runTime) {
        if(runTime < 0) return "00:00:00";

        long hour = runTime / 3600;
        long minute = (runTime % 3600) / 60;
        long second = runTime % 60;

        return unitTimeFormat(hour) + ":" + unitTimeFormat(minute) + ":" +
                unitTimeFormat(second);
    }

    private static String unitTimeFormat(long number) {
        return String.format("%02d", number);
    }
}
