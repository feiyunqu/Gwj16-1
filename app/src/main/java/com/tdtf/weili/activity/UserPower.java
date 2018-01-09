package com.tdtf.weili.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.SQLException;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.fragment.radiobutton.RadioPower_0;
import com.tdtf.weili.fragment.radiobutton.RadioPower_1;
import com.tdtf.weili.fragment.radiobutton.RadioPower_2;
import com.tdtf.weili.service.MyService;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UserPower extends AppCompatActivity {
    Button btnapply;
    Button btnback;
    Button btnadd;
    Button btnupdate;
    EditText edit_user;
    EditText edit_password;
    EditText edit_truepassword;
    Spinner spinner_power;
    RadioGroup powergroup;
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    FragmentManager fragmentManager;
    FragmentTransaction transaction;
    private RadioPower_0 radiopower0;
    private RadioPower_1 radiopower1;
    private RadioPower_2 radiopower2;
    String powerdata0, powerdata1, powerdata2;
    List<String> spinnerList;
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
                        textPress = (TextView) findViewById(R.id.text_press_12);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure / 10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_power);
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
        final TextClock textClock = (TextClock) findViewById(R.id.textClock12);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_16);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_16);
        textUser.setText("用户名:\n" + Myutils.getUsername());
        dbHelper = new MyDatabaseHelper(this);
        myDiary.diary(dbHelper, Diary.userPower_start);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                myDiary.diary(dbHelper, Diary.userPower_name_input(s.toString()));
            }
        };
        powergroup = (RadioGroup) findViewById(R.id.power_group);
        powergroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton:
                        myDiary.diary(dbHelper, Diary.userPower_powName("管理员"));
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        if (radiopower0 == null) {
                            radiopower0 = new RadioPower_0();
                            transaction.add(R.id.radiofragment, radiopower0);
                        }
                        if (radiopower0 != null) {
                            transaction.hide(radiopower0);
                        }
                        if (radiopower1 != null) {
                            transaction.hide(radiopower1);
                        }
                        if (radiopower2 != null) {
                            transaction.hide(radiopower2);
                        }
                        transaction.show(radiopower0);
                        transaction.commit();
                        break;
                    case R.id.radioButton2:
                        myDiary.diary(dbHelper, Diary.userPower_powName("操作员"));
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        if (radiopower1 == null) {
                            radiopower1 = new RadioPower_1();
                            transaction.add(R.id.radiofragment, radiopower1);
                        }
                        if (radiopower0 != null) {
                            transaction.hide(radiopower0);
                        }
                        if (radiopower1 != null) {
                            transaction.hide(radiopower1);
                        }
                        if (radiopower2 != null) {
                            transaction.hide(radiopower2);
                        }
                        transaction.show(radiopower1);
                        transaction.commit();
                        break;
                    case R.id.radioButton3:
                        myDiary.diary(dbHelper, Diary.userPower_powName("维护员"));
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        if (radiopower2 == null) {
                            radiopower2 = new RadioPower_2();
                            transaction.add(R.id.radiofragment, radiopower2);
                        }
                        if (radiopower0 != null) {
                            transaction.hide(radiopower0);
                        }
                        if (radiopower1 != null) {
                            transaction.hide(radiopower1);
                        }
                        if (radiopower2 != null) {
                            transaction.hide(radiopower2);
                        }
                        transaction.show(radiopower2);
                        transaction.commit();
                        break;
                    default:
                        break;
                }
            }
        });
        btnapply = (Button) findViewById(R.id.btn_apply);
        btnapply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.userPower_save);
                try {
                    radiopower0.setpower(new RadioPower_0.CallBacks() {
                        @Override
                        public void process(String str) {
                            powerdata0 = str;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    radiopower1.setpower(new RadioPower_1.CallBacks() {
                        @Override
                        public void process(String str) {
                            powerdata1 = str;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    radiopower2.setpower(new RadioPower_2.CallBacks() {
                        @Override
                        public void process(String str) {
                            powerdata2 = str;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (radiopower0!=null){
                        dbHelper.getReadableDatabase().execSQL(
                                "update Power set powerData = ? where powerName = ?",
                                new String[]{powerdata0, "管理员"});
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    if (radiopower1 != null) {
                        dbHelper.getReadableDatabase().execSQL(
                                "update Power set powerData = ? where powerName = ?",
                                new String[]{powerdata1, "操作员"});
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                try {
                    if (radiopower2 != null) {
                        dbHelper.getReadableDatabase().execSQL(
                                "update Power set powerData = ? where powerName = ?",
                                new String[]{powerdata2, "维护员"});
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                Toast.makeText(UserPower.this,"保存成功",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UserPower.this, Calibration.class));
                unbindService(serviceConnection);
                finish();
            }
        });
        btnback = (Button) findViewById(R.id.btn_back);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.userPower_back);
                startActivity(new Intent(UserPower.this, Calibration.class));
                unbindService(serviceConnection);
                finish();
            }
        });
        //////////////////////////////////////////////////////////
        edit_user = (EditText) findViewById(R.id.edit_user);
        edit_user.addTextChangedListener(textWatcher);
        edit_password = (EditText) findViewById(R.id.edit_password);
        edit_truepassword = (EditText) findViewById(R.id.edit_truepassword);
        spinner_power = (Spinner) findViewById(R.id.spinner_power);
        ArrayAdapter<String> spinnerAadapter = new ArrayAdapter<>(this, R.layout.spinner_text_item, getDataSource());
        spinnerAadapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner_power.setAdapter(spinnerAadapter);
        spinner_power.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                myDiary.diary(dbHelper, Diary.userPower_power(spinnerList.get(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnadd = (Button) findViewById(R.id.btnadd);
        btnadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.userPower_add);
                if (!TextUtils.isEmpty(edit_user.getText())) {
                    Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                            "select name from User where userName=?", new String[]{edit_user.getText().toString()});
                    if (!cursor.moveToFirst()) {
                        if (edit_password.getText().toString().equals(edit_truepassword.getText().toString())) {
                            dbHelper.getReadableDatabase().execSQL("insert into User values(null,?,?,?,?,?)", new Object[]{
                                    spinner_power.getSelectedItemPosition(),
                                    Myutils.formatDateTime(System.currentTimeMillis()),
                                    spinner_power.getSelectedItem().toString(),
                                    edit_user.getText().toString(),
                                    edit_password.getText().toString()
                            });
                            edit_user.setText("");
                            edit_password.setText("");
                            edit_truepassword.setText("");
                            Toast.makeText(getApplicationContext(), "添加成功",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "确认密碼不匹配，请重新输入",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "用户已存在",
                                Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                } else {
                    Toast.makeText(getApplicationContext(), "用户名不能为空",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnupdate = (Button) findViewById(R.id.btnupdate);
        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.userPower_change);
                if (edit_password.getText().toString().equals(edit_truepassword.getText().toString())) {
                    Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                            "select name from User where userName=?", new String[]{edit_user.getText().toString()});
                    if (cursor.moveToFirst()) {
                        if (!cursor.getString(cursor.getColumnIndex("name")).equals(spinner_power.getSelectedItem().toString())) {
                            dbHelper.getReadableDatabase().execSQL(
                                    "update User set userPower=?,registerTime=?,name=?,password=? where userName=?", new Object[]{
                                            spinner_power.getSelectedItemPosition(),
                                            Myutils.formatDateTime(System.currentTimeMillis()),
                                            spinner_power.getSelectedItem().toString(),
                                            edit_password.getText().toString(),
                                            edit_user.getText().toString()
                                    });
                            Toast.makeText(getApplicationContext(), "更新成功",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "用户权限设置冲突，请重新设置权限",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "用户不存在", Toast.LENGTH_SHORT).show();
                    }
                    cursor.close();
                } else {
                    Toast.makeText(getApplicationContext(), "确认密碼不匹配，请重新输入",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public List<String> getDataSource() {
        spinnerList = new ArrayList<>();
        spinnerList.add("管理员");
        spinnerList.add("操作员");
        spinnerList.add("维护员");
        return spinnerList;
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

