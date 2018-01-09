package com.tdtf.weili.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.tdtf.weili.fragment.passopt.CustomOpt;
import com.tdtf.weili.fragment.passopt.LvchuOpt;
import com.tdtf.weili.fragment.passopt.MazuiqijuOpt;
import com.tdtf.weili.fragment.passopt.Wuran_fiveOpt;
import com.tdtf.weili.fragment.passopt.Wuran_nintyeightOpt;
import com.tdtf.weili.fragment.passopt.YaodianOpt;
import com.tdtf.weili.service.MyService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class PassOpt extends AppCompatActivity {
    RadioGroup rg;
    SharedPreferences perferences;
    SharedPreferences coordinate;
    String serial;
    String passway;
    MyDiary myDiary = new MyDiary();
    MyDatabaseHelper dbHelper;

    Button btndefine;
    Button btnsave;
    Button btnback;
    RadioButton rbtnmazui, rbtnlvchu, rbtn05, rbtn98, rbtnzgyd, rbtncustom;

    CustomOpt customOpt;
    MazuiqijuOpt mazuiqijuOpt;
    LvchuOpt lvchuOpt;
    Wuran_fiveOpt wuran05;
    Wuran_nintyeightOpt wuran98;
    YaodianOpt yaodianOpt;
    FragmentManager fragmentManager;
    FragmentTransaction transaction;

    private int m = 0;
    int[] radio_id;
    int[] radioIdLvchu;
    Boolean[] checked_state;
    String[] checked_values;

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
                        textPress = (TextView) findViewById(R.id.text_press_9);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_opt);
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
        final TextClock textClock = (TextClock) findViewById(R.id.textClock9);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_11);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_11);
        textUser.setText("用户名:\n" + Myutils.getUsername());

        dbHelper = new MyDatabaseHelper(this);
        myDiary.diary(dbHelper, Diary.passOpt_start);

        perferences = getSharedPreferences("checkBoxState", MODE_PRIVATE);

        rg = (RadioGroup) findViewById(R.id.rg);
        rg.check(perferences.getInt("rg", 0));

        rbtncustom = (RadioButton) findViewById(R.id.rBtncustom);
        rbtnmazui = (RadioButton) findViewById(R.id.rBtnmazui);
        rbtnlvchu = (RadioButton) findViewById(R.id.rBtnlvchu);
        rbtn05 = (RadioButton) findViewById(R.id.rBtn05);
        rbtn98 = (RadioButton) findViewById(R.id.rBtn98);
        rbtnzgyd = (RadioButton) findViewById(R.id.rBtnzgyd);
        btndefine = (Button) findViewById(R.id.btn_define);

        fragmentManager = getFragmentManager();
        transaction = fragmentManager.beginTransaction();
        customOpt = new CustomOpt();
        mazuiqijuOpt = new MazuiqijuOpt();
        lvchuOpt = new LvchuOpt();
        wuran05 = new Wuran_fiveOpt();
        wuran98 = new Wuran_nintyeightOpt();
        yaodianOpt = new YaodianOpt();
        transaction.add(R.id.passFragment, mazuiqijuOpt);
        transaction.add(R.id.passFragment, lvchuOpt);
        transaction.add(R.id.passFragment, wuran05);
        transaction.add(R.id.passFragment, wuran98);
        transaction.add(R.id.passFragment, yaodianOpt);
        transaction.add(R.id.passFragment, customOpt);
        fragmentHide();
        transaction.commit();

        final String[] x = getSharedPreference("坐标X");
        final BigDecimal[] xray = new BigDecimal[x.length];
        for (int h = 0; h < x.length; h++) {
            xray[h] = new BigDecimal(x[h]);
        }
        final String[] arrayxishu = getSharedPreference("系数");
        final BigDecimal[] a = new BigDecimal[x.length - 1];
        final BigDecimal[] b = new BigDecimal[x.length - 1];
        final BigDecimal[] c = new BigDecimal[x.length - 1];
        final BigDecimal[] d = new BigDecimal[x.length - 1];
        for (int m = 0; m < arrayxishu.length / 4; m++) {
            a[m] = new BigDecimal(arrayxishu[m]);
            b[m] = new BigDecimal(arrayxishu[m + (x.length - 1)]);
            c[m] = new BigDecimal(arrayxishu[m + 2 * (x.length - 1)]);
            d[m] = new BigDecimal(arrayxishu[m + 3 * (x.length - 1)]);
        }

        if (rbtncustom.isChecked()) {
            fragmentManager = getFragmentManager();
            transaction = fragmentManager.beginTransaction();
            if (customOpt == null) {
                customOpt = new CustomOpt();
                transaction.add(R.id.passFragment, customOpt);
            }
            fragmentHide();
            transaction.show(customOpt);
            transaction.commit();
            btndefine.setVisibility(View.VISIBLE);
            rbtncustom.setTextSize(30);
            rbtncustom.setTextColor(Color.parseColor("#FF4FA0C9"));
        } else if (rbtnmazui.isChecked()) {
            fragmentManager = getFragmentManager();
            transaction = fragmentManager.beginTransaction();
            if (mazuiqijuOpt == null) {
                mazuiqijuOpt = new MazuiqijuOpt();
                transaction.add(R.id.passFragment, mazuiqijuOpt);
            }
            fragmentHide();
            transaction.show(mazuiqijuOpt);
            transaction.commit();
            rbtnmazui.setTextSize(30);
            rbtnmazui.setTextColor(Color.parseColor("#FF4FA0C9"));

            BigDecimal[] pass = {new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6")};
            serial = order(xray, pass, a, b, c, d);
            passway = "03";
        } else if (rbtnlvchu.isChecked()) {
            fragmentManager = getFragmentManager();
            transaction = fragmentManager.beginTransaction();
            if (lvchuOpt == null) {
                lvchuOpt = new LvchuOpt();
                transaction.add(R.id.passFragment, lvchuOpt);
            }
            fragmentHide();
            transaction.show(lvchuOpt);
            transaction.commit();
            rbtnlvchu.setTextSize(30);
            rbtnlvchu.setTextColor(Color.parseColor("#FF4FA0C9"));

            BigDecimal[] pass = {new BigDecimal("15"), new BigDecimal("25")};
            serial = order(xray, pass, a, b, c, d);
            passway = "02";
        } else if (rbtn98.isChecked()) {
            fragmentManager = getFragmentManager();
            transaction = fragmentManager.beginTransaction();
            if (wuran98 == null) {
                wuran98 = new Wuran_nintyeightOpt();
                transaction.add(R.id.passFragment, wuran98);
            }
            fragmentHide();
            transaction.show(wuran98);
            transaction.commit();
            rbtn98.setTextSize(30);
            rbtn98.setTextColor(Color.parseColor("#FF4FA0C9"));

            BigDecimal[] pass = {new BigDecimal("15"), new BigDecimal("25")};
            serial = order(xray, pass, a, b, c, d);
            passway = "02";
        } else if (rbtn05.isChecked()) {
            fragmentManager = getFragmentManager();
            transaction = fragmentManager.beginTransaction();
            if (wuran05 == null) {
                wuran05 = new Wuran_fiveOpt();
                transaction.add(R.id.passFragment, wuran05);
            }
            fragmentHide();
            transaction.show(wuran05);
            transaction.commit();
            rbtn05.setTextSize(30);
            rbtn05.setTextColor(Color.parseColor("#FF4FA0C9"));

            BigDecimal[] pass = {new BigDecimal("25"), new BigDecimal("50"), new BigDecimal("100")};
            serial = order(xray, pass, a, b, c, d);
            passway = "03";
        } else if (rbtnzgyd.isChecked()) {
            fragmentManager = getFragmentManager();
            transaction = fragmentManager.beginTransaction();
            if (yaodianOpt == null) {
                yaodianOpt = new YaodianOpt();
                transaction.add(R.id.passFragment, yaodianOpt);
            }
            fragmentHide();
            transaction.show(yaodianOpt);
            transaction.commit();
            rbtnzgyd.setTextSize(30);
            rbtnzgyd.setTextColor(Color.parseColor("#FF4FA0C9"));

            BigDecimal[] pass = {new BigDecimal("10"), new BigDecimal("25")};
            serial = order(xray, pass, a, b, c, d);
            passway = "02";
        }



        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rBtnmazui:
                        if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[9])) {
                            myDiary.diary(dbHelper, Diary.passOpt_maZuiQiJU);
                            btndefine.setVisibility(View.INVISIBLE);
                            BigDecimal[] pass = {new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6")};
                            serial = order(xray, pass, a, b, c, d);
                            passway = "03";
                        }
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        if (mazuiqijuOpt == null) {
                            mazuiqijuOpt = new MazuiqijuOpt();
                            transaction.add(R.id.passFragment, mazuiqijuOpt);
                        }
                        fragmentHide();
                        transaction.show(mazuiqijuOpt);
                        transaction.commit();
                        rbtnmazui.setTextSize(30);
                        rbtnlvchu.setTextSize(24);
                        rbtn05.setTextSize(24);
                        rbtn98.setTextSize(24);
                        rbtnzgyd.setTextSize(24);
                        rbtncustom.setTextSize(24);
                        rbtnmazui.setTextColor(Color.parseColor("#FF4FA0C9"));
                        rbtnlvchu.setTextColor(Color.parseColor("#FF000000"));
                        rbtn05.setTextColor(Color.parseColor("#FF000000"));
                        rbtn98.setTextColor(Color.parseColor("#FF000000"));
                        rbtnzgyd.setTextColor(Color.parseColor("#FF000000"));
                        rbtncustom.setTextColor(Color.parseColor("#FF000000"));
                        break;
                    case R.id.rBtnlvchu:
                        if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[10])) {
                            myDiary.diary(dbHelper, Diary.passOpt_lvChu);
                            btndefine.setVisibility(View.INVISIBLE);

                            BigDecimal[] pass = {new BigDecimal("15"), new BigDecimal("25")};
                            serial = order(xray, pass, a, b, c, d);
                            passway = "02";
                        }
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        if (lvchuOpt == null) {
                            lvchuOpt = new LvchuOpt();
                            transaction.add(R.id.passFragment, lvchuOpt);
                        }
                        fragmentHide();
                        transaction.show(lvchuOpt);
                        transaction.commit();
                        rbtnmazui.setTextSize(24);
                        rbtnlvchu.setTextSize(30);
                        rbtn05.setTextSize(24);
                        rbtn98.setTextSize(24);
                        rbtnzgyd.setTextSize(24);
                        rbtncustom.setTextSize(24);
                        rbtnmazui.setTextColor(Color.parseColor("#FF000000"));
                        rbtnlvchu.setTextColor(Color.parseColor("#FF4FA0C9"));
                        rbtn05.setTextColor(Color.parseColor("#FF000000"));
                        rbtn98.setTextColor(Color.parseColor("#FF000000"));
                        rbtnzgyd.setTextColor(Color.parseColor("#FF000000"));
                        rbtncustom.setTextColor(Color.parseColor("#FF000000"));
                        break;
                    case R.id.rBtn05:
                        if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[11])) {
                            myDiary.diary(dbHelper, Diary.passOpt_wuRan05);
                            btndefine.setVisibility(View.INVISIBLE);

                            BigDecimal[] pass = {new BigDecimal("25"), new BigDecimal("50"), new BigDecimal("100")};
                            serial = order(xray, pass, a, b, c, d);
                            passway = "03";
                        }
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        if (wuran05 == null) {
                            wuran05 = new Wuran_fiveOpt();
                            transaction.add(R.id.passFragment, wuran05);
                        }
                        fragmentHide();
                        transaction.show(wuran05);
                        transaction.commit();
                        rbtnmazui.setTextSize(24);
                        rbtnlvchu.setTextSize(24);
                        rbtn05.setTextSize(30);
                        rbtn98.setTextSize(24);
                        rbtnzgyd.setTextSize(24);
                        rbtncustom.setTextSize(24);
                        rbtnmazui.setTextColor(Color.parseColor("#FF000000"));
                        rbtnlvchu.setTextColor(Color.parseColor("#FF000000"));
                        rbtn05.setTextColor(Color.parseColor("#FF4FA0C9"));
                        rbtn98.setTextColor(Color.parseColor("#FF000000"));
                        rbtnzgyd.setTextColor(Color.parseColor("#FF000000"));
                        rbtncustom.setTextColor(Color.parseColor("#FF000000"));
                        break;
                    case R.id.rBtn98:
                        if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[12])) {
                            myDiary.diary(dbHelper, Diary.passOpt_wuRan98);
                            btndefine.setVisibility(View.INVISIBLE);

                            BigDecimal[] pass = {new BigDecimal("15"), new BigDecimal("25")};
                            serial = order(xray, pass, a, b, c, d);
                            passway = "02";
                        }
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        if (wuran98 == null) {
                            wuran98 = new Wuran_nintyeightOpt();
                            transaction.add(R.id.passFragment, wuran98);
                        }
                        fragmentHide();
                        transaction.show(wuran98);
                        transaction.commit();
                        rbtnmazui.setTextSize(24);
                        rbtnlvchu.setTextSize(24);
                        rbtn05.setTextSize(24);
                        rbtn98.setTextSize(30);
                        rbtnzgyd.setTextSize(24);
                        rbtncustom.setTextSize(24);
                        rbtnmazui.setTextColor(Color.parseColor("#FF000000"));
                        rbtnlvchu.setTextColor(Color.parseColor("#FF000000"));
                        rbtn05.setTextColor(Color.parseColor("#FF000000"));
                        rbtn98.setTextColor(Color.parseColor("#FF4FA0C9"));
                        rbtnzgyd.setTextColor(Color.parseColor("#FF000000"));
                        rbtncustom.setTextColor(Color.parseColor("#FF000000"));
                        break;
                    case R.id.rBtnzgyd:
                        if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[13])) {
                            myDiary.diary(dbHelper, Diary.passOpt_yaoDian);
                            btndefine.setVisibility(View.INVISIBLE);

                            BigDecimal[] pass = {new BigDecimal("10"), new BigDecimal("25")};
                            serial = order(xray, pass, a, b, c, d);
                            passway = "02";
                        }
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        if (yaodianOpt == null) {
                            yaodianOpt = new YaodianOpt();
                            transaction.add(R.id.passFragment, yaodianOpt);
                        }
                        fragmentHide();
                        transaction.show(yaodianOpt);
                        transaction.commit();
                        rbtnmazui.setTextSize(24);
                        rbtnlvchu.setTextSize(24);
                        rbtn05.setTextSize(24);
                        rbtn98.setTextSize(24);
                        rbtnzgyd.setTextSize(30);
                        rbtncustom.setTextSize(24);
                        rbtnmazui.setTextColor(Color.parseColor("#FF000000"));
                        rbtnlvchu.setTextColor(Color.parseColor("#FF000000"));
                        rbtn05.setTextColor(Color.parseColor("#FF000000"));
                        rbtn98.setTextColor(Color.parseColor("#FF000000"));
                        rbtnzgyd.setTextColor(Color.parseColor("#FF4FA0C9"));
                        rbtncustom.setTextColor(Color.parseColor("#FF000000"));
                        break;
                    case R.id.rBtncustom:
                        if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[14])) {
                            myDiary.diary(dbHelper, Diary.passOpt_custom);
                            btndefine.setVisibility(View.VISIBLE);
                        }
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        if (customOpt == null) {
                            customOpt = new CustomOpt();
                            transaction.add(R.id.passFragment, customOpt);
                        }
                        fragmentHide();
                        transaction.show(customOpt);
                        transaction.commit();
                        rbtnmazui.setTextSize(24);
                        rbtnlvchu.setTextSize(24);
                        rbtn05.setTextSize(24);
                        rbtn98.setTextSize(24);
                        rbtnzgyd.setTextSize(24);
                        rbtncustom.setTextSize(30);
                        rbtnmazui.setTextColor(Color.parseColor("#FF000000"));
                        rbtnlvchu.setTextColor(Color.parseColor("#FF000000"));
                        rbtn05.setTextColor(Color.parseColor("#FF000000"));
                        rbtn98.setTextColor(Color.parseColor("#FF000000"));
                        rbtnzgyd.setTextColor(Color.parseColor("#FF000000"));
                        rbtncustom.setTextColor(Color.parseColor("#FF4FA0C9"));
                        break;
                    default:
                        break;
                }
            }
        });

        btndefine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.passOpt_btn_custom);
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[15])) {
                    final Boolean[] checked_state = new Boolean[16];
                    final String[] checked_values = new String[16];
                    customOpt.state(new CustomOpt.CallBacks() {
                        @Override
                        public void process(Boolean[] booleen) {
                            System.arraycopy(booleen, 0, checked_state, 0, 16);
                        }

                        @Override
                        public void values(String[] strings) {
                            System.arraycopy(strings, 0, checked_values, 0, 16);
                        }
                    });
                    SharedPreferences.Editor editor;
                    editor = perferences.edit();
                    editor.putInt("rg", rg.getCheckedRadioButtonId());
                    editor.putInt("rbtn0", rbtnmazui.getId());
                    editor.putInt("rbtn1", rbtnlvchu.getId());
                    editor.putInt("rbtn2", rbtn05.getId());
                    editor.putInt("rbtn3", rbtn98.getId());
                    editor.putInt("rbtn4", rbtnzgyd.getId());
                    editor.putInt("rbtn5", rbtncustom.getId());
                    editor.putBoolean("cb1", checked_state[0]);
                    editor.putBoolean("cb2", checked_state[1]);
                    editor.putBoolean("cb3", checked_state[2]);
                    editor.putBoolean("cb4", checked_state[3]);
                    editor.putBoolean("cb5", checked_state[4]);
                    editor.putBoolean("cb6", checked_state[5]);
                    editor.putBoolean("cb7", checked_state[6]);
                    editor.putBoolean("cb8", checked_state[7]);
                    editor.putBoolean("cb9", checked_state[8]);
                    editor.putBoolean("cb10", checked_state[9]);
                    editor.putBoolean("cb11", checked_state[10]);
                    editor.putBoolean("cb12", checked_state[11]);
                    editor.putBoolean("cb13", checked_state[12]);
                    editor.putBoolean("cb14", checked_state[13]);
                    editor.putBoolean("cb15", checked_state[14]);
                    editor.putBoolean("cb16", checked_state[15]);
                    editor.putString("cbt1", checked_values[0]);
                    editor.putString("cbt2", checked_values[1]);
                    editor.putString("cbt3", checked_values[2]);
                    editor.putString("cbt4", checked_values[3]);
                    editor.putString("cbt5", checked_values[4]);
                    editor.putString("cbt6", checked_values[5]);
                    editor.putString("cbt7", checked_values[6]);
                    editor.putString("cbt8", checked_values[7]);
                    editor.putString("cbt9", checked_values[8]);
                    editor.putString("cbt10", checked_values[9]);
                    editor.putString("cbt11", checked_values[10]);
                    editor.putString("cbt12", checked_values[11]);
                    editor.putString("cbt13", checked_values[12]);
                    editor.putString("cbt14", checked_values[13]);
                    editor.putString("cbt15", checked_values[14]);
                    editor.putString("cbt16", checked_values[15]);
                    editor.putString("serial", serial);
                    editor.putString("passway", passway);
                    editor.apply();
                    startActivityForResult(new Intent(PassOpt.this, PassDefine.class), 0);
                    unbindService(serviceConnection);
                    finish();
                } else {
                    Toast.makeText(PassOpt.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnsave = (Button) findViewById(R.id.btn_pass_save);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!show.getText().toString().equals("权限不匹配，该选项无效")) {
                checked_state = new Boolean[16];
                checked_values = new String[16];
                customOpt.state(new CustomOpt.CallBacks() {
                    @Override
                    public void process(Boolean[] booleen) {
                        System.arraycopy(booleen, 0, checked_state, 0, 16);
                    }

                    @Override
                    public void values(String[] strings) {
                        System.arraycopy(strings, 0, checked_values, 0, 16);
                    }
                });
                radio_id = new int[15];
                wuran05.state(new Wuran_fiveOpt.CallBacks() {
                    @Override
                    public void process(int[] ints) {
                        System.arraycopy(ints, 0, radio_id, 0, 15);

                    }
                });
                radioIdLvchu = new int[15];
                lvchuOpt.state(new LvchuOpt.CallBacks() {
                    @Override
                    public void process(int[] ints) {
                        System.arraycopy(ints, 0, radioIdLvchu, 0, 15);
                    }
                });
                if (rg.getCheckedRadioButtonId() == rbtncustom.getId()) {
                    int mNum = 0;
                    for (int i = 0; i < 16; i++) {
                        if (checked_state[i]) {
                            mNum++;
                        }
                    }
                    BigDecimal[] pass = new BigDecimal[mNum];
                    while (m < mNum) {
                        for (int k = 0; k < 16; k++) {
                            if (checked_state[k]) {
                                pass[m] = new BigDecimal(checked_values[k]);
                                m++;
                            }
                        }
                    }
                    serial = order(xray, pass, a, b, c, d);
                    passway = String.valueOf(mNum);

                }

                m = 0;
                ////////////////////////////////////////////////////
                if (rg.getCheckedRadioButtonId() == rbtnlvchu.getId()) {
                    RadioButton rBt_sample = (RadioButton) findViewById(R.id.radioButton_sample);
                    RadioButton rBtn_compare_0 = (RadioButton) findViewById(R.id.rBtn_compare_0);
                    RadioButton rBtn_compare_1 = (RadioButton) findViewById(R.id.rBtn_compare_1);
                    RadioButton rBtn_compare_2 = (RadioButton) findViewById(R.id.rBtn_compare_2);
                    RadioButton rBtn_compare_3 = (RadioButton) findViewById(R.id.rBtn_compare_3);
                    RadioButton rBtn_compare_4 = (RadioButton) findViewById(R.id.rBtn_compare_4);
                    TextView textCompare_0 = (TextView) findViewById(R.id.text_compare_0);
                    TextView textCompare_1 = (TextView) findViewById(R.id.text_compare_1);
                    TextView textCompare_2 = (TextView) findViewById(R.id.text_compare_2);
                    TextView textCompare_3 = (TextView) findViewById(R.id.text_compare_3);
                    TextView textCompare_4 = (TextView) findViewById(R.id.text_compare_4);
                    if (rBt_sample.isChecked()) {
                        if (rBtn_compare_0.isChecked()) {
                            if (textCompare_0.getText().equals("0.0")) {
                                Toast.makeText(PassOpt.this, "保存失败。所选数据不能为0", Toast.LENGTH_SHORT).show();
                            } else {
                                saving();
                            }
                        }
                        if (rBtn_compare_1.isChecked()) {
                            if (textCompare_1.getText().equals("0.0")) {
                                Toast.makeText(PassOpt.this, "保存失败。所选数据不能为0", Toast.LENGTH_SHORT).show();
                            } else {
                                saving();
                            }
                        }
                        if (rBtn_compare_2.isChecked()) {
                            if (textCompare_2.getText().equals("0.0")) {
                                Toast.makeText(PassOpt.this, "保存失败。所选数据不能为0", Toast.LENGTH_SHORT).show();
                            } else {
                                saving();
                            }
                        }
                        if (rBtn_compare_3.isChecked()) {
                            if (textCompare_3.getText().equals("0.0")) {
                                Toast.makeText(PassOpt.this, "保存失败。所选数据不能为0", Toast.LENGTH_SHORT).show();
                            } else {
                                saving();
                            }
                        }
                        if (rBtn_compare_4.isChecked()) {
                            if (textCompare_4.getText().equals("0.0")) {
                                Toast.makeText(PassOpt.this, "保存失败。所选数据不能为0", Toast.LENGTH_SHORT).show();
                            } else {
                                saving();
                            }
                        }
                    } else {
                        saving();
                    }
                } else {
                    saving();
                }

//                } else {
//                    Toast.makeText(PassOpt.this, "请根据您的权限，选择有效功能", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        btnback = (Button) findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.passOpt_back);
                startActivity(new Intent(PassOpt.this, MainMenu.class));
                unbindService(serviceConnection);
                finish();
            }
        });
    }

    // TODO: 2017/4/11 参数依顺序分别是X坐标，通道，系数a，系数b，系数c，系数d，返回值：串口指令
    private static String order(BigDecimal[] xray, BigDecimal[] pass, BigDecimal[] a, BigDecimal[] b, BigDecimal[] c, BigDecimal[] d) {
        BigDecimal saber;
        String lancer;
        String basaker = "";
        int archer;
        for (int j = 0; j < pass.length; j++) {
            for (int i = 0; i < xray.length - 1; i++) {
                if ((pass[j].compareTo(xray[i]) == 1 || pass[j].compareTo(xray[i]) == 0) &&
                        (pass[j].compareTo(xray[i + 1]) == -1 || pass[j].compareTo(xray[i + 1]) == 0)) {
                    saber = a[i].multiply(pass[j]).multiply(pass[j]).multiply(pass[j]).add
                            (b[i].multiply(pass[j]).multiply(pass[j])).add(c[i].multiply(pass[j])).add(d[i]);
                    archer = saber.multiply(new BigDecimal("10")).intValue();
                    lancer = dec2hex(archer);
                    basaker = basaker + lancer;
                    break;
                }
            }
        }
        return basaker;
    }

    // TODO: 2016/12/22  十进制转十六进制字符串（整形）
    public static String dec2hex(int s) {
        String ssr;
        String fusion = "";
        ssr = Integer.toHexString(s);
        switch (ssr.length()) {
            case 1:
                fusion = "000" + ssr;
                break;
            case 2:
                fusion = "00" + ssr;
                break;
            case 3:
                fusion = "0" + ssr;
                break;
            default:
                break;
        }
        return fusion;
    }

    protected void saving() {
        SharedPreferences.Editor editor;
        editor = perferences.edit();
        editor.putInt("rg", rg.getCheckedRadioButtonId());
        editor.putInt("rbtn0", rbtnmazui.getId());
        editor.putInt("rbtn1", rbtnlvchu.getId());
        editor.putInt("rbtn2", rbtn05.getId());
        editor.putInt("rbtn3", rbtn98.getId());
        editor.putInt("rbtn4", rbtnzgyd.getId());
        editor.putInt("rbtn5", rbtncustom.getId());
        editor.putBoolean("cb1", checked_state[0]);
        editor.putBoolean("cb2", checked_state[1]);
        editor.putBoolean("cb3", checked_state[2]);
        editor.putBoolean("cb4", checked_state[3]);
        editor.putBoolean("cb5", checked_state[4]);
        editor.putBoolean("cb6", checked_state[5]);
        editor.putBoolean("cb7", checked_state[6]);
        editor.putBoolean("cb8", checked_state[7]);
        editor.putBoolean("cb9", checked_state[8]);
        editor.putBoolean("cb10", checked_state[9]);
        editor.putBoolean("cb11", checked_state[10]);
        editor.putBoolean("cb12", checked_state[11]);
        editor.putBoolean("cb13", checked_state[12]);
        editor.putBoolean("cb14", checked_state[13]);
        editor.putBoolean("cb15", checked_state[14]);
        editor.putBoolean("cb16", checked_state[15]);
        editor.putString("cbt1", checked_values[0]);
        editor.putString("cbt2", checked_values[1]);
        editor.putString("cbt3", checked_values[2]);
        editor.putString("cbt4", checked_values[3]);
        editor.putString("cbt5", checked_values[4]);
        editor.putString("cbt6", checked_values[5]);
        editor.putString("cbt7", checked_values[6]);
        editor.putString("cbt8", checked_values[7]);
        editor.putString("cbt9", checked_values[8]);
        editor.putString("cbt10", checked_values[9]);
        editor.putString("cbt11", checked_values[10]);
        editor.putString("cbt12", checked_values[11]);
        editor.putString("cbt13", checked_values[12]);
        editor.putString("cbt14", checked_values[13]);
        editor.putString("cbt15", checked_values[14]);
        editor.putString("cbt16", checked_values[15]);
        editor.putInt("radioParent", radio_id[0]);
        editor.putInt("radioCompare", radio_id[1]);
        editor.putInt("radioSave", radio_id[2]);
        editor.putInt("radioParent_0", radio_id[3]);
        editor.putInt("radioParent_1", radio_id[4]);
        editor.putInt("radioCompare_0", radio_id[5]);
        editor.putInt("radioCompare_1", radio_id[6]);
        editor.putInt("radioCompare_2", radio_id[7]);
        editor.putInt("radioCompare_3", radio_id[8]);
        editor.putInt("radioCompare_4", radio_id[9]);
        editor.putInt("radioSave_0", radio_id[10]);
        editor.putInt("radioSave_1", radio_id[11]);
        editor.putInt("radioSave_2", radio_id[12]);
        editor.putInt("radioSave_3", radio_id[13]);
        editor.putInt("radioSave_4", radio_id[14]);
        editor.putInt("radioParentLvchu", radioIdLvchu[0]);
        editor.putInt("radioCompareLvchu", radioIdLvchu[1]);
        editor.putInt("radioSaveLvchu", radioIdLvchu[2]);
        editor.putInt("radioParentLvchu_0", radioIdLvchu[3]);
        editor.putInt("radioParentLvchu_1", radioIdLvchu[4]);
        editor.putInt("radioCompareLvchu_0", radioIdLvchu[5]);
        editor.putInt("radioCompareLvchu_1", radioIdLvchu[6]);
        editor.putInt("radioCompareLvchu_2", radioIdLvchu[7]);
        editor.putInt("radioCompareLvchu_3", radioIdLvchu[8]);
        editor.putInt("radioCompareLvchu_4", radioIdLvchu[9]);
        editor.putInt("radioSaveLvchu_0", radioIdLvchu[10]);
        editor.putInt("radioSaveLvchu_1", radioIdLvchu[11]);
        editor.putInt("radioSaveLvchu_2", radioIdLvchu[12]);
        editor.putInt("radioSaveLvchu_3", radioIdLvchu[13]);
        editor.putInt("radioSaveLvchu_4", radioIdLvchu[14]);
        editor.putString("serial", serial);
        editor.putString("passway", passway);
        editor.apply();
        Toast.makeText(PassOpt.this, "保存成功", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(PassOpt.this, MainMenu.class));
        unbindService(serviceConnection);
        finish();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case RESULT_OK:
                Bundle b = data.getExtras(); //data为B中回传的Intent
                String str = b.getString("str1");//str即为回传的值
                break;
            default:
                break;
        }
    }

    public void fragmentHide() {
        if (customOpt != null) {
            transaction.hide(customOpt);
        }
        if (mazuiqijuOpt != null) {
            transaction.hide(mazuiqijuOpt);
        }
        if (lvchuOpt != null) {
            transaction.hide(lvchuOpt);
        }
        if (wuran05 != null) {
            transaction.hide(wuran05);
        }
        if (wuran98 != null) {
            transaction.hide(wuran98);
        }
        if (yaodianOpt != null) {
            transaction.hide(yaodianOpt);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //退出程序时关闭MyDatabaseHelper里的SQLiteDatabase
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
