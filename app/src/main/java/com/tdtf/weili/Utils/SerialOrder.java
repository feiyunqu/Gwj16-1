package com.tdtf.weili.Utils;

/**
 * Created by a on 2017/2/21.
 * 串口各个指令
 */

public class SerialOrder {
    public static final String ORDER_HANDSHAKING ="aa0030cc33c33c";//握手
    public static final String ORDER_CLEAN       ="aa000ecc33c33c";//开始清洗
    public static final String ORDER_STOP        ="aa000fcc33c33c";//中止
    public static final String ORDER_SAMPLEBACK  ="aa0017cc33c33c";//取样器归位
    public static final String ORDER_ZERO        ="aa0018cc33c33c";//取样器回收
    public static final String ORDER_DESCLEAN    ="aa001fcc33c33c";//反冲
    public static final String ORDER_SENSORSTATE ="aa001bcc33c33c";//传感器状态
    public static final String ORDER_VALVESTATE  ="aa001ccc33c33c";//阀状态
    public static final String ORDER_SIZE        ="aa001dcc33c33c";//体积测定
    public static final String ORDER_RESUME      ="aa0020cc33c33c";//继续、确认
    public static final String ORDER_RELEASE     ="aa0022cc33c33c";//排压
    public static final String ORDER_PRESSURE    ="aa0023cc33c33c";//读取压力值
    public static final String ORDER_NOISE       ="aa0024cc33c33c";//噪声测试

    public static final String ORDER_UNOKCLEAN   ="aa008ecc33c33c";//清洗返回错误
    public static final String ORDER_UNOKSTART   ="aa009dcc33c33c";//检测异常错误
    public static final String ORDER_UNOKSENSOR  ="aa009bcc33c33c";//传感器异常
    public static final String ORDER_UNOKVALVE   ="aa009ccc33c33c";//阀异常
    //public static final String ORDER_UNOKSIZE  ="aa009dcc33c33c";//体积检测异常
    public static final String ORDER_UNOKDESCLEAN="aa009fcc33c33c";//反冲异常
    public static final String ORDER_UNOKDOWN    ="aa0099cc33c33c";//取样器下降异常
    public static final String ORDER_UNOKSAMPLE  ="aa004fcc33c33c";//取样异常
    public static final String ORDER_UNOKPRESS   ="aa00a1cc33c33c";//加压异常

    public static String preQuantity(String string)  {return "aa0001"+string+"cc33c33c";}//预走量
    public static String sampling(String string)     {return "aa0002"+string+"cc33c33c";}//取样量
    public static String sample_speed(String string) {return "aa0003"+string+"cc33c33c";}//取样速度
    public static String back_speed(String string)   {return "aa0004"+string+"cc33c33c";}//回推速度
    public static String clean_speed(String string)  {return "aa0005"+string+"cc33c33c";}//清洗速度
    public static String mix_speed(String string)    {return "aa"+string+"cc33c33c";}//搅拌速度
    public static String passWay(String string)      {return "aa0007"+string+"cc33c33c";}//监测通道数
    public static String start(String string)        {return "aa000d"+string+"cc33c33c";}//开始检测
    public static String downing(String string)      {return "aa0019"+string+"cc33c33c";}//取样器下降量
    public static String press(String string)        {return "aa0021"+string+"cc33c33c";}//加压
    public static String special_speed(String string){return "aa0025"+string+"cc33c33c";}//取样速度8368

    public static final String ORDER_OKPREPARING    ="aa0001cc33c33c";//预走量接受正常返回
    public static final String ORDER_OKSAMPLING     ="aa0002cc33c33c";//取样量接受正常返回
    public static final String ORDER_OKSAMPLE_SPEED ="aa0003cc33c33c";//取样速度接受正常返回
    public static final String ORDER_OKBACK_SPEED   ="aa0004cc33c33c";//回推速度接受正常返回
    public static final String ORDER_OKCLEAN_SPEED  ="aa0005cc33c33c";//清洗速度接受正常返回
    public static final String ORDER_OKPASSWAY      ="aa0007cc33c33c";//设置通道数正常返回
    public static final String ORDER_OKTHRESHOLD    ="aa0008cc33c33c";//设置N个通道对应门限值正常返回
    public static final String ORDER_OKSTART        ="aa000dcc33c33c";//开始检测
//    public static final String ORDER_OKDOWN       ="aa0019cc33c33c";//取样头下降正常返回
    public static final String ORDER_OKPRESS        ="aa0021cc33c33c";//加压正常返回
    public static final String ORDER_OKSPECIAL_SPEED="aa0025cc33c33c";//取样速度8368接受正常返回
}
