package com.tdtf.weili.Utils;

/**
 * Created by a on 2017/9/8.
 */

public class Transform {
    /***
     * 字节转16进制字符串，长度2
     * @param b byte数组
     * @return 16进制字符串
     */
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp;
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    /***
     * 十进制字符串转十六进制字符串（浮点）长度4
     * @param s 浮点字符串
     * @return
     */
    public static String double2hex(String s) {
        String ssr;
        String fusion;
        double fla = Double.parseDouble(s) * 10;
        int hex = (int) fla;
        ssr = Integer.toHexString(hex);
        switch (ssr.length()) {
            case 1:
                fusion = "000" + ssr;
                break;
            case 2:
                fusion = "00" + ssr;
                break;
            case 3:
                fusion = "0" + ssr;
                break;
            default:
                fusion = ssr;
                break;
        }
        return fusion;
    }

    /***
     * 十进制字符串转十六进制字符串（浮点）长度2
     * @param s 浮点字符串
     * @return
     */
    public static String float2hex(String s) {
        String ssr;
        String fusion;
        double fla = Double.parseDouble(s) * 10;
        int hex = (int) fla;
        ssr = Integer.toHexString(hex);
        if (ssr.length() == 1) {
            fusion = "0" + ssr;
        } else {
            fusion = ssr;
        }
        return fusion;
    }

    /**
     * 十进制字符串转十六进制字符串（Integer） 长度4
     *
     * @param s 整数字符串
     * @return
     */
    public static String dec2hexFour(String s) {
        String ssr;
        String fusion = "";
        int fla = Integer.parseInt(s);
        ssr = Integer.toHexString(fla);
        switch (ssr.length()) {
            case 1:
                fusion = "000" + ssr;
                break;
            case 2:
                fusion = "00" + ssr;
                break;
            case 3:
                fusion = "0" + ssr;
                break;
            default:
                break;
        }
        return fusion;
    }

    /**
     * 十进制字符串转十六进制字符串（Integer) 长度两位
     * @param s 整数字符串
     * @return
     */
    public static String dec2hexTwo(String s) {
        String ssr;
        String fusion;
        int fla = Integer.parseInt(s);
        ssr = Integer.toHexString(fla);
        if (ssr.length() == 1) {
            fusion = "0" + ssr;
        } else {
            fusion = ssr;
        }
        return fusion;
    }
}
