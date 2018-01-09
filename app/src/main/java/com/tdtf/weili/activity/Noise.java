package com.tdtf.weili.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
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
import com.tdtf.weili.information.Method;
import com.tdtf.weili.service.MyService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

public class Noise extends AppCompatActivity {
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    SharedPreferences coordinate;
    FileOutputStream mOutputStream;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    Timer timer;
    KeyListener key;
    int time = 0;
    boolean flag_start = false;
    boolean flag_limit = true;

    Button btnsave;
    Button btnback;
    Button btnstop;
    EditText[] edit_biaochi;
    TextView[] text_jishu;
    TextView textPress;

    String streamdata = "";
    String[] noisy = new String[16];
    BigDecimal[] str_modify = new BigDecimal[16];
    BigDecimal[] yray_correct;
    BigDecimal[] modify;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;

    /*TODO 读取串口的数据处理*/
    private class DataReceived implements Runnable {
        @Override
        public void run() {
            String msg = stringBuffer.toString();
            if (stringBuffer.length() != 0) {
                stringBuffer.delete(0, stringBuffer.length());
                // TODO: 2017/8/11 检测数据
                if (flag_start) {
                    streamdata = streamdata + msg;
                    if (streamdata.startsWith("aa0000cc33c33c") || streamdata.startsWith("aa0023")) {
                        streamdata = "";
                    }
                    if (streamdata.startsWith("aa0010") && streamdata.endsWith("cc33c33c")) {
                        if (streamdata.endsWith("aa0024cc33c33c")) {//噪声结束，计时停止
                            streamdata = streamdata.substring(0, streamdata.length() - 14);
                            timer.cancel();
                            flag_start = false;
                            btnsave.setVisibility(View.VISIBLE);
                            btnback.setVisibility(View.VISIBLE);
                            btnstop.setVisibility(View.GONE);
                            for (int i = 0; i < 16; i++) {
                                edit_biaochi[i].setKeyListener(key);
                            }
                        }
                        for (int i = 0; i < 16; i++) {
                            int number = Integer.parseInt(streamdata.substring(i * 6 + 6, i * 6 + 12), 16);
                            String str = String.valueOf((float) number);
                            text_jishu[i].setText(str);
                        }
                        streamdata = "";
                    }
                }
                if (msg.length() == 16) {//压力检测
                    if (msg.substring(0, 6).equals("aa0023") &&
                            msg.substring(msg.length() - 8).equals("cc33c33c")) {
                        int pressure = Integer.parseInt(msg.substring(6, 8), 16);
                        textPress = (TextView) findViewById(R.id.text_press_6);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }

                if (msg.equals(SerialOrder.ORDER_OKPASSWAY)) {
                    String serial = "";
                    for (int l = 0; l < 16; l++) {
                        serial = serial + Transform.double2hex(yray_correct[l].toString());
                    }
                    try {
                        String order = "aa0008" + serial + "cc33c33c";
                        //"032003e809c40fa017701f4027104e20c350c350c350c350c350c350c350c350"
                        for (int i = 0; i < order.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(order.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.equals(SerialOrder.ORDER_OKTHRESHOLD)) {//开始检测
                    flag_start = true;
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_NOISE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_NOISE.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noise);
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
        myDiary.diary(dbHelper, Diary.noise_start);
        final TextClock textClock = (TextClock) findViewById(R.id.textClock16);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_8);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_8);
        textUser.setText("用户名:\n" + Myutils.getUsername());

        edit_biaochi = new EditText[16];
        edit_biaochi[0] = (EditText) findViewById(R.id.edit_biaochi_0);
        edit_biaochi[1] = (EditText) findViewById(R.id.edit_biaochi_1);
        edit_biaochi[2] = (EditText) findViewById(R.id.edit_biaochi_2);
        edit_biaochi[3] = (EditText) findViewById(R.id.edit_biaochi_3);
        edit_biaochi[4] = (EditText) findViewById(R.id.edit_biaochi_4);
        edit_biaochi[5] = (EditText) findViewById(R.id.edit_biaochi_5);
        edit_biaochi[6] = (EditText) findViewById(R.id.edit_biaochi_6);
        edit_biaochi[7] = (EditText) findViewById(R.id.edit_biaochi_7);
        edit_biaochi[8] = (EditText) findViewById(R.id.edit_biaochi_8);
        edit_biaochi[9] = (EditText) findViewById(R.id.edit_biaochi_9);
        edit_biaochi[10] = (EditText) findViewById(R.id.edit_biaochi_10);
        edit_biaochi[11] = (EditText) findViewById(R.id.edit_biaochi_11);
        edit_biaochi[12] = (EditText) findViewById(R.id.edit_biaochi_12);
        edit_biaochi[13] = (EditText) findViewById(R.id.edit_biaochi_13);
        edit_biaochi[14] = (EditText) findViewById(R.id.edit_biaochi_14);
        edit_biaochi[15] = (EditText) findViewById(R.id.edit_biaochi_15);

        text_jishu = new TextView[16];
        text_jishu[0] = (TextView) findViewById(R.id.text_jishu_0);
        text_jishu[1] = (TextView) findViewById(R.id.text_jishu_1);
        text_jishu[2] = (TextView) findViewById(R.id.text_jishu_2);
        text_jishu[3] = (TextView) findViewById(R.id.text_jishu_3);
        text_jishu[4] = (TextView) findViewById(R.id.text_jishu_4);
        text_jishu[5] = (TextView) findViewById(R.id.text_jishu_5);
        text_jishu[6] = (TextView) findViewById(R.id.text_jishu_6);
        text_jishu[7] = (TextView) findViewById(R.id.text_jishu_7);
        text_jishu[8] = (TextView) findViewById(R.id.text_jishu_8);
        text_jishu[9] = (TextView) findViewById(R.id.text_jishu_9);
        text_jishu[10] = (TextView) findViewById(R.id.text_jishu_10);
        text_jishu[11] = (TextView) findViewById(R.id.text_jishu_11);
        text_jishu[12] = (TextView) findViewById(R.id.text_jishu_12);
        text_jishu[13] = (TextView) findViewById(R.id.text_jishu_13);
        text_jishu[14] = (TextView) findViewById(R.id.text_jishu_14);
        text_jishu[15] = (TextView) findViewById(R.id.text_jishu_15);

        key = edit_biaochi[0].getKeyListener();

        noisy = getSharedPreference("noise");
        for (int l = 0; l < 16; l++) {
            edit_biaochi[l].setText(noisy[l]);
            edit_biaochi[l].setSelection(edit_biaochi[l].getText().length());
            edit_biaochi[l].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editSelf = (EditText) v;
                    btnsave.setFocusableInTouchMode(false);
                    if (!hasFocus) judgement(editSelf);
                }
            });
        }

        btnsave = (Button) findViewById(R.id.btn_save);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.noise_save);
                // TODO: 2017/8/15 发送通道数
                btnsave.setFocusableInTouchMode(true);
                btnsave.requestFocusFromTouch();
                boolean flag_null = true;
                for (int l = 0; l < 16; l++) {
                    if (TextUtils.isEmpty(edit_biaochi[l].getText())) {
                        flag_null = false;
                        Toast.makeText(Noise.this, "标尺输入不能为空",
                                Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        flag_null = true;
                    }
                }
                int n = 0;
                for (int i = 0; i < 16; i++) {
                    if (!TextUtils.isEmpty(edit_biaochi[i].getText())) {
                        n++;
                    }
                }
                if (n == 16 && flag_limit && flag_null) {
                    btnsave.setVisibility(View.INVISIBLE);
                    btnback.setVisibility(View.GONE);
                    btnstop.setVisibility(View.VISIBLE);
                    for (int i = 0; i < 16; i++) {
                        edit_biaochi[i].setKeyListener(null);
                    }
                    yray_correct = new BigDecimal[16];
                    modify = new BigDecimal[17];
                    for (int m = 0; m < 17; m++) {
                        modify[m] = new BigDecimal(getSharedPreference("change")[m]);
                    }
                    for (int w = 0; w < 16; w++) {
                        str_modify[w] = new BigDecimal(edit_biaochi[w].getText().toString());
                    }
                    yray_correct = Method.correct(str_modify, modify);
                    try {
                        for (int i = 0; i < SerialOrder.passWay("10").length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.passWay("10").substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //////////////////////////////////////////////////////////////////////////////
                    timer = new Timer();
                    time = 0;
                    final TextView textView = (TextView) findViewById(R.id.text_timer);
                    final Handler handler_time = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            String str = "计时:\n" + String.valueOf(msg.what) + "s";
                            textView.setText(str);
                        }
                    };
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = time;
                            handler_time.sendMessage(message);
                            time++;
                        }
                    };
                    timer.schedule(task, 0, 1000);

