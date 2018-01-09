package com.tdtf.weili.Utils;

import android.database.Cursor;
import android.util.Log;

import com.tdtf.weili.database.MyDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by a on 2017/2/20.
 */

public class Myutils {
    //    public static final String[] POWER_CLASS = {"管理员", "操作员", "维护员"};
    public static final String[] DATA_TIMES = {"first", "second", "third", "fourth", "fifth",
            "sixth", "seventh", "eighth", "ninth", "tenth", "eleventh", "twelfth", "average"};
    public static final String[] BUTTON_NAME = {
            "开始检测", "打印设置", "检测方式", "取样方式", "预走量",
            "取样量", "检测次数", "取样位置", "计数单位", "麻醉器具",
            "8386滤除", "8386-05污染", "8386-98污染", "中国药典", "自定义",
            "自定义设置", "开机清洗设置", "关机清洗设置", "反冲", "标定操作",
            "标尺设置", "标定参数", "噪声测定", "用户设置", "修正参数"};

    public static String formatDate(long time) {
        String part = "yyyy-MM-dd";
        SimpleDateFormat formatter = new SimpleDateFormat(part, Locale.getDefault());
        return formatter.format(new Date(time));
    }

    public static String formatDateTime(long time) {
        String part = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(part, Locale.getDefault());
        return formatter.format(new Date(time));
    }

//    public static String formatShortTime(long time) {
//        int count = (int) (time / 1000);
//        int hour = count / 3600;
//        int minute = count % 3600 / 60;
//        int second = count % 60;
//        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
//    }

    private static String powerstring;

    public static void setPowerstring(String powerstring) {
        Myutils.powerstring = powerstring;
    }

    public static String getPowerstring(MyDatabaseHelper myDatabaseHelper, String name) {
        if (powerstring == null) {
            try {
                Cursor cursor = myDatabaseHelper.getReadableDatabase().rawQuery(
                        "select _id,powerData from Power where powerName=?", new String[]{name});
                while (cursor.moveToNext()) {
                    powerstring = cursor.getString(cursor.getColumnIndex("powerData"));
                }
                cursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return powerstring;
    }

    private static String powername = "";

    public static void setPowername(String powername) {
        Myutils.powername = powername;
    }

    public static String getPowername() {
        return powername;
    }

    private static String username = "";

    public static void setUsername(String username) {
        Myutils.username = username;
    }

    public static String getUsername() {
        return username;
    }

    private static int mixSpeed=8;

    public static int getMixSpeed() {
        return mixSpeed;
    }

    public static void setMixSpeed(int mixSpeed) {
        Myutils.mixSpeed = mixSpeed;
    }
}
