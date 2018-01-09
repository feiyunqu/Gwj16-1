/*TODO 线程关闭是个大问题，没解决利索*/
package com.tdtf.weili.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.api.SerialPort;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.information.Initial;
import com.tdtf.weili.service.MyService;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    FileOutputStream mOutputStream;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    ServiceConnection serviceConnection;

    TextView textSample;
    TextView textSensor;
    TextView textFar;

    /*TODO 读取串口的数据处理*/
    private class DataReceived implements Runnable {
        @Override
        public void run() {
            if (stringBuffer.length() != 0) {
                String msg = stringBuffer.toString();
                stringBuffer.delete(0, stringBuffer.length());
                if (msg.length() < 14) {
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                switch (msg) {
                    case SerialOrder.ORDER_SENSORSTATE:
                        myDiary.diary(dbHelper, Diary.mainActivity_sensorOk);
                        textSensor.setBackgroundResource(R.drawable.mainactivity_05);
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
                        unbindService(serviceConnection);
                        finish();
                        break;
                    case SerialOrder.ORDER_RELEASE:
                        try {
                            for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SerialOrder.ORDER_UNOKSENSOR://
                        textSensor.setBackgroundResource(R.drawable.mainactivity_08);
                        Toast.makeText(getApplicationContext(), "传感器状态异常，请调整至正常状态再重启", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
//                if (msg.equals(SerialOrder.ORDER_SENSORSTATE)) {//传感器检测
//                    myDiary.diary(dbHelper, Diary.mainActivity_sensorOk);
//                    textSensor.setBackgroundResource(R.drawable.mainactivity_05);
//                    Intent intent = new Intent(MainActivity.this, Login.class);
//                    startActivity(intent);
//                    unbindService(serviceConnection);
//                    finish();
//                }

//                if (msg.equals(SerialOrder.ORDER_RELEASE)) {
//                    try {
//                        for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
//                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

//                if (msg.equals(SerialOrder.ORDER_UNOKSENSOR)) {
//                    textSensor.setBackgroundResource(R.drawable.mainactivity_08);
//                    Toast.makeText(getApplicationContext(), "传感器状态异常，请调整至正常状态再重启", Toast.LENGTH_SHORT).show();
//                }
                if (msg.length() == 16) {//压力检测
                    myDiary.diary(dbHelper, Diary.mainActivity_pressOk);
                    if (msg.substring(0, 6).equals("aa0023") &&
                            msg.substring(msg.length() - 8).equals("cc33c33c")) {
                        if (Integer.parseInt(msg.substring(6, 8), 16) < 5) {
                            textFar.setBackgroundResource(R.drawable.mainactivity_06);
                            try {
                                for (int i = 0; i < SerialOrder.ORDER_SENSORSTATE.length(); i = i + 2) {
                                    mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_SENSORSTATE.substring(i, i + 2), 16));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                                    mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyService.MyBinder myBinder = (MyService.MyBinder) service;
                MyService myService = myBinder.getService();
                myService.setValues(new MyService.CallBacks() {
                    @Override
                    public void startRead(StringBuffer strBuffer) {
                        stringBuffer = strBuffer;
                        handler.post(dataReceived);
                    }

                    @Override
                    public void output(FileOutputStream outputStream) {
                        mOutputStream = outputStream;
                        try {
                            for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        startService(new Intent(this, MyService.class));
        Intent bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

        dbHelper = new MyDatabaseHelper(this);
        myDiary.diary(dbHelper, Diary.mainActivity_start);
        Initial initial = new Initial();
        textSensor = (TextView) findViewById(R.id.textSensor);
        textSample = (TextView) findViewById(R.id.textSample);
        textFar = (TextView) findViewById(R.id.textFar);
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery("select * from Power ", null);
        if (!cursor.moveToFirst()) {
            initial.initialization(this);
            initial.datainitial(dbHelper);
        }
        //initial.updatainitial(dbHelper);
        cursor.close();

        textSample.setBackgroundResource(R.drawable.mainactivity_04);
        myDiary.diary(dbHelper, Diary.mainActivity_sampleOk);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
