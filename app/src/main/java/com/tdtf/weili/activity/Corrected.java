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
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.service.MyService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class Corrected extends AppCompatActivity {
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    SharedPreferences changable;
    EditText[] edit_change;
    String[] strings_change;
    Button btn_save;
    Button btn_back;
//    boolean limit;
    boolean flag_limit;
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
                        textPress = (TextView) findViewById(R.id.text_press_3);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrected);
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
        myDiary.diary(dbHelper, Diary.correct_start);
        final TextClock textClock = (TextClock) findViewById(R.id.textClock17);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_4);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_4);
        textUser.setText("用户名:\n" + Myutils.getUsername());

        edit_change = new EditText[17];
        edit_change[0] = (EditText) findViewById(R.id.edit_change_0);
        edit_change[1] = (EditText) findViewById(R.id.edit_change_1);
        edit_change[2] = (EditText) findViewById(R.id.edit_change_2);
        edit_change[3] = (EditText) findViewById(R.id.edit_change_3);
        edit_change[4] = (EditText) findViewById(R.id.edit_change_4);
        edit_change[5] = (EditText) findViewById(R.id.edit_change_5);
        edit_change[6] = (EditText) findViewById(R.id.edit_change_6);
        edit_change[7] = (EditText) findViewById(R.id.edit_change_7);
        edit_change[8] = (EditText) findViewById(R.id.edit_change_8);
        edit_change[9] = (EditText) findViewById(R.id.edit_change_9);
        edit_change[10] = (EditText) findViewById(R.id.edit_change_10);
        edit_change[11] = (EditText) findViewById(R.id.edit_change_11);
        edit_change[12] = (EditText) findViewById(R.id.edit_change_12);
        edit_change[13] = (EditText) findViewById(R.id.edit_change_13);
        edit_change[14] = (EditText) findViewById(R.id.edit_change_14);
        edit_change[15] = (EditText) findViewById(R.id.edit_change_15);
        edit_change[16] = (EditText) findViewById(R.id.edit_change_16);

        for (int i = 0; i < 17; i++) {
            edit_change[i].setText(getSharedPreference("change")[i]);
            edit_change[i].setSelection(edit_change[i].length());
        }

        edit_change[0].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[0]);
            }
        });
        edit_change[1].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[1]);
            }
        });
        edit_change[2].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[2]);
            }
        });
        edit_change[3].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[3]);
            }
        });
        edit_change[4].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[4]);
            }
        });
        edit_change[5].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[5]);
            }
        });
        edit_change[6].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[6]);
            }
        });
        edit_change[7].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[7]);
            }
        });
        edit_change[8].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[8]);
            }
        });
        edit_change[9].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[9]);
            }
        });
        edit_change[10].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[10]);
            }
        });
        edit_change[11].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[11]);
            }
        });
        edit_change[12].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[12]);
            }
        });
        edit_change[13].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[13]);
            }
        });
        edit_change[14].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[14]);
            }
        });
        edit_change[15].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) judgement(edit_change[15]);
            }
        });
        edit_change[16].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                btn_save.setFocusableInTouchMode(false);
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(edit_change[16].getText())) {
                        if (!(edit_change[16].getText().toString().startsWith(".") || edit_change[16].getText().toString().endsWith("."))) {
                            BigDecimal bigDecimal = new BigDecimal(edit_change[16].getText().toString());
                            if (bigDecimal.compareTo(new BigDecimal("4600.0")) == -1) {
                                edit_change[16].setText("4600.0");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("5100.0")) == 1) {
                                    edit_change[16].setText("5100.0");
                                } else {
                                    boolean flag_edit = true;
                                    byte[] editByte = edit_change[16].getText().toString().getBytes();
                                    for (int k = 0; k < editByte.length; k++) {
                                        if (editByte[k] == '.') {
                                            flag_edit = false;
                                            break;
                                        }
                                    }
                                    if (flag_edit) {
                                        edit_change[16].getText().append(".0");
                                    }
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(Corrected.this, "基准输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.correct_jiZhun(edit_change[16].getText().toString()));
                    }
                }
            }
        });

        btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.correct_save);
                btn_save.setFocusableInTouchMode(true);
                btn_save.requestFocusFromTouch();
                boolean flag_null = true;
                for (int l = 0; l < 17; l++) {
                    if (TextUtils.isEmpty(edit_change[l].getText())) {
                        flag_null = false;
                        Toast.makeText(Corrected.this, "输入不能为空",
                                Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        flag_null = true;
                    }
                }
                if (flag_limit&&flag_null) {
                    strings_change = new String[17];
                    for (int i = 0; i < 17; i++) {
                        strings_change[i] = edit_change[i].getText().toString();
                    }
                    setSharedPreference("change", strings_change);
                    Toast.makeText(Corrected.this,"保存成功",Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Corrected.this, Calibration.class));
                    unbindService(serviceConnection);
                    finish();
                }
            }
        });

        btn_back = (Button) findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.correct_back);
                startActivity(new Intent(Corrected.this, Calibration.class));
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

    protected void judgement(EditText edittext) {
        if (!TextUtils.isEmpty(edittext.getText())) {
            if (!(edittext.getText().toString().startsWith(".") || edittext.getText().toString().endsWith("."))) {
                BigDecimal bigDecimal = new BigDecimal(edittext.getText().toString());
                if (bigDecimal.compareTo(new BigDecimal("-60.0")) == -1) {
                    edittext.setText("-60.0");
                } else {
                    if (bigDecimal.compareTo(new BigDecimal("60.0")) == 1) {
                        edittext.setText("60.0");
                    } else {
                        boolean flag_edit = true;
                        byte[] editByte = edittext.getText().toString().getBytes();
                        for (int k = 0; k < editByte.length; k++) {
                            if (editByte[k] == '.') {
                                flag_edit = false;
                                break;
                            }
                        }
                        if (flag_edit) {
                            edittext.getText().append(".0");
                        }
                    }
                }
                flag_limit = true;
            } else {
                flag_limit = false;
                Toast.makeText(Corrected.this, "通道输入不正确", Toast.LENGTH_SHORT).show();
            }
            myDiary.diary(dbHelper, Diary.correct_tongDao(edittext.getText().toString()));
        }
    }

    public String[] getSharedPreference(String key) {
        String regularEx = "#";
        String[] str;
        changable = getSharedPreferences("arrayxishu", MODE_PRIVATE);
        String values;
        values = changable.getString(key, "");
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
