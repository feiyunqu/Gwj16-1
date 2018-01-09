package com.tdtf.weili.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String SQLITE_DB_NAME = "weili16.db";

    public MyDatabaseHelper(Context context) {
        super(context, SQLITE_DB_NAME, null, 1);
    }

    private static final String SQLITE_DB_DATA = "create table Data(" +
            "_id integer primary key autoincrement," +
            "particle varchar(50)," +//粒径
            "differential varchar(50)," +//微分
            "integral varchar(50)," +//积分
            "sid varchar(50))";//标识
    private static final String SQLITE_DB_DATA_MAZUI = "create table DataMazui(" +
            "_id integer primary key autoincrement," +
            "number46 varchar(50)," +//46
            "number05 varchar(50)," +//05
            "sid varchar(50))";//标识
    private static final String SQLITE_DB_DATA_LVCHU = "create table DataLvchu(" +
            "_id integer primary key autoincrement," +
            "number varchar(50)," +//20
            "sid varchar(50))";//标识
    private static final String SQLITE_DB_DATA_WURAN = "create table DataWuran(" +
            "_id integer primary key autoincrement," +
            "number25 varchar(50)," +//25
            "number50 varchar(50)," +//50
            "number100 varchar(50)," +//100
            "sid varchar(50))";//标识
    private static final String SQLITE_DB_DATA_WR = "create table DataWR(" +
            "_id integer primary key autoincrement," +
            "number15 varchar(50)," +//15
            "number25 varchar(50)," +//25
            "sid varchar(50))";//标识
    private static final String SQLITE_DB_DATA_YAODIAN = "create table DataYaodian(" +
            "_id integer primary key autoincrement," +
            "number15 varchar(50)," +//15
            "number25 varchar(50)," +//25
            "sid varchar(50))";//标识
    private static final String SQLITE_DB_DATANAME = "create table Dataname(" +
            "_id integer primary key autoincrement," +
            "name varchar(50)," +//样品名称
            "pihao," +//样品批号
            "first varchar(50)," +//第一次
            "second varchar(50)," +//二
            "third varchar(50)," +//三
            "fourth varchar(50)," +//四
            "fifth varchar(50)," +//五
            "sixth varchar(50)," +//六
            "seventh varchar(50)," +//七
            "eighth varchar(50)," +//八
            "ninth varchar(50)," +//九
            "tenth varchar(50)," +//十
            "eleventh varchar(50)," +//十一
            "twelfth varchar(50)," +//十二
            "average varchar(50)," +//均值
            "fragment varchar(50)," +//不同的fragment(0.1.2.3.4.5)(自定义，麻醉，lvchu，污染05，污染98，药典)
            "fragcode varchar(50)," +//对应flagment的号（0.1.2.3.4.5）
            "wuran," +//污染指数
            "lvchu," +//滤除率
            "dateTime long)";//实验日期
    private static final String SQLITE_DB_USER = "create table User(" +
            "_id integer primary key autoincrement," +
            "userPower int," +//用户权限？
            "registerTime long," +//注册日期or信息更新日期
            "name varchar(50)," +//权限名称
            "userName varchar(100) not null unique," +//用户名
            "password varchar(100))";//登录密码
    private static final String SQLITE_DB_DIARY = "create table Diary(" +
            "_id integer primary key autoincrement," +
            "dateTime long," +//时间日期
            "context varchar(100)," +//操作内容
            "powerName varchar(50)," +//权限名称
            "userName varchar(100))"; //用户名称
    private static final String SQLITE_DB_POWER = "create table Power(" +
            "_id integer primary key autoincrement," +
            "powerName varchar(50)," +//权限名称
            "powerData varchar(50))";//权限数据

    @Override
    public void onCreate(SQLiteDatabase db) {
        //第一次使用数据库时自动建表
        db.execSQL(SQLITE_DB_DATA);
        db.execSQL(SQLITE_DB_DATA_MAZUI);
        db.execSQL(SQLITE_DB_DATA_LVCHU);
        db.execSQL(SQLITE_DB_DATA_WURAN);
        db.execSQL(SQLITE_DB_DATA_WR);
        db.execSQL(SQLITE_DB_DATA_YAODIAN);
        db.execSQL(SQLITE_DB_DATANAME);
        db.execSQL(SQLITE_DB_USER);
        db.execSQL(SQLITE_DB_DIARY);
        db.execSQL(SQLITE_DB_POWER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("--------onUpgrade Called--------" + oldVersion + "--->" + newVersion);
    }
}
