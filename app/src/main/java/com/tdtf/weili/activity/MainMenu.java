package com.tdtf.weili.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.Utils.Transform;
import com.tdtf.weili.api.SerialPort;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.service.MyService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainMenu extends AppCompatActivity {
    SharedPreferences perferences;
    SharedPreferences infomation;
    FileOutputStream mOutputStream,mixOUtputStream;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    MyDiary myDiary = new MyDiary();
    MyDatabaseHelper dbHelper;
    boolean flag = true;
    int speed = Myutils.getMixSpeed();

    SerialPort spp;

    Button btnjccz;
    Button btnjcsz;
    Button btnqxcz;
    Button btntdsz;
    Button btnqtsz;
    Button btntdbd;
    ProgressBar bar;
    Button mixdown;
    Button mixup;
    TextView info_0;
    TextView info_1;
    TextView info_2;
    TextView info_3;
    TextView info_4;
    TextView info_5;
    TextView info_6;
    TextView info_7;
    TextView textPress;

    /*TODO 读取串口的数据处理*/
    private class DataReceived implements Runnable {
        @Override
        public void run() {
            if (stringBuffer.length() != 0) {
                String msg = stringBuffer.toString();
                stringBuffer.delete(0, stringBuffer.length());
                if (msg.length() == 16) {//压力检测
                    if (msg.substring(0, 6).equals("aa0023") &&
                            msg.substring(msg.length() - 8).equals("cc33c33c")) {
                        int pressure = Integer.parseInt(msg.substring(6, 8), 16);
                        textPress = (TextView) findViewById(R.id.textpressclean_0);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                myBinder = (MyService.MyBinder) service;
                myService = myBinder.getService();
                myBinder.threadGo();
                myBinder.press();
                myService.setValues(new MyService.CallBacks() {
                    @Override
                    public void startRead(StringBuffer strBuffer) {
                        stringBuffer = strBuffer;
                        handler.post(dataReceived);
                    }

                    @Override
                    public void output(FileOutputStream outputStream) {
                        mOutputStream = outputStream;
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

        final TextClock textClock = (TextClock) findViewById(R.id.textClock13);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_0);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_0);
        textUser.setText("用户名:\n" + Myutils.getUsername());
        dbHelper = new MyDatabaseHelper(this);
        myDiary.diary(dbHelper, Diary.mainMenu_start);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setProgress(speed);
        info_0 = (TextView) findViewById(R.id.text_info_0);
        info_1 = (TextView) findViewById(R.id.text_info_1);
        info_2 = (TextView) findViewById(R.id.text_info_2);
        info_3 = (TextView) findViewById(R.id.text_info_3);
        info_4 = (TextView) findViewById(R.id.text_info_4);
        info_5 = (TextView) findViewById(R.id.text_info_5);
        info_6 = (TextView) findViewById(R.id.text_info_6);
        info_7 = (TextView) findViewById(R.id.text_info_7);
        perferences = getSharedPreferences("jianceshezhi", MODE_PRIVATE);
        info_0.setText(perferences.getString("jcfs", ""));
        info_1.setText(perferences.getString("yuzou", "") + "ml");
        info_2.setText(perferences.getString("qyfs", ""));
        info_3.setText(perferences.getString("quyang", "") + "ml");
        info_4.setText(perferences.getString("jccs", "") + "次");
        info_5.setText(perferences.getString("jishu", "") + "ml");
        info_7.setText(String.valueOf((float)Integer.parseInt(perferences.getString("qywz", ""))/10) + "Bar");
        infomation = getSharedPreferences("checkBoxState", MODE_PRIVATE);
        if (infomation.getInt("rg", 0) == infomation.getInt("rbtn0", 0)) {
            info_6.setText(R.string.passopt2);
        } else if (infomation.getInt("rg", 0) == infomation.getInt("rbtn1", 0)) {
            info_6.setText(R.string.passopt3);
        } else if (infomation.getInt("rg", 0) == infomation.getInt("rbtn2", 0)) {
            info_6.setText(R.string.passopt4);
        } else if (infomation.getInt("rg", 0) == infomation.getInt("rbtn3", 0)) {
            info_6.setText(R.string.passopt5);
        } else if (infomation.getInt("rg", 0) == infomation.getInt("rbtn4", 0)) {
            info_6.setText(R.string.passopt6);
        } else if (infomation.getInt("rg", 0) == infomation.getInt("rbtn5", 0)) {
            info_6.setText(R.string.passopt7);
        }
        /*TODO 打开串口*/
        try {
            spp = new SerialPort(new File("/dev/ttyAMA3"), 9600, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mixOUtputStream = (FileOutputStream) spp.getOutputStream();

        mixdown = (Button) findViewById(R.id.btn_mix_down);
        mixdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speed < 2) {
                    speed = 2;
                }
                bar.setProgress(--speed);
                Myutils.setMixSpeed(speed);
                try {
                    for (int i = 0; i < SerialOrder.mix_speed(Transform.dec2hexTwo(String.valueOf(speed))).length(); i = i + 2) {
                        mixOUtputStream.write(Integer.parseInt(SerialOrder.mix_speed(Transform.dec2hexTwo(String.valueOf(speed))).substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mixup = (Button) findViewById(R.id.btn_mix_up);
        mixup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (speed > 23) {
                    speed = 23;
                }
                bar.setProgress(++speed);
                Myutils.setMixSpeed(speed);
                try {
                    for (int i = 0; i < SerialOrder.mix_speed(Transform.dec2hexTwo(String.valueOf(speed))).length(); i = i + 2) {
                        mixOUtputStream.write(Integer.parseInt(SerialOrder.mix_speed(Transform.dec2hexTwo(String.valueOf(speed))).substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btnjccz = (Button) findViewById(R.id.btnjccz);//检测操作
        btnjccz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (info_6.getText().toString().equals("8368滤除")) {
                    if (infomation.getInt("radioCompareLvchu", 0) == 0 && infomation.getInt("radioSaveLvchu", 0) == 0) {
                        Toast.makeText(MainMenu.this, "8368滤除未设置", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(MainMenu.this, DetectMenu.class);
                        startActivity(intent);
                        unbindService(serviceConnection);
                        finish();
                    }
                } else if (info_6.getText().toString().equals("8368_05")) {
                    if (infomation.getInt("radioCompare", 0) == 0 && infomation.getInt("radioSave", 0) == 0) {
                        Toast.makeText(MainMenu.this, "8368_05未设置", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(MainMenu.this, DetectMenu.class);
                        startActivity(intent);
                        unbindService(serviceConnection);
                        finish();
                    }
                } else if (infomation.getInt("rg", 0) == 0) {
                    Toast.makeText(MainMenu.this, "请先进行通道设置和检测设置", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(MainMenu.this, DetectMenu.class);
                    startActivity(intent);
                    unbindService(serviceConnection);
                    finish();
                }
            }
        });

        btnjcsz = (Button) findViewById(R.id.btnjcsz);//检测设置
        btnjcsz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, DetectOpt.class));
                unbindService(serviceConnection);
                finish();
            }
        });

        btnqxcz = (Button) findViewById(R.id.btnqxcz);//清洗操作
        btnqxcz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, CleanOption.class));
                unbindService(serviceConnection);
                finish();
            }
        });

        btntdsz = (Button) findViewById(R.id.btntdsz);//通道设置
        btntdsz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, PassOpt.class));
                unbindService(serviceConnection);
                finish();
            }
        });

        btnqtsz = (Button) findViewById(R.id.btnqtsz);//其他设置
        btnqtsz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, Option.class));
                unbindService(serviceConnection);
                finish();
            }
        });

        btntdbd = (Button) findViewById(R.id.btntdbd);//通道标定
        btntdbd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainMenu.this, Calibration.class));
                unbindService(serviceConnection);
                finish();
            }
        });
        try {
            for (int i = 0; i < SerialOrder.mix_speed(Transform.dec2hexTwo(String.valueOf(speed))).length(); i = i + 2) {
                mixOUtputStream.write(Integer.parseInt(SerialOrder.mix_speed(Transform.dec2hexTwo(String.valueOf(speed))).substring(i, i + 2), 16));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        spp.close();
        flag = false;
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
