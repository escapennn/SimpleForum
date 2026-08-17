package com.linzp.forum.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 时间显示工具。
 * 列表里不需要完整时间，看到"3分钟前"比"2026-08-15 21:07"舒服。
 */
public final class TimeUtils {

    private static final long MINUTE = 60 * 1000L;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;

    private static final SimpleDateFormat FULL_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat TIME_ONLY =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    private TimeUtils() {
    }

    /**
     * 相对时间：刚刚 / x分钟前 / x小时前 / 昨天 HH:mm / MM-dd / yyyy-MM-dd
     */
    public static String formatRelative(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 0) {
            // 设备时间被改过的极端情况，直接按完整时间显示
            return FULL_FORMAT.format(new Date(timestamp));
        }
        if (diff < MINUTE) {
            return "刚刚";
        }
        if (diff < HOUR) {
            return (diff / MINUTE) + "分钟前";
        }
        if (diff < DAY) {
            return (diff / HOUR) + "小时前";
        }
        if (isYesterday(timestamp)) {
            return "昨天 " + TIME_ONLY.format(new Date(timestamp));
        }
        if (isSameYear(timestamp)) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timestamp);
            return (c.get(Calendar.MONTH) + 1) + "月" + c.get(Calendar.DAY_OF_MONTH) + "日";
        }
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public static String formatFull(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        return FULL_FORMAT.format(new Date(timestamp));
    }

    /** 帖子列表右侧那个精确到分钟的短时间 */
    public static String formatShort(long timestamp) {
        if (timestamp <= 0) {
            return "";
        }
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < DAY && !isYesterday(timestamp)) {
            return TIME_ONLY.format(new Date(timestamp));
        }
        return DATE_FORMAT.format(new Date(timestamp));
    }

    private static boolean isYesterday(long timestamp) {
        Calendar today = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);

        int todayYear = today.get(Calendar.YEAR);
        int todayDay = today.get(Calendar.DAY_OF_YEAR);
        int targetYear = target.get(Calendar.YEAR);
        int targetDay = target.get(Calendar.DAY_OF_YEAR);

        if (todayYear != targetYear) {
            // 跨年时判断昨天要特殊处理，1月1日的昨天是去年12月31日
            return todayDay == 1 && targetDay >= 365;
        }
        return todayDay - targetDay == 1;
    }

    private static boolean isSameYear(long timestamp) {
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(timestamp);
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR);
    }
}
