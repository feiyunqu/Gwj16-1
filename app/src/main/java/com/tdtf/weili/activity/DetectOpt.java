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
import com.tdtf.weili.Utils.Power;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.service.MyService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;

import static android.text.InputType.TYPE_NULL;

public class DetectOpt extends AppCompatActivity {
    SharedPreferences perferences;
    MyDiary myDiary = new MyDiary();
    MyDatabaseHelper dbHelper;

    private ListPopupWindow popupWindow;
    private ListPopupWindow popupWindow2;

    ArrayList<String> strjcfs;
    ArrayList<String> strqyfs;

    TextView[] textopt;
    EditText[] editTexts;
    Button btnqyfs;
    Button btnjcfs;
    Button btnsave;
    Button btnback;

    boolean flag_limit = true;

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
                        textPress = (TextView) findViewById(R.id.text_press_5);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_opt);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                myBinder = (MyService.MyBinder) service;
                myService = myBinder.getService();
                myBinder.threadGo();
                Log.d("tag", "onServiceConnected: 1");
                myBinder.press();
                Log.d("tag", "onServiceConnected: 2");
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
        myDiary.diary(dbHelper, Diary.detectOpt_start);
        final TextClock textClock = (TextClock) findViewById(R.id.textClock6);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_7);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_7);
        textUser.setText("用户名:\n" + Myutils.getUsername());
        // TODO: 2016/12/7 Shared preferences
        perferences = getSharedPreferences("jianceshezhi", MODE_PRIVATE);
        editTexts = new EditText[5];
        editTexts[0] = (EditText) findViewById(R.id.edityz);
        editTexts[1] = (EditText) findViewById(R.id.editqy);
        editTexts[4] = (EditText) findViewById(R.id.editjccs);
        editTexts[3] = (EditText) findViewById(R.id.editqywz);
        editTexts[2] = (EditText) findViewById(R.id.editjs);
        textopt = new TextView[5];
        textopt[0] = (TextView) findViewById(R.id.textView_yuzou);
        textopt[1] = (TextView) findViewById(R.id.textView_quyang);
        textopt[4] = (TextView) findViewById(R.id.textView_cishu);
        textopt[3] = (TextView) findViewById(R.id.textView_press);
        textopt[2] = (TextView) findViewById(R.id.textView_jishu);

        editTexts[0].setText(perferences.getString("yuzou", ""));
        if (!Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[4])) {
            editTexts[0].setInputType(TYPE_NULL);
        }

        editTexts[1].setText(perferences.getString("quyang", ""));
        if (!Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[5])) {
            editTexts[1].setInputType(TYPE_NULL);
        }

        editTexts[4].setText(perferences.getString("jccs", ""));
        if (!Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[6])) {
            editTexts[4].setInputType(TYPE_NULL);
        }

        editTexts[3].setText(String.valueOf((float)Integer.parseInt(perferences.getString("qywz", ""))/10));
        if (!Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[7])) {
            editTexts[3].setInputType(TYPE_NULL);
        }

        editTexts[2].setText(perferences.getString("jishu", ""));
        if (!Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[8])) {
            editTexts[2].setInputType(TYPE_NULL);
        }

        for (int l = 0; l < 5; l++) {
            editTexts[l].setSelection(editTexts[l].getText().toString().length());
        }

        btnjcfs = (Button) findViewById(R.id.btn_jcfs);
        btnjcfs.setText(perferences.getString("jcfs", ""));
        strjcfs = new ArrayList<>();
        strjcfs.add("自动");
        strjcfs.add("手动");
        popupWindow = new ListPopupWindow(this);
        popupWindow.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strjcfs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#ff000000"));
                tv.setTextSize(24);
                return tv;
            }
        });
        popupWindow.setAnchorView(btnjcfs);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setModal(true);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                btnjcfs.setText(strjcfs.get(position));
                myDiary.diary(dbHelper, Diary.detectOpt_jianCeFangShi(strjcfs.get(position)));
                popupWindow.dismiss();
            }
        });
        btnjcfs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[2])) {
                    popupWindow.show();
                } else {
                    Toast.makeText(DetectOpt.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //////////////////////////////////////////////////////////////////////
        btnqyfs = (Button) findViewById(R.id.btn_qyfs);
        btnqyfs.setText(perferences.getString("qyfs", null));
        strqyfs = new ArrayList<>();
        strqyfs.add("自动");
        strqyfs.add("手动");
        popupWindow2 = new ListPopupWindow(this);
        popupWindow2.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, strqyfs) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#ff000000"));
                tv.setTextSize(24);
                return tv;
            }
        });
        popupWindow2.setAnchorView(btnqyfs);
        popupWindow2.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow2.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow2.setModal(true);
        popupWindow2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                btnqyfs.setText(strqyfs.get(position));
                myDiary.diary(dbHelper, Diary.detectOpt_quYangFangShi(strqyfs.get(position)));
                popupWindow2.dismiss();
            }
        });
        btnqyfs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[3])) {
                    popupWindow2.show();
                } else {
                    Toast.makeText(DetectOpt.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // TODO: 2016/12/5
        btnsave = (Button) findViewById(R.id.btnsave);
        btnsave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btnsave.setFocusableInTouchMode(true);
                btnsave.requestFocusFromTouch();
                boolean flag_null = true;
                for (int l = 0; l < 5; l++) {
                    if (TextUtils.isEmpty(editTexts[l].getText())) {
                        flag_null = false;
                        Toast.makeText(DetectOpt.this, textopt[l].getText().toString() + "输入不能为空",
                                Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        flag_null = true;
                    }
                }
                if (flag_limit && flag_null) {
                    myDiary.diary(dbHelper, Diary.detectOpt_go);
                    float fPress=Float.parseFloat(editTexts[3].getText().toString());
                    int iPress=(int)(fPress*10);
                    String sPress=String.valueOf(iPress);
                    Log.d("ffff", "onClick: "+sPress);
                    SharedPreferences.Editor editor;
                    editor = perferences.edit();
                    editor.putString("yuzou", editTexts[0].getText().toString());
                    editor.putString("quyang", editTexts[1].getText().toString());
                    editor.putString("jccs", editTexts[4].getText().toString());
                    editor.putString("qywz", sPress);
                    editor.putString("jishu", editTexts[2].getText().toString());
                    editor.putString("jcfs", btnjcfs.getText().toString());
                    editor.putString("qyfs", btnqyfs.getText().toString());
                    editor.apply();
                    Toast.makeText(DetectOpt.this, "保存成功", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(DetectOpt.this, MainMenu.class));
                    unbindService(serviceConnection);
                    finish();
                }
            }
        });
        // TODO: 2016/12/5  返回
        btnback = (Button) findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.detectOpt_back);
                startActivity(new Intent(DetectOpt.this, MainMenu.class));
                unbindService(serviceConnection);
                finish();
            }
        });

        editTexts[0].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(editTexts[0].getText())) {
                        if (!(editTexts[0].getText().toString().startsWith(".") || editTexts[0].getText().toString().endsWith("."))) {
                            BigDecimal bigDecimal = new BigDecimal(editTexts[0].getText().toString());
                            if (bigDecimal.compareTo(new BigDecimal("0.2")) == -1) {
                                editTexts[0].setText("0.2");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("1.0")) == 1) {
                                    editTexts[0].setText("1.0");
                                } else {
                                    boolean flag_edit = true;
                                    byte[] editByte = editTexts[0].getText().toString().getBytes();
                                    for (int k = 0; k < editByte.length; k++) {
                                        if (editByte[k] == '.') {
                                            flag_edit = false;
                                            break;
                                        }
                                    }
                                    if (flag_edit) {
                                        editTexts[0].getText().append(".0");
                                    }
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(DetectOpt.this, "预走量输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.detectOpt_yuZou(editTexts[0].getText().toString()));
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
                            if (bigDecimal.compareTo(new BigDecimal("0.2")) == -1) {
                                editTexts[1].setText("0.2");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("500.0")) == 1) {
                                    editTexts[1].setText("500.0");
                                } else {
                                    boolean flag_edit = true;
                                    byte[] editByte = editTexts[1].getText().toString().getBytes();
                                    for (int k = 0; k < editByte.length; k++) {
                                        if (editByte[k] == '.') {
                                            flag_edit = false;
                                            break;
                                        }
                                    }
                                    if (flag_edit) {
                                        editTexts[1].getText().append(".0");
                                    }
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(DetectOpt.this, "取样量输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.detectOpt_quYang(editTexts[1].getText().toString()));
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
                            if (bigDecimal.compareTo(new BigDecimal("0.2")) == -1) {
                                editTexts[2].setText("0.2");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("1.0")) == 1) {
                                    editTexts[2].setText("1.0");
                                } else {
                                    boolean flag_edit = true;
                                    byte[] editByte = editTexts[2].getText().toString().getBytes();
                                    for (int k = 0; k < editByte.length; k++) {
                                        if (editByte[k] == '.') {
                                            flag_edit = false;
                                            break;
                                        }
                                    }
                                    if (flag_edit) {
                                        editTexts[2].getText().append(".0");
                                    }
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(DetectOpt.this, "计数输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.detectOpt_jiShu(editTexts[2].getText().toString()));
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
                            if (bigDecimal.compareTo(new BigDecimal("0.5")) == -1) {
                                editTexts[3].setText("0.5");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("3.0")) == 1) {
                                    editTexts[3].setText("3.0");
                                }else {
                                    boolean flag_edit = true;
                                    byte[] editByte = editTexts[3].getText().toString().getBytes();
                                    for (int k = 0; k < editByte.length; k++) {
                                        if (editByte[k] == '.') {
                                            flag_edit = false;
                                            break;
                                        }
                                    }
                                    if (flag_edit) {
                                        editTexts[3].getText().append(".0");
                                    }
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(DetectOpt.this, "压力输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.detectOpt_pressValue(editTexts[3].getText().toString()));
                    }
                    btnsave.setFocusableInTouchMode(false);
                }
            }
        });
        editTexts[4].setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!TextUtils.isEmpty(editTexts[4].getText())) {
                        if (!(editTexts[4].getText().toString().startsWith(".") || editTexts[4].getText().toString().endsWith("."))) {
                            BigDecimal bigDecimal = new BigDecimal(editTexts[4].getText().toString());
                            if (bigDecimal.compareTo(new BigDecimal("1")) == -1) {
                                editTexts[4].setText("1");
                            } else {
                                if (bigDecimal.compareTo(new BigDecimal("12")) == 1) {
                                    editTexts[4].setText("12");
                                }
                            }
                            flag_limit = true;
                        } else {
                            flag_limit = false;
                            Toast.makeText(DetectOpt.this, "检测次数输入不正确", Toast.LENGTH_SHORT).show();
                        }
                        myDiary.diary(dbHelper, Diary.detectOpt_ciShu(editTexts[4].getText().toString()));
                    }
                    btnsave.setFocusableInTouchMode(false);
                }
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
