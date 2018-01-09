package com.tdtf.weili.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class Speed extends AppCompatActivity {
    SharedPreferences coordinate;
    Button btnsave;
    Button btnback;
    EditText[] editTexts;
    FileOutputStream mOutputStream;

    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();

    String[] str_speed;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    TextView textPress;
    boolean flag_limit = true;

    /*TODO 读取串口的数据处理*/
    private class DataReceived implements Runnable {
        @Override
        public void run() {
            if (stringBuffer.length() != 0) {
                String msg = stringBuffer.toString();
                stringBuffer.delete(0, stringBuffer.length());
                if (msg.equals(SerialOrder.ORDER_OKSAMPLE_SPEED)) {
                    try {
                        for (int i = 0; i < SerialOrder.back_speed(Transform.dec2hexTwo(editTexts[1].getText().toString())).length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.back_speed(Transform.dec2hexTwo(editTexts[1].getText().toString())).substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.equals(SerialOrder.ORDER_OKBACK_SPEED)) {
                    try {
                        for (int i = 0; i < SerialOrder.clean_speed(Transform.dec2hexTwo(editTexts[2].getText().toString())).length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.clean_speed(Transform.dec2hexTwo(editTexts[2].getText().toString())).substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.equals(SerialOrder.ORDER_OKCLEAN_SPEED)) {
                    try {
                        for (int i = 0; i < SerialOrder.special_speed(Transform.dec2hexTwo(editTexts[3].getText().toString())).length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.special_speed(Transform.dec2hexTwo(editTexts[3].getText().toString())).substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (msg.equals(SerialOrder.ORDER_OKSPECIAL_SPEED)) {
                    String[] strings = new String[]{
                            editTexts[0].getText().toString(),
                            editTexts[1].getText().toString(),
                            editTexts[2].getText().toString(),
                            editTexts[3].getText().toString()
                    };
                    setSharedPreference("speeds", strings);
                    Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Speed.this, Calibration.class));
                    unbindService(serviceConnection);
                    finish();
                }
                if (msg.length() == 16) {//压力检测
                    if (msg.substring(0, 6).equals("aa0023") &&
                            msg.substring(msg.length() - 8).equals("cc33c33c")) {
                        int pressure = Integer.parseInt(msg.substring(6, 8), 16);
                        textPress = (TextView) findViewById(R.id.text_press_11);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed);
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
        myDiary.diary(dbHelper, Diary.speed_start);
        final TextClock textClock = (TextClock) findViewById(R.id.textClock14);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_13);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_13);
        textUser.setText("用户名:\n" + Myutils.getUsername());
        editTexts = new EditText[4];
        editTexts[0] = (EditText) findViewById(R.id.edit_speed_sample);
        editTexts[1] = (EditText) findViewById(R.id.edit_speed_back);
        editTexts[2] = (EditText) findViewById(R.id.edit_speed_clean);
        editTexts[3] = (EditText) findViewById(R.id.edit_speed_special);
        str_speed = getSharedPreference("speeds");
        editTexts[0].setText(str_speed[0]);
        editTexts[1].setText(str_speed[1]);
        editTexts[2].setText(str_speed[2]);
        editTexts[3].setText(str_speed[3]);
        editTexts[0].setSelection(editTexts[0].getText().length());
        editTexts[1].setSelection(editTexts[1].getText().length());
        editTexts[2].setSelection(editTexts[2].getText().length());
        editTexts[3].setSelection(editTexts[3].getText().length());

        editTexts[0].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(editTexts[0].getText())) {
                        if (!(editTexts[0].getText().toString().startsWith(".") || editTexts[0].getText().toString().endsWith("."))) {
                            BigDecimal bigDecimal = new BigDecimal(editTexts[0].getText().toString());
                            if (bigDecimal.compareTo(new BigDecimal("5")) == -1) {
                                editTexts[0].setText("5");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("60")) == 1) {
                                    editTexts[0].setText("60");
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(Speed.this, "取样速度输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.speed_quYang(editTexts[0].getText().toString()));
                    }
                    btnsave.setFocusableInTouchMode(false);
                }
            }
        });
        editTexts[1].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(editTexts[1].getText())) {
                        if (!(editTexts[1].getText().toString().startsWith(".") || editTexts[1].getText().toString().endsWith("."))) {
                            BigDecimal bigDecimal = new BigDecimal(editTexts[1].getText().toString());
                            if (bigDecimal.compareTo(new BigDecimal("5")) == -1) {
                                editTexts[1].setText("5");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("60")) == 1) {
                                    editTexts[1].setText("60");
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(Speed.this, "回推速度输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.speed_huiTui(editTexts[1].getText().toString()));
                    }
                    btnsave.setFocusableInTouchMode(false);
                }
            }
        });
        editTexts[2].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(editTexts[2].getText())) {
                        if (!(editTexts[2].getText().toString().startsWith(".") || editTexts[2].getText().toString().endsWith("."))) {
                            BigDecimal bigDecimal = new BigDecimal(editTexts[2].getText().toString());
                            if (bigDecimal.compareTo(new BigDecimal("5")) == -1) {
                                editTexts[2].setText("5");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("60")) == 1) {
                                    editTexts[2].setText("60");
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(Speed.this, "清洗速度输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.speed_qingXi(editTexts[2].getText().toString()));
                    }
                    btnsave.setFocusableInTouchMode(false);
                }
            }
        });
        editTexts[3].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(editTexts[3].getText())) {
                        if (!(editTexts[3].getText().toString().startsWith(".") || editTexts[3].getText().toString().endsWith("."))) {
                            BigDecimal bigDecimal = new BigDecimal(editTexts[3].getText().toString());
                            if (bigDecimal.compareTo(new BigDecimal("5")) == -1) {
                                editTexts[3].setText("5");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("150")) == 1) {
                                    editTexts[3].setText("150");
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(Speed.this, "8368_05取样速度输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.speed_quYang05(editTexts[3].getText().toString()));
                    }
                    btnsave.setFocusableInTouchMode(false);
                }
            }
        });

        btnsave = (Button) findViewById(R.id.btn_save);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnsave.setFocusableInTouchMode(true);
                btnsave.requestFocusFromTouch();
                boolean flag_null = true;
                for (int l = 0; l < 4; l++) {
                    if (TextUtils.isEmpty(editTexts[l].getText())) {
                        flag_null = false;
                        Toast.makeText(Speed.this, "输入不能为空",
                                Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        flag_null = true;
                    }
                }
                if (flag_null && flag_limit) {
                    myDiary.diary(dbHelper, Diary.speed_save);
                    try {
                        for (int i = 0; i < SerialOrder.sample_speed(Transform.dec2hexTwo(editTexts[0].getText().toString())).length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.sample_speed(Transform.dec2hexTwo(editTexts[0].getText().toString())).substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        btnback = (Button) findViewById(R.id.btn_back);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.speed_back);
                startActivity(new Intent(Speed.this, Calibration.class));
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

    public void setSharedPreference(String key, String[] values) {
        String regularEx = "#";
        String str = "";
        SharedPreferences coefficient;

        coefficient = getSharedPreferences("arrayxishu", MODE_PRIVATE);
        SharedPreferences.Editor editor;
        if (values != null && values.length > 0) {
            for (String value : values) {
                str += value;
                str += regularEx;
            }
            editor = coefficient.edit();
            editor.putString(key, str);
            editor.apply();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                }
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
