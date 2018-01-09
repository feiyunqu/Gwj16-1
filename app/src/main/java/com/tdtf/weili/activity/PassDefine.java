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

public class PassDefine extends AppCompatActivity {
    SharedPreferences coordinate;
    SharedPreferences perferences;
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    Boolean limit_lj;

    Button btnsave;
    Button btnback;
    EditText[] edit_lijing;
    TextView[] text_biaochi;

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
                        textPress = (TextView) findViewById(R.id.text_press_8);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_define);
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
        final TextClock textClock = (TextClock) findViewById(R.id.textClock8);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_10);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_10);
        textUser.setText("用户名:\n" + Myutils.getUsername());
        dbHelper = new MyDatabaseHelper(this);
        myDiary.diary(dbHelper, Diary.passDefine_start);

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

        edit_lijing = new EditText[16];
        edit_lijing[0] = (EditText) findViewById(R.id.edit_lijing1);
        edit_lijing[1] = (EditText) findViewById(R.id.edit_lijing2);
        edit_lijing[2] = (EditText) findViewById(R.id.edit_lijing3);
        edit_lijing[3] = (EditText) findViewById(R.id.edit_lijing4);
        edit_lijing[4] = (EditText) findViewById(R.id.edit_lijing5);
        edit_lijing[5] = (EditText) findViewById(R.id.edit_lijing6);
        edit_lijing[6] = (EditText) findViewById(R.id.edit_lijing7);
        edit_lijing[7] = (EditText) findViewById(R.id.edit_lijing8);
        edit_lijing[8] = (EditText) findViewById(R.id.edit_lijing9);
        edit_lijing[9] = (EditText) findViewById(R.id.edit_lijing10);
        edit_lijing[10] = (EditText) findViewById(R.id.edit_lijing11);
        edit_lijing[11] = (EditText) findViewById(R.id.edit_lijing12);
        edit_lijing[12] = (EditText) findViewById(R.id.edit_lijing13);
        edit_lijing[13] = (EditText) findViewById(R.id.edit_lijing14);
        edit_lijing[14] = (EditText) findViewById(R.id.edit_lijing15);
        edit_lijing[15] = (EditText) findViewById(R.id.edit_lijing16);

        text_biaochi = new TextView[16];
        text_biaochi[0] = (TextView) findViewById(R.id.text_biaochi1);
        text_biaochi[1] = (TextView) findViewById(R.id.text_biaochi2);
        text_biaochi[2] = (TextView) findViewById(R.id.text_biaochi3);
        text_biaochi[3] = (TextView) findViewById(R.id.text_biaochi4);
        text_biaochi[4] = (TextView) findViewById(R.id.text_biaochi5);
        text_biaochi[5] = (TextView) findViewById(R.id.text_biaochi6);
        text_biaochi[6] = (TextView) findViewById(R.id.text_biaochi7);
        text_biaochi[7] = (TextView) findViewById(R.id.text_biaochi8);
        text_biaochi[8] = (TextView) findViewById(R.id.text_biaochi9);
        text_biaochi[9] = (TextView) findViewById(R.id.text_biaochi10);
        text_biaochi[10] = (TextView) findViewById(R.id.text_biaochi11);
        text_biaochi[11] = (TextView) findViewById(R.id.text_biaochi12);
        text_biaochi[12] = (TextView) findViewById(R.id.text_biaochi13);
        text_biaochi[13] = (TextView) findViewById(R.id.text_biaochi14);
        text_biaochi[14] = (TextView) findViewById(R.id.text_biaochi15);
        text_biaochi[15] = (TextView) findViewById(R.id.text_biaochi16);

        for (int y = 0; y < 16; y++) {
            edit_lijing[y].setTag(y);
        }

        TextWatcher textdefine = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                View rootview = PassDefine.this.getWindow().getDecorView();
                View aaa = rootview.findFocus();
                try {
                    EditText bbb = (EditText) aaa;
                    if (bbb.getText().toString().startsWith(".")) {
                        bbb.setText("");
                    }
                    myDiary.diary(dbHelper, Diary.passDefine_input(bbb.getText().toString()));
                    if (!TextUtils.isEmpty(bbb.getText())) {
                        BigDecimal bigDecimal = new BigDecimal(bbb.getText().toString());
                        if ((bigDecimal.compareTo(xray[0]) == 1 || bigDecimal.compareTo(xray[0]) == 0) &&
                                (bigDecimal.compareTo(xray[x.length - 1]) == -1 || bigDecimal.compareTo(xray[x.length - 1]) == 0)) {
                            String anwser = biaochi(xray, bigDecimal, a, b, c, d);
                            text_biaochi[(int) bbb.getTag()].setText(anwser);
                        } else {
                            Toast.makeText(getApplicationContext(), "粒径超出曲线范围", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                int m = 0;
//                while (m < 16) {
//                    if (edit_lijing[m].equals(aaa)) {
//                        myDiary.diary(dbHelper, Diary.passDefine_input(edit_lijing[m].getText().toString()));
//                        if (start == 0 && s.toString().equals(".")) {
//                            edit_lijing[m].setText("");
//                        }
//
//                        try {
//                            if (!TextUtils.isEmpty(edit_lijing[m].getText())) {
//                                if (edit_lijing[m].getText().length() > 2 && edit_lijing[m].getText().charAt(edit_lijing[m].getText().length() - 3) == '.') {
//                                    BigDecimal bigDecimal = new BigDecimal(edit_lijing[m].getText().toString());
//                                    if ((bigDecimal.compareTo(xray[0]) == 1 || bigDecimal.compareTo(xray[0]) == 0) &&
//                                            (bigDecimal.compareTo(xray[x.length - 1]) == -1 || bigDecimal.compareTo(xray[x.length - 1]) == 0)) {
//                                        String anwser = biaochi(xray, bigDecimal, a, b, c, d);
//                                        text_biaochi[m].setText(anwser);
//                                    } else {
//                                        Toast.makeText(getApplicationContext(), "粒径超出曲线范围", Toast.LENGTH_SHORT).show();
//                                    }
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                        break;
//                    }
//                    m++;
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        for (int m = 0; m < 16; m++) {
            edit_lijing[m].addTextChangedListener(textdefine);
            edit_lijing[m].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText editSelf=(EditText)v;
                    if (!TextUtils.isEmpty(editSelf.getText())){
                        byte[] editByte = editSelf.getText().toString().getBytes();
                        for (int m = 0; m < editByte.length; m++) {
                            if (editByte[m] == '.') {
                                if (m == editByte.length - 1) {//最后一位是小数点
                                    editSelf.setText(editSelf.getText().toString() + "00");
                                }
                                if (m == editByte.length - 2) {
                                    editSelf.setText(editSelf.getText().toString() + "0");
                                }
                                if (m<editByte.length - 2){
                                    editSelf.setText(editSelf.getText().toString().substring(0,m+3));
                                }
                                break;
                            }
                            if (m == editByte.length - 1) {
                                editSelf.setText(editSelf.getText().toString() + ".00");
                            }
                        }
                        editSelf.selectAll();
                        BigDecimal bigDecimal = new BigDecimal(editSelf.getText().toString());
                        if (!((bigDecimal.compareTo(xray[0]) == 1 || bigDecimal.compareTo(xray[0]) == 0) &&
                                (bigDecimal.compareTo(xray[x.length - 1]) == -1 || bigDecimal.compareTo(xray[x.length - 1]) == 0))) {
                            text_biaochi[(int)editSelf.getTag()].setText("");
                            Toast.makeText(getApplicationContext(), "粒径超出曲线范围", Toast.LENGTH_SHORT).show();
                        }else {
                            String anwser = biaochi(xray, bigDecimal, a, b, c, d);
                            text_biaochi[(int)editSelf.getTag()].setText(anwser);
                        }
                        btnsave.setFocusableInTouchMode(false);
                    }
                }
            });
        }

        perferences = getSharedPreferences("checkBoxState", MODE_PRIVATE);
        edit_lijing[0].setText(perferences.getString("cbt1", ""));
        edit_lijing[1].setText(perferences.getString("cbt2", ""));
        edit_lijing[2].setText(perferences.getString("cbt3", ""));
        edit_lijing[3].setText(perferences.getString("cbt4", ""));
        edit_lijing[4].setText(perferences.getString("cbt5", ""));
        edit_lijing[5].setText(perferences.getString("cbt6", ""));
        edit_lijing[6].setText(perferences.getString("cbt7", ""));
        edit_lijing[7].setText(perferences.getString("cbt8", ""));
        edit_lijing[8].setText(perferences.getString("cbt9", ""));
        edit_lijing[9].setText(perferences.getString("cbt10", ""));
        edit_lijing[10].setText(perferences.getString("cbt11", ""));
        edit_lijing[11].setText(perferences.getString("cbt12", ""));
        edit_lijing[12].setText(perferences.getString("cbt13", ""));
        edit_lijing[13].setText(perferences.getString("cbt14", ""));
        edit_lijing[14].setText(perferences.getString("cbt15", ""));
        edit_lijing[15].setText(perferences.getString("cbt16", ""));
        text_biaochi[0].setText(perferences.getString("bc1", ""));
        text_biaochi[1].setText(perferences.getString("bc2", ""));
        text_biaochi[2].setText(perferences.getString("bc3", ""));
        text_biaochi[3].setText(perferences.getString("bc4", ""));
        text_biaochi[4].setText(perferences.getString("bc5", ""));
        text_biaochi[5].setText(perferences.getString("bc6", ""));
        text_biaochi[6].setText(perferences.getString("bc7", ""));
        text_biaochi[7].setText(perferences.getString("bc8", ""));
        text_biaochi[8].setText(perferences.getString("bc9", ""));
        text_biaochi[9].setText(perferences.getString("bc10", ""));
        text_biaochi[10].setText(perferences.getString("bc11", ""));
        text_biaochi[11].setText(perferences.getString("bc12", ""));
        text_biaochi[12].setText(perferences.getString("bc13", ""));
        text_biaochi[13].setText(perferences.getString("bc14", ""));
        text_biaochi[14].setText(perferences.getString("bc15", ""));
        text_biaochi[15].setText(perferences.getString("bc16", ""));

        btnsave = (Button) findViewById(R.id.btnsave);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnsave.setFocusableInTouchMode(true);
                btnsave.requestFocusFromTouch();
                for (int l = 0; l < 16; l++) {
                    if (!TextUtils.isEmpty(text_biaochi[l].getText())) {
                        limit_lj = true;
                    } else {
                        Toast.makeText(PassDefine.this, "数据输入不正确", Toast.LENGTH_SHORT).show();
                        limit_lj = false;
                        break;
                    }
                }
                myDiary.diary(dbHelper, Diary.passDefine_save);
//                flag = true;
//                for (int k = 0; k < 15; k++) {
//                    if (new BigDecimal(edit_lijing[k].getText().toString()).compareTo(
//                            new BigDecimal(edit_lijing[k + 1].getText().toString())) == -1) {
//                        flag = true;
//                    } else {
//                        flag = false;
//                        Toast.makeText(PassDefine.this, "粒径大小顺序错误", Toast.LENGTH_SHORT).show();
//                        break;
//                    }
//                }
                if (limit_lj) {
                    BigDecimal[] bigDecimals = new BigDecimal[16];
                    for (int k = 0; k < 16; k++) {
                        bigDecimals[k] = new BigDecimal(edit_lijing[k].getText().toString());
                    }
                    for (int i = 0; i < 16 - 1; i++) {
                        for (int j = 0; j < 16 - 1; j++) {
                            if (bigDecimals[j].compareTo(bigDecimals[j + 1]) == 1) {
                                BigDecimal bD_tem = bigDecimals[j];
                                bigDecimals[j] = bigDecimals[j + 1];
                                bigDecimals[j + 1] = bD_tem;
                            }
                        }
                    }
                    for (int k = 0; k < 16; k++) {
                        edit_lijing[k].setText(String.valueOf(bigDecimals[k]));
                        text_biaochi[k].setText(biaochi(xray, bigDecimals[k], a, b, c, d));
                    }

                    SharedPreferences.Editor editor;
                    editor = perferences.edit();
                    editor.putString("cbt1", edit_lijing[0].getText().toString());
                    editor.putString("cbt2", edit_lijing[1].getText().toString());
                    editor.putString("cbt3", edit_lijing[2].getText().toString());
                    editor.putString("cbt4", edit_lijing[3].getText().toString());
                    editor.putString("cbt5", edit_lijing[4].getText().toString());
                    editor.putString("cbt6", edit_lijing[5].getText().toString());
                    editor.putString("cbt7", edit_lijing[6].getText().toString());
                    editor.putString("cbt8", edit_lijing[7].getText().toString());
                    editor.putString("cbt9", edit_lijing[8].getText().toString());
                    editor.putString("cbt10", edit_lijing[9].getText().toString());
                    editor.putString("cbt11", edit_lijing[10].getText().toString());
                    editor.putString("cbt12", edit_lijing[11].getText().toString());
                    editor.putString("cbt13", edit_lijing[12].getText().toString());
                    editor.putString("cbt14", edit_lijing[13].getText().toString());
                    editor.putString("cbt15", edit_lijing[14].getText().toString());
                    editor.putString("cbt16", edit_lijing[15].getText().toString());
                    editor.putString("bc1", text_biaochi[0].getText().toString());
                    editor.putString("bc2", text_biaochi[1].getText().toString());
                    editor.putString("bc3", text_biaochi[2].getText().toString());
                    editor.putString("bc4", text_biaochi[3].getText().toString());
                    editor.putString("bc5", text_biaochi[4].getText().toString());
                    editor.putString("bc6", text_biaochi[5].getText().toString());
                    editor.putString("bc7", text_biaochi[6].getText().toString());
                    editor.putString("bc8", text_biaochi[7].getText().toString());
                    editor.putString("bc9", text_biaochi[8].getText().toString());
                    editor.putString("bc10", text_biaochi[9].getText().toString());
                    editor.putString("bc11", text_biaochi[10].getText().toString());
                    editor.putString("bc12", text_biaochi[11].getText().toString());
                    editor.putString("bc13", text_biaochi[12].getText().toString());
                    editor.putString("bc14", text_biaochi[13].getText().toString());
                    editor.putString("bc15", text_biaochi[14].getText().toString());
                    editor.putString("bc16", text_biaochi[15].getText().toString());
                    editor.apply();

                    Toast.makeText(getApplicationContext(), "已完成排序并保存", Toast.LENGTH_SHORT).show();
                    startActivityForResult(new Intent(PassDefine.this, PassOpt.class), 0);
                    unbindService(serviceConnection);
                    finish();
                }
            }
        });
        btnback = (Button) findViewById(R.id.btn_back);
        btnback.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.passDefine_back);
                startActivityForResult(new Intent(PassDefine.this, PassOpt.class), 0);
                unbindService(serviceConnection);
                finish();
            }
        });
    }

    // TODO: 2017/4/11 参数依顺序分别是X坐标，通道，系数a，系数b，系数c，系数d，返回值：串口指令
    private static String biaochi(BigDecimal[] xray, BigDecimal pass, BigDecimal[] a, BigDecimal[] b, BigDecimal[] c, BigDecimal[] d) {
        BigDecimal saber;
        String archer = "";
        for (int i = 0; i < xray.length - 1; i++) {
            if ((pass.compareTo(xray[i]) == 1 || pass.compareTo(xray[i]) == 0) && (pass.compareTo(xray[i + 1]) == -1 || pass.compareTo(xray[i + 1]) == 0)) {
                saber = a[i].multiply(pass).multiply(pass).multiply(pass).add(b[i].multiply(pass).multiply(pass)).add(c[i].multiply(pass)).add(d[i]);
                archer = saber.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                break;
            }
        }
        return archer;
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        //退出程序时关闭MyDatabaseHelper里的SQLiteDatabase
        if (dbHelper != null) {
            dbHelper.close();
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
