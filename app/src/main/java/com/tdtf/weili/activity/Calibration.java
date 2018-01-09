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
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.Utils.Power;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.service.MyService;

import java.io.FileOutputStream;
import java.io.IOException;

import static android.text.InputType.TYPE_NULL;

public class Calibration extends AppCompatActivity {
    SharedPreferences coordinate;
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    Button btnbcsz;
    Button btnbdcz;
    Button btnbdcs;
    Button btnzscd;
    Button btnuser;
    Button btnxzcs;
    Button btnback;
    TextView textdqbc;
    TextView textback;
    TextView textclean;
    TextView text8368;
    TextView[] textView;
    String[] str_biaochi, str_t, str_tt, str_speed;

    TextView textPress;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    FileOutputStream mOutputStream;

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
                        textPress = (TextView) findViewById(R.id.text_press_1);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure / 10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
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

        dbHelper = new MyDatabaseHelper(this);
        myDiary.diary(dbHelper, Diary.calibration_start);
        textdqbc = (TextView) findViewById(R.id.text_dqbc);
        textView = new TextView[3];
        textView[0] = (TextView) findViewById(R.id.text_bd_point);
        textView[1] = (TextView) findViewById(R.id.text_bd_speed);
        textView[2] = (TextView) findViewById(R.id.text_bdff);
        ///////////////////////////////titleInfo
        final TextClock textClock = (TextClock) findViewById(R.id.textClock2);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_1);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_1);
        textUser.setText("用户名:\n" + Myutils.getUsername());
        str_biaochi = getSharedPreference("selector");
        str_t = getSharedPreference("state_1");
        str_tt = getSharedPreference("state_2");
        str_speed = getSharedPreference("speeds");
        textdqbc.setText(str_biaochi[0]);
        switch (str_biaochi[0]) {
            case "标尺一":
                for (int l = 0; l < textView.length; l++) {
                    textView[l].setText(str_t[l]);
                }
                break;
            case "标尺二":
                for (int l = 0; l < textView.length; l++) {
                    textView[l].setText(str_tt[l]);
                }
                break;
            default:
                break;
        }
        textView[1].setText(str_speed[0]);
        textback = (TextView) findViewById(R.id.text_back_speed);
        textclean = (TextView) findViewById(R.id.text_clean_speed);
        text8368 = (TextView) findViewById(R.id.text8368);
        textback.setText(str_speed[1]);
        textclean.setText(str_speed[2]);
        text8368.setText(str_speed[3]);
        btnbcsz = (Button) findViewById(R.id.btn_bcsz);
        btnbcsz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[20])) {
                    startActivity(new Intent(Calibration.this, CalibrationOpt.class));
                    unbindService(serviceConnection);
                    finish();
                } else {
                    Toast.makeText(Calibration.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnbdcz = (Button) findViewById(R.id.btn_bdcz);
        btnbdcz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[19])) {
                    startActivity(new Intent(Calibration.this, Standard.class));
                    unbindService(serviceConnection);
                    finish();
                } else {
                    Toast.makeText(Calibration.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnbdcs = (Button) findViewById(R.id.btn_bdcs);
        btnbdcs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[21])) {
                    startActivity(new Intent(Calibration.this, Speed.class));
                    unbindService(serviceConnection);
                    finish();
                } else {
                    Toast.makeText(Calibration.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnzscd = (Button) findViewById(R.id.btn_zscd);
        btnzscd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[22])) {
                    startActivity(new Intent(Calibration.this, Noise.class));
                    unbindService(serviceConnection);
                    finish();
                } else {
                    Toast.makeText(Calibration.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnuser = (Button) findViewById(R.id.btn_user);
        btnuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[23])) {
                    startActivity(new Intent(Calibration.this, UserPower.class));
                    unbindService(serviceConnection);
                    finish();
                } else {
                    Toast.makeText(Calibration.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnxzcs = (Button) findViewById(R.id.btn_xzcs);
        btnxzcs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[24])) {
                    startActivity(new Intent(Calibration.this, Corrected.class));
                    unbindService(serviceConnection);
                    finish();
                } else {
                    Toast.makeText(Calibration.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnback = (Button) findViewById(R.id.btn_back);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.calibration_back);
                startActivity(new Intent(Calibration.this, MainMenu.class));
                unbindService(serviceConnection);
                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public String[] getSharedPreference(String key) {
        String regularEx = "#";
        String[] str;
        coordinate = getSharedPreferences("arrayxishu", MODE_PRIVATE);
        String values;
        values = coordinate.getString(key, "");
        str = values.split(regularEx);
        return str;
    }
}
