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
import com.tdtf.weili.Utils.Netprinter;
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
import java.util.ArrayList;

public class Standard extends AppCompatActivity {
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    SharedPreferences perferences, coordinate;
    FileOutputStream mOutputStream, pOutputStream;

    SerialPort psp;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();

    int press_value;
    String serial = "", streamdata = "";
    String[] strings, str_lj, str_bc;
//    boolean limit_bc, limit_lj;
    boolean flag_start = false;
    boolean flag=true,flag_limit=true;
    Button btnsave;
    Button btnprint;
    Button btnback;
    TextView textPress;
    TextView[] text_wf, text_jf;
    EditText[] edit_bc, edit_lj;

    Netprinter netprinter = new Netprinter();
    ArrayList<String> lijing = new ArrayList<>();
    ArrayList<String> biaochi = new ArrayList<>();
    ArrayList<String> jifen = new ArrayList<>();
    ArrayList<String> weifen = new ArrayList<>();

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
                    if (streamdata.startsWith("aa0000cc33c33c")) {
                        streamdata = "";
                    }
                    if (streamdata.startsWith("aa0010") && streamdata.endsWith("cc33c33c")) {
                        for (int i = 0; i < 16; i++) {
                            int number = Integer.parseInt(streamdata.substring(i * 6 + 6, i * 6 + 12), 16);
                            String str = String.valueOf((float) number);
                            text_jf[i].setText(str);
                        }
                        for (int k = 0; k < 16; k++) {
                            if (k == 15) {
                                text_wf[k].setText("0.0");
                            } else {
                                int number = Integer.parseInt(streamdata.substring(k * 6 + 6, k * 6 + 12), 16)
                                        - Integer.parseInt(streamdata.substring((k + 1) * 6 + 6, (k + 1) * 6 + 12), 16);
                                String str = String.valueOf((float) number);
                                text_wf[k].setText(str);
                            }
                        }
                        streamdata = "";
                    }
                }

                if (msg.equals(SerialOrder.ORDER_OKPASSWAY)) {
                    for (int l = 0; l < 16; l++) {
                        serial = serial + Transform.double2hex(edit_bc[l].getText().toString());
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

                if (msg.equals(SerialOrder.ORDER_OKSAMPLING)) {
                    // TODO: 2016/12/22 发送预走量的指令
                    try {
                        //十六进制字符串
                        String down = SerialOrder.preQuantity(Transform.float2hex(strings[1]));//设置预走量
                        for (int i = 0; i < down.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(down.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.equals(SerialOrder.ORDER_OKTHRESHOLD)) {
                    try {
                        // TODO: 2017/7/10 加压
                        String cabin = SerialOrder.press(Transform.dec2hexTwo(strings[2]));
                        for (int i = 0; i < cabin.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(cabin.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.equals(SerialOrder.ORDER_RELEASE)) {
                    // TODO: 2017/7/10  排气成功
                    try {
                        //十六进制字符串
                        for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (msg.equals(SerialOrder.ORDER_OKPRESS)) {//开始检测
                    flag_start = true;
                    try {
                        for (int i = 0; i < SerialOrder.start("01").length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.start("01").substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                // TODO: 2017/8/11 检测完成排气
                if (msg.equals(SerialOrder.ORDER_OKSTART)) {
                    anticontrol();
                    flag_start=false;
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //TODO 检测异常中止后收排气(startwork)
                if (msg.equals(SerialOrder.ORDER_UNOKSTART)) {
                    anticontrol();
                    streamdata="";
                    flag_start=false;
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // TODO: 2017/8/11 压力检测
                if (msg.length() == 16) {
                    if (msg.substring(0, 6).equals("aa0023") &&
                            msg.substring(msg.length() - 8).equals("cc33c33c")) {
                        press_value = Integer.parseInt(msg.substring(6, 8), 16);
                        textPress = (TextView) findViewById(R.id.text_press);
                        textPress.setText("压力值:\n" + String.valueOf((float) press_value/10));
                        if (press_value < 5) {
                            //TODO: 2016/12/22  发送取样量指令
                            try {
                                String down = SerialOrder.sampling(Transform.double2hex(strings[0]));
                                for (int i = 0; i < down.length(); i = i + 2) {
                                    mOutputStream.write(Integer.parseInt(down.substring(i, i + 2), 16));
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
        setContentView(R.layout.activity_standard);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                myBinder = (MyService.MyBinder) service;
                myService = myBinder.getService();
                myService.setValues(new MyService.CallBacks() {
                    @Override
                    public void startRead(StringBuffer strBuffer) {
                        stringBuffer = strBuffer;
                        handler.post(dataReceived);
                    }

                    @Override
                    public void output(FileOutputStream outputStream) {
                        mOutputStream = outputStream;
                        //TODO: 2016/12/22  发送读取压力值指令
                        try {
                            //十六进制字符串
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
        Intent bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
        dbHelper = new MyDatabaseHelper(this);
        myDiary.diary(dbHelper, Diary.standard_start);
        final TextClock textClock = (TextClock) findViewById(R.id.textClock15);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_14);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_14);
        textUser.setText("用户名:\n" + Myutils.getUsername());

        perferences = getSharedPreferences("jianceshezhi", MODE_PRIVATE);
        strings = new String[3];
        strings[0] = perferences.getString("quyang", "");//取样量
        strings[1] = perferences.getString("yuzou", "");//预走量
        strings[2] = perferences.getString("qywz", "");//压力值
        // TODO: 2017/8/11 粒径
        edit_lj = new EditText[16];
        edit_lj[0] = (EditText) findViewById(R.id.edit_lj_0);
        edit_lj[1] = (EditText) findViewById(R.id.edit_lj_1);
        edit_lj[2] = (EditText) findViewById(R.id.edit_lj_2);
        edit_lj[3] = (EditText) findViewById(R.id.edit_lj_3);
        edit_lj[4] = (EditText) findViewById(R.id.edit_lj_4);
        edit_lj[5] = (EditText) findViewById(R.id.edit_lj_5);
        edit_lj[6] = (EditText) findViewById(R.id.edit_lj_6);
        edit_lj[7] = (EditText) findViewById(R.id.edit_lj_7);
        edit_lj[8] = (EditText) findViewById(R.id.edit_lj_8);
        edit_lj[9] = (EditText) findViewById(R.id.edit_lj_9);
        edit_lj[10] = (EditText) findViewById(R.id.edit_lj_10);
        edit_lj[11] = (EditText) findViewById(R.id.edit_lj_11);
        edit_lj[12] = (EditText) findViewById(R.id.edit_lj_12);
        edit_lj[13] = (EditText) findViewById(R.id.edit_lj_13);
        edit_lj[14] = (EditText) findViewById(R.id.edit_lj_14);
        edit_lj[15] = (EditText) findViewById(R.id.edit_lj_15);
        // TODO: 2017/8/11 积分
        text_jf = new TextView[16];
        text_jf[0] = (TextView) findViewById(R.id.text_jf_0);
        text_jf[1] = (TextView) findViewById(R.id.text_jf_1);
        text_jf[2] = (TextView) findViewById(R.id.text_jf_2);
        text_jf[3] = (TextView) findViewById(R.id.text_jf_3);
        text_jf[4] = (TextView) findViewById(R.id.text_jf_4);
        text_jf[5] = (TextView) findViewById(R.id.text_jf_5);
        text_jf[6] = (TextView) findViewById(R.id.text_jf_6);
        text_jf[7] = (TextView) findViewById(R.id.text_jf_7);
        text_jf[8] = (TextView) findViewById(R.id.text_jf_8);
        text_jf[9] = (TextView) findViewById(R.id.text_jf_9);
        text_jf[10] = (TextView) findViewById(R.id.text_jf_10);
        text_jf[11] = (TextView) findViewById(R.id.text_jf_11);
        text_jf[12] = (TextView) findViewById(R.id.text_jf_12);
        text_jf[13] = (TextView) findViewById(R.id.text_jf_13);
        text_jf[14] = (TextView) findViewById(R.id.text_jf_14);
        text_jf[15] = (TextView) findViewById(R.id.text_jf_15);
        // TODO: 2016/12/14 微分
        text_wf = new TextView[16];
        text_wf[0] = (TextView) findViewById(R.id.text_wf_0);
        text_wf[1] = (TextView) findViewById(R.id.text_wf_1);
        text_wf[2] = (TextView) findViewById(R.id.text_wf_2);
        text_wf[3] = (TextView) findViewById(R.id.text_wf_3);
        text_wf[4] = (TextView) findViewById(R.id.text_wf_4);
        text_wf[5] = (TextView) findViewById(R.id.text_wf_5);
        text_wf[6] = (TextView) findViewById(R.id.text_wf_6);
        text_wf[7] = (TextView) findViewById(R.id.text_wf_7);
        text_wf[8] = (TextView) findViewById(R.id.text_wf_8);
        text_wf[9] = (TextView) findViewById(R.id.text_wf_9);
        text_wf[10] = (TextView) findViewById(R.id.text_wf_10);
        text_wf[11] = (TextView) findViewById(R.id.text_wf_11);
        text_wf[12] = (TextView) findViewById(R.id.text_wf_12);
        text_wf[13] = (TextView) findViewById(R.id.text_wf_13);
        text_wf[14] = (TextView) findViewById(R.id.text_wf_14);
        text_wf[15] = (TextView) findViewById(R.id.text_wf_15);
        // TODO: 2017/8/11 标尺
        edit_bc = new EditText[16];
        edit_bc[0] = (EditText) findViewById(R.id.edit_bc_0);
        edit_bc[1] = (EditText) findViewById(R.id.edit_bc_1);
        edit_bc[2] = (EditText) findViewById(R.id.edit_bc_2);
        edit_bc[3] = (EditText) findViewById(R.id.edit_bc_3);
        edit_bc[4] = (EditText) findViewById(R.id.edit_bc_4);
        edit_bc[5] = (EditText) findViewById(R.id.edit_bc_5);
        edit_bc[6] = (EditText) findViewById(R.id.edit_bc_6);
        edit_bc[7] = (EditText) findViewById(R.id.edit_bc_7);
        edit_bc[8] = (EditText) findViewById(R.id.edit_bc_8);
        edit_bc[9] = (EditText) findViewById(R.id.edit_bc_9);
        edit_bc[10] = (EditText) findViewById(R.id.edit_bc_10);
        edit_bc[11] = (EditText) findViewById(R.id.edit_bc_11);
        edit_bc[12] = (EditText) findViewById(R.id.edit_bc_12);
        edit_bc[13] = (EditText) findViewById(R.id.edit_bc_13);
        edit_bc[14] = (EditText) findViewById(R.id.edit_bc_14);
        edit_bc[15] = (EditText) findViewById(R.id.edit_bc_15);

        TextWatcher textWatcher_lj = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 0 && s.toString().equals(".")) {
                    int m = 0;
                    while (m < 16) {
                        if (edit_lj[m].getText().toString().equals(".")) {
                            edit_lj[m].setText("");
                        }
                        m++;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                myDiary.diary(dbHelper, Diary.standard_liJing_input(s.toString()));
            }
        };

        str_lj = getSharedPreference("std_lj");
        str_bc = getSharedPreference("std_bc");

        for (int m = 0; m < 16; m++) {
            edit_lj[m].addTextChangedListener(textWatcher_lj);
            edit_lj[m].setText(str_lj[m]);
            edit_bc[m].setText(str_bc[m]);
            edit_lj[m].setSelection(edit_lj[m].getText().length());
            edit_bc[m].setSelection(edit_bc[m].getText().length());
            edit_bc[m].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editSelf=(EditText)v;
                    btnsave.setFocusableInTouchMode(false);
                    if (!hasFocus) judgement(editSelf);
                }
            });
        }

        btnsave = (Button) findViewById(R.id.btn_save);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.standard_save);
                btnsave.setFocusableInTouchMode(true);
                btnsave.requestFocusFromTouch();

                for (int k = 0; k < 16; k++) {
                    if (!TextUtils.isEmpty(edit_lj[k].getText())) {
                        if (TextUtils.isEmpty(edit_bc[k].getText())) {
                            String string = "已输入粒径的对应标尺不能为空";
                            flag = false;
                            Toast.makeText(Standard.this, string, Toast.LENGTH_SHORT).show();
                            break;
                        } else {
                            flag = true;
                            //判断粒径输入格式
                            boolean flag_edit_liJing = true;
                            byte[] editByte = edit_lj[k].getText().toString().getBytes();
                            for (int m = 0; m < editByte.length; m++) {
                                if (editByte[m] == '.') {
                                    flag_edit_liJing = false;
                                    break;
                                }
                            }
                            if (flag_edit_liJing) {
                                edit_lj[k].getText().append(".00");
                            } else {
                                if (edit_lj[k].getText().charAt(edit_lj[k].getText().length()-2)=='.') {
                                    edit_lj[k].getText().append("0");
                                }
                            }
                        }
                    }
                }

                if (flag&&flag_limit) {
                    control();
                    String[] strings_lj = new String[16];
                    String[] strings_bc = new String[16];
                    for (int s = 0; s < 16; s++) {
                        strings_lj[s] = edit_lj[s].getText().toString();
                        strings_bc[s] = edit_bc[s].getText().toString();
                    }
                    setSharedPreference("std_lj", strings_lj);
                    setSharedPreference("std_bc", strings_bc);
                    if (press_value < 5) {
                        try {
                            String mpass = SerialOrder.passWay("10");//设置检测通道数
                            for (int i = 0; i < mpass.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(mpass.substring(i, i + 2), 16));
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
        });
        btnprint = (Button) findViewById(R.id.btn_print);
        btnprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.standard_print);
                /*TODO 打开串口2*/
                try {
                    psp = new SerialPort(new File("/dev/ttyAMA2"), 9600, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                pOutputStream = (FileOutputStream) psp.getOutputStream();
                for (int p = 0; p < 16; p++) {
                    lijing.add(edit_lj[p].getText().toString());
                    biaochi.add(edit_bc[p].getText().toString());
                    jifen.add(text_jf[p].getText().toString());
                    weifen.add(text_wf[p].getText().toString());
                }
                try {
                    for (int l = 15; l > -1; l--) {
                        netprinter.printdatalijing(pOutputStream, lijing.get(l), 6);
                        netprinter.printdatabiaochi(pOutputStream, biaochi.get(l), 7);
                        netprinter.printdatajifen(pOutputStream, jifen.get(l), 9);
                        netprinter.printdataweifen(pOutputStream, weifen.get(l), 10);
                        netprinter.printline(pOutputStream);
                    }
                    netprinter.printtitle(pOutputStream, "粒径");
                    netprinter.printblank_bd(pOutputStream);
                    netprinter.printtitle(pOutputStream, "标尺");
                    netprinter.printblank_bd(pOutputStream);
                    netprinter.printtitle(pOutputStream, "积分");
                    netprinter.printblank_bd(pOutputStream);
                    netprinter.printtitle(pOutputStream, "微分");
                    netprinter.printline(pOutputStream);
                    netprinter.printline(pOutputStream);
                    netprinter.printline(pOutputStream);
                    mOutputStream.write(0x0d);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                psp.close();
            }

        });
        btnback = (Button) findViewById(R.id.btn_back);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.standard_back);
                startActivity(new Intent(Standard.this, Calibration.class));
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
                if (bigDecimal.compareTo(new BigDecimal("10.0")) == -1) {
                    edittext.setText("10.0");
                } else {
                    if (bigDecimal.compareTo(new BigDecimal("5000.0")) == 1) {
                        edittext.setText("5000.0");
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
                Toast.makeText(Standard.this, "标尺输入不正确", Toast.LENGTH_SHORT).show();
            }
            myDiary.diary(dbHelper, Diary.standard_biaoChi_input(edittext.getText().toString()));
        }
    }

    private void control() {
        btnsave.setClickable(false);
        btnprint.setClickable(false);
        btnback.setVisibility(View.INVISIBLE);
    }

    private void anticontrol() {
        btnsave.setClickable(true);
        btnprint.setClickable(true);
        btnback.setVisibility(View.VISIBLE);
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
