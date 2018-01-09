package com.tdtf.weili.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListPopupWindow;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.tdtf.weili.api.SerialPort;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.information.Equation;
import com.tdtf.weili.information.Method;
import com.tdtf.weili.service.MyService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class CalibrationOpt extends AppCompatActivity {
    SharedPreferences coordinate;
    BigDecimal[] xray;
    BigDecimal[] yray;
    BigDecimal[] margs;
    BigDecimal[][] xishu;
    Boolean flag = true;

    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    SerialPort sp;
    FileOutputStream pOutputStream;
    Netprinter netprinter = new Netprinter();

    private ListPopupWindow popupWindow;
    ArrayList<String> select;
    ArrayList<String> lijing;
    ArrayList<String> biaochi;

    Button btnback;
    Button btnsave;
    Button btnselect;
    Button btnprint;
    EditText[] edit_lijing;
    EditText[] edit_biaochi;
    TextView[] textView;

    String print_time;
    String[] str_x, str_y, str_xx, str_yy, str_s, str_t, str_tt;

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
                        textPress = (TextView) findViewById(R.id.text_press_2);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_opt);
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
                        pOutputStream = outputStream;
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
        myDiary.diary(dbHelper, Diary.calibrateOpt_start);
        textView = new TextView[3];
        textView[0] = (TextView) findViewById(R.id.text_bd_point);
        textView[1] = (TextView) findViewById(R.id.text_bd_speed);
        textView[2] = (TextView) findViewById(R.id.text_bd_method);

        final TextClock textClock = (TextClock) findViewById(R.id.textClock3);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_2);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_2);
        textUser.setText("用户名:\n" + Myutils.getUsername());
        // TODO: 2017/8/1 时间格式
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
        print_time = formatter.format(curDate);
        // TODO: 2017/8/3 读取标尺
        str_s = getSharedPreference("selector");
        btnselect = (Button) findViewById(R.id.btn_select);
        btnselect.setText(str_s[0]);
        str_x = getSharedPreference("坐标X");
        str_y = getSharedPreference("坐标Y");
        str_t = getSharedPreference("state_1");
        str_xx = getSharedPreference("坐标XX");
        str_yy = getSharedPreference("坐标YY");
        str_tt = getSharedPreference("state_2");
        TextWatcher textWatcher_lijing = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 0 && s.toString().equals(".")) {
                    int m = 0;
                    while (m < 16) {
                        if (edit_lijing[m].getText().toString().equals(".")) {
                            edit_lijing[m].setText("");
                        }
                        m++;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                myDiary.diary(dbHelper, Diary.calibrateOpt_liJing_input(s.toString()));

            }
        };
        TextWatcher textWatcher_biaochi = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start == 0 && s.toString().equals(".")) {
                    int m = 0;
                    while (m < 16) {
                        if (edit_biaochi[m].getText().toString().equals(".")) {
                            edit_biaochi[m].setText("");
                        }
                        m++;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                myDiary.diary(dbHelper, Diary.calibrateOpt_biaoChi_input(s.toString()));

            }
        };

        edit_lijing = new EditText[16];
        edit_lijing[0] = (EditText) findViewById(R.id.edit_lijing0);
        edit_lijing[1] = (EditText) findViewById(R.id.edit_lijing1);
        edit_lijing[2] = (EditText) findViewById(R.id.edit_lijing2);
        edit_lijing[3] = (EditText) findViewById(R.id.edit_lijing3);
        edit_lijing[4] = (EditText) findViewById(R.id.edit_lijing4);
        edit_lijing[5] = (EditText) findViewById(R.id.edit_lijing5);
        edit_lijing[6] = (EditText) findViewById(R.id.edit_lijing6);
        edit_lijing[7] = (EditText) findViewById(R.id.edit_lijing7);
        edit_lijing[8] = (EditText) findViewById(R.id.edit_lijing8);
        edit_lijing[9] = (EditText) findViewById(R.id.edit_lijing9);
        edit_lijing[10] = (EditText) findViewById(R.id.edit_lijing10);
        edit_lijing[11] = (EditText) findViewById(R.id.edit_lijing11);
        edit_lijing[12] = (EditText) findViewById(R.id.edit_lijing12);
        edit_lijing[13] = (EditText) findViewById(R.id.edit_lijing13);
        edit_lijing[14] = (EditText) findViewById(R.id.edit_lijing14);
        edit_lijing[15] = (EditText) findViewById(R.id.edit_lijing15);

        for (int m = 0; m < 16; m++) {
            edit_lijing[m].addTextChangedListener(textWatcher_lijing);
        }

        edit_biaochi = new EditText[16];
        edit_biaochi[0] = (EditText) findViewById(R.id.edit_biaochi0);
        edit_biaochi[1] = (EditText) findViewById(R.id.edit_biaochi1);
        edit_biaochi[2] = (EditText) findViewById(R.id.edit_biaochi2);
        edit_biaochi[3] = (EditText) findViewById(R.id.edit_biaochi3);
        edit_biaochi[4] = (EditText) findViewById(R.id.edit_biaochi4);
        edit_biaochi[5] = (EditText) findViewById(R.id.edit_biaochi5);
        edit_biaochi[6] = (EditText) findViewById(R.id.edit_biaochi6);
        edit_biaochi[7] = (EditText) findViewById(R.id.edit_biaochi7);
        edit_biaochi[8] = (EditText) findViewById(R.id.edit_biaochi8);
        edit_biaochi[9] = (EditText) findViewById(R.id.edit_biaochi9);
        edit_biaochi[10] = (EditText) findViewById(R.id.edit_biaochi10);
        edit_biaochi[11] = (EditText) findViewById(R.id.edit_biaochi11);
        edit_biaochi[12] = (EditText) findViewById(R.id.edit_biaochi12);
        edit_biaochi[13] = (EditText) findViewById(R.id.edit_biaochi13);
        edit_biaochi[14] = (EditText) findViewById(R.id.edit_biaochi14);
        edit_biaochi[15] = (EditText) findViewById(R.id.edit_biaochi15);

        for (int m = 0; m < 16; m++) {
            edit_biaochi[m].addTextChangedListener(textWatcher_biaochi);
        }

        switch (str_s[0]) {
            case "标尺一":
                for (int m = 0; m < 16; m++) {
                    try {
                        edit_lijing[m].setText(str_x[m]);
                        edit_biaochi[m].setText(str_y[m]);
                        edit_lijing[m].setSelection(str_x[m].length());
                        edit_biaochi[m].setSelection(str_y[m].length());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (int l = 0; l < textView.length; l++) {
                    textView[l].setText(str_t[l]);
                }
                break;
            case "标尺二":
                for (int m = 0; m < 16; m++) {
                    try {
                        edit_lijing[m].setText(str_xx[m]);
                        edit_biaochi[m].setText(str_yy[m]);
                        edit_lijing[m].setSelection(str_xx[m].length());
                        edit_biaochi[m].setSelection(str_yy[m].length());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                for (int l = 0; l < textView.length; l++) {
                    textView[l].setText(str_tt[l]);
                }
                break;
            default:
                break;
        }

        select = new ArrayList<>();
        select.add("标尺一");
        select.add("标尺二");
//        select.add("标尺三");
        popupWindow = new ListPopupWindow(this);
        popupWindow.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, select) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#ff000000"));
                tv.setTextSize(24);
                return tv;
            }
        });
        popupWindow.setAnchorView(btnselect);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setModal(true);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                btnselect.setText(select.get(position));
                myDiary.diary(dbHelper, Diary.calibrateOpt_select(select.get(position)));
                switch (position) {
                    case 0:
                        for (int m = 0; m < 16; m++) {
                            try {
                                edit_lijing[m].setText(str_x[m]);
                                edit_biaochi[m].setText(str_y[m]);
                                edit_lijing[m].setSelection(str_x[m].length());
                                edit_biaochi[m].setSelection(str_y[m].length());
                            } catch (Exception e) {
                                e.printStackTrace();
                                edit_lijing[m].setText("");
                                edit_biaochi[m].setText("");
                            }
                        }
                        for (int l = 0; l < textView.length; l++) {
                            textView[l].setText(str_t[l]);
                        }
                        break;
                    case 1:
                        for (int m = 0; m < 16; m++) {
                            try {

                                edit_lijing[m].setText(str_xx[m]);
                                edit_biaochi[m].setText(str_yy[m]);
                                edit_lijing[m].setSelection(str_xx[m].length());
                                edit_biaochi[m].setSelection(str_yy[m].length());
                            } catch (Exception e) {
                                e.printStackTrace();
                                edit_lijing[m].setText("");
                                edit_biaochi[m].setText("");
                            }
                        }
                        for (int l = 0; l < textView.length; l++) {
                            textView[l].setText(str_tt[l]);
                        }
                        break;
                    default:
                        break;
                }
                popupWindow.dismiss();
            }
        });
        btnselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.show();
            }
        });

        btnsave = (Button) findViewById(R.id.btn_save);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lenth = 0;
                for (int k = 0; k < 16; k++) {
                    if (!TextUtils.isEmpty(edit_lijing[k].getText())) {
                        lenth++;
                    }
                }
                textView[0].setText(String.valueOf(lenth));
                if (lenth > 4) {
                    myDiary.diary(dbHelper, Diary.calibrateOpt_save);
                    for (int k = 0; k < 16; k++) {
                        if (!TextUtils.isEmpty(edit_lijing[k].getText())) {
                            if (TextUtils.isEmpty(edit_biaochi[k].getText())) {
                                String string = "已输入粒径的对应标尺不能为空";
                                flag = false;
                                Toast.makeText(CalibrationOpt.this, string, Toast.LENGTH_SHORT).show();
                                break;
                            } else {
                                flag = true;
                                //判断粒径输入格式
                                boolean flag_edit_liJing = true;
                                byte[] editByte = edit_lijing[k].getText().toString().getBytes();
                                for (int m = 0; m < editByte.length; m++) {
                                    if (editByte[m] == '.') {
                                        flag_edit_liJing = false;
                                        break;
                                    }
                                }
                                if (flag_edit_liJing) {
                                    edit_lijing[k].getText().append(".00");
                                } else {
                                    if (edit_lijing[k].getText().charAt(edit_lijing[k].getText().length()-2)=='.') {
                                        edit_lijing[k].getText().append("0");
                                    }
                                }
                                //判断标尺输入格式
                                boolean flag_edit_biaoChi = true;
                                byte[] editByte_biaoChi = edit_biaochi[k].getText().toString().getBytes();
                                for (int m = 0; m < editByte_biaoChi.length; m++) {
                                    if (editByte_biaoChi[m] == '.') {
                                        flag_edit_biaoChi = false;
                                        break;
                                    }
                                }
                                if (flag_edit_biaoChi) {
                                    edit_biaochi[k].getText().append(".0");
                                } else {
                                    if (edit_biaochi[k].getText().charAt(edit_biaochi[k].getText().length()-1)=='.') {
                                        edit_biaochi[k].getText().append("0");
                                    }
                                }
                                if (edit_lijing[k].getText().charAt(edit_lijing[k].getText().length()-3)!='.'||
                                        edit_biaochi[k].getText().charAt(edit_biaochi[k].getText().length()-2)!='.'){
                                    flag=false;
                                }
                            }
                        }
                    }
                    if (flag) {
                        xray = new BigDecimal[lenth];
                        yray = new BigDecimal[lenth];
                        for (int l = 0; l < lenth; l++) {
                            try {
                                xray[l] = new BigDecimal(edit_lijing[l].getText().toString());
                                yray[l] = new BigDecimal(edit_biaochi[l].getText().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        margs = new BigDecimal[lenth];
                        margs[0] = new BigDecimal("0");
                        margs[lenth - 1] = new BigDecimal("0");
                        BigDecimal[] mm = Equation.main(xray, yray, lenth);
                        for (int i = 0; i < lenth - 2; i++) {
                            margs[i + 1] = mm[i];
                        }

                        xishu = Method.main(xray, yray, margs);

                        String[] xcoodinate = new String[lenth];
                        for (int j = 0; j < xray.length; j++) {
                            xcoodinate[j] = xray[j].toString();
                        }
                        String[] ycoodinate = new String[lenth];
                        for (int m = 0; m < yray.length; m++) {
                            ycoodinate[m] = yray[m].toString();
                        }
                        String[] arrayxishu = new String[4 * (lenth - 1)];
                        for (int n = 0; n < xishu.length; n++) {
                            for (int h = 0; h < xishu[n].length; h++) {
                                arrayxishu[h + n * xishu[n].length] = xishu[n][h].toString();
                            }
                        }
                        String[] selector = new String[]{btnselect.getText().toString()};
                        String[] str_text = new String[]{
                                textView[0].getText().toString(),
                                textView[1].getText().toString(),
                                textView[2].getText().toString()
                        };
                        switch (btnselect.getText().toString()) {
                            case "标尺一":
                                setSharedPreference("坐标X", xcoodinate);
                                setSharedPreference("坐标Y", ycoodinate);
                                setSharedPreference("state_1", str_text);
                                break;
                            case "标尺二":
                                setSharedPreference("坐标XX", xcoodinate);
                                setSharedPreference("坐标YY", ycoodinate);
                                setSharedPreference("state_2", str_text);
                                break;
                            default:
                                break;
                        }
                        setSharedPreference("系数", arrayxishu);
                        setSharedPreference("selector", selector);
                        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CalibrationOpt.this, Calibration.class));
                        unbindService(serviceConnection);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "保存失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "输入坐标不能少于4个", Toast.LENGTH_SHORT).show();
                }

            }
        });
        btnprint = (Button) findViewById(R.id.btn_print);
        btnprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.calibrateOpt_print);
                /*TODO 打开串口*/
                try {
                    sp = new SerialPort(new File("/dev/ttyAMA2"), 9600, 0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mOutputStream = (FileOutputStream) sp.getOutputStream();
                lijing = new ArrayList<>();
                biaochi = new ArrayList<>();
                for (int m = 0; m < 16; m++) {
                    lijing.add(edit_lijing[m].getText().toString());
                    biaochi.add(edit_biaochi[m].getText().toString());
                }
                try {
                    for (int l = 15; l > -1; l--) {
                        netprinter.printindent(mOutputStream);
                        netprinter.printdatalijing(mOutputStream, lijing.get(l), 6);
                        netprinter.printdatajifen(mOutputStream, biaochi.get(l), 13);
                        netprinter.printline(mOutputStream);
                    }
                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "粒径");
                    netprinter.printblank(mOutputStream);
                    netprinter.printtitle(mOutputStream, "标尺");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    for (int x = 0; x < 30; x++) {
                        netprinter.printtitle(mOutputStream, "-");
                    }
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "选择标尺：" + btnselect.getText().toString());
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + print_time);
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "检测时间：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "标定点：008");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "取样速度：015");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "标定温度：36℃");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "标定方法：乳胶球");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    for (int x = 0; x < 30; x++) {
                        netprinter.printtitle(mOutputStream, "-");
                    }
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "标尺设置");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    for (int x = 0; x < 30; x++) {
                        netprinter.printtitle(mOutputStream, "-");
                    }
                    netprinter.printline(mOutputStream);

                    netprinter.printline(mOutputStream);
                    netprinter.printline(mOutputStream);
                    netprinter.printline(mOutputStream);
                    mOutputStream.write(0x0d);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sp.close();
            }
        });

        btnback = (Button) findViewById(R.id.btn_back);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.calibrateOpt_back);
                startActivity(new Intent(CalibrationOpt.this, Calibration.class));
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
