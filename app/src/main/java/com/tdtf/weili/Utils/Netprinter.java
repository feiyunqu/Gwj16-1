package com.tdtf.weili.Utils;

/**
 * Created by a on 2017/5/15.
 * printer
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Netprinter {
    // TODO: 2017/7/13 汉字
    public void printtitle(FileOutputStream mOutputStream, String title) throws IOException {
        mOutputStream.write(title.getBytes("gbk"));
    }
    // TODO: 2017/7/13 数字
    public void printdatalijing(FileOutputStream mOutputStream, String title,int len) throws IOException {
        mOutputStream.write(27);
        mOutputStream.write(102);
        mOutputStream.write(0);//m
        mOutputStream.write(len-title.length());//n
        mOutputStream.write(title.getBytes("gbk"));
    }
    public void printdatajifen(FileOutputStream mOutputStream, String title,int len) throws IOException {
        mOutputStream.write(27);
        mOutputStream.write(102);
        mOutputStream.write(0);//m
        mOutputStream.write(len-title.length());//n
        mOutputStream.write(title.getBytes("gbk"));
    }
    public void printdataweifen(FileOutputStream mOutputStream, String title,int len) throws IOException {
        mOutputStream.write(27);
        mOutputStream.write(102);
        mOutputStream.write(0);//m
        mOutputStream.write(len-title.length());//n
        mOutputStream.write(title.getBytes("gbk"));
    }
    public void printdatabiaochi(FileOutputStream mOutputStream, String title,int len) throws IOException {
        mOutputStream.write(27);
        mOutputStream.write(102);
        mOutputStream.write(0);//m
        mOutputStream.write(len-title.length());//n
        mOutputStream.write(title.getBytes("gbk"));
    }

    // TODO: 2017/7/13 直方图
    // TODO: 2017/7/13 缩进 1b 66 空格或换行 m=00空格模式 m=01换行模式 n=02两个空格
    public void printindent (FileOutputStream mOutputStream) throws IOException {
        mOutputStream.write(0x1b);
        mOutputStream.write(0x66);
        mOutputStream.write(0x00);//m
        mOutputStream.write(0x01);//n
    }
    // TODO: 2017/7/13 间隔
    public void printblank (FileOutputStream mOutputStream) throws IOException {
        mOutputStream.write(0x1b);
        mOutputStream.write(0x66);
        mOutputStream.write(0x00);//m
        mOutputStream.write(0x07);//n
    }

    // TODO: 2017/8/14 标定间隔
    public void printblank_bd (FileOutputStream mOutputStream) throws IOException {
        mOutputStream.write(0x1b);
        mOutputStream.write(0x66);
        mOutputStream.write(0x00);//m
        mOutputStream.write(0x04);//n
    }
    // TODO: 2017/7/13 换行 
    public void printline (FileOutputStream mOutputStream) throws IOException {
        mOutputStream.write(0x0a);//n
    }
    // TODO: 2017/7/13 平均值
}
