package com.tdtf.weili.Utils;

import android.util.Log;

/**
 * Created by a on 2017/4/18.
 * String[] name = new String[]{"开始检测", "打印设置", "检测方式", "取样方式", "预走量", "取样量",
 * "检测次数", "取样位置", "计数单位", "麻醉器具", "8386滤除", "8386-05污染", "8386-98污染",
 * "中国药典", "自定义", "自定义设置", "开机清洗设置", "关机清洗设置", "反冲", "标定操作","标尺设置",
 * "标定参数","噪声测定","用户设置","修正参数"}
 */

public class Power {
    private static Boolean flag;
    public static Boolean getFlag(String str,String name) {
        switch (name) {
            case "开始检测":flag = str.substring(0, 1).equals("1");
                break;
            case "打印设置":flag = str.substring(1, 2).equals("1");
                break;
            case "检测方式":flag = str.substring(2, 3).equals("1");
                break;
            case "取样方式":flag = str.substring(3, 4).equals("1");
                break;
            case "预走量":flag = str.substring(4, 5).equals("1");
                break;
            case "取样量":flag = str.substring(5, 6).equals("1");
                break;
            case "检测次数":flag = str.substring(6, 7).equals("1");
                break;
            case "取样位置":flag = str.substring(7, 8).equals("1");
                break;
            case "计数单位":flag = str.substring(8, 9).equals("1");
                break;
            case "麻醉器具":flag = str.substring(9, 10).equals("1");
                break;
            case "8386滤除":flag = str.substring(10, 11).equals("1");
                break;
            case "8386-05污染":flag = str.substring(11, 12).equals("1");
                break;
            case "8386-98污染":flag = str.substring(12, 13).equals("1");
                break;
            case "中国药典":flag = str.substring(13, 14).equals("1");
                break;
            case "自定义":flag = str.substring(14, 15).equals("1");
                break;
            case "自定义设置":flag = str.substring(15, 16).equals("1");
                break;
            case "开机清洗设置":flag = str.substring(16, 17).equals("1");
                break;
            case "关机清洗设置":flag = str.substring(17, 18).equals("1");
                break;
            case "反冲":flag = str.substring(18, 19).equals("1");
                break;
            case "标定操作":flag = str.substring(19, 20).equals("1");
                break;
            case "标尺设置":flag = str.substring(20, 21).equals("1");
                break;
            case "标定参数":flag = str.substring(21, 22).equals("1");
                break;
            case "噪声测定":flag = str.substring(22, 23).equals("1");
                break;
            case "用户设置":flag = str.substring(23, 24).equals("1");
                break;
            case "修正参数":flag = str.substring(24, 25).equals("1");
                break;
        }
        return flag;
    }
}