                    for (int l = 0; l < edit_biaochi.length; l++) {
                        noisy[l] = edit_biaochi[l].getText().toString();
                    }
                    setSharedPreference("noise", noisy);
                } else {
                    Toast.makeText(getApplicationContext(), "标尺不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnstop = (Button) findViewById(R.id.btn_stop);
        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.noise_stop);
                flag_start = false;
                streamdata = "";
                try {
                    for (int i = 0; i < SerialOrder.ORDER_STOP.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_STOP.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                btnback.setVisibility(View.VISIBLE);
                btnstop.setVisibility(View.GONE);
                btnsave.setVisibility(View.VISIBLE);
                for (int i = 0; i < 16; i++) {
                    edit_biaochi[i].setKeyListener(key);
                }
                timer.cancel();
            }
        });
        btnback = (Button) findViewById(R.id.btn_back);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.noise_back);
                startActivity(new Intent(Noise.this, Calibration.class));
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
                if (bigDecimal.compareTo(new BigDecimal("1.0")) == -1) {
                    edittext.setText("1.0");
                } else {
                    if (bigDecimal.compareTo(new BigDecimal("5100.0")) == 1) {
                        edittext.setText("5100.0");
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
                Toast.makeText(Noise.this, "标尺输入不正确", Toast.LENGTH_SHORT).show();
            }
            myDiary.diary(dbHelper, Diary.noise_biaoChi(edittext.getText().toString()));
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
