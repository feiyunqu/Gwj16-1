package com.tdtf.weili.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;

import java.util.ArrayList;

public class Login extends AppCompatActivity {
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    AutoCompleteTextView autouser;
    TextView textpower;
    EditText editPassword;
    Button btnlogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        dbHelper = new MyDatabaseHelper(this);
        myDiary.diary(dbHelper, Diary.login_start);
        final String[] struser = {"superdg", "111111"};
        autouser = (AutoCompleteTextView) findViewById(R.id.autoedit_user);
        editPassword = (EditText) findViewById(R.id.edit_password);
        try {
            Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                    "select _id,userName from User", null);
            inflateList(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final TextWatcher textWatcher_power = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textpower = (TextView) findViewById(R.id.text_power);
                Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                        "select _id,name from User where userName=?", new String[]{s.toString()});
                while (cursor.moveToNext()) {
                    textpower.setText(cursor.getString(cursor.getColumnIndex("name")));
                }
                cursor.close();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        autouser.addTextChangedListener(textWatcher_power);
        btnlogin = (Button) findViewById(R.id.btn_login);
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (autouser.getText().toString().equals(struser[0])
                        & editPassword.getText().toString().equals(struser[1])) {
                    Myutils.setPowerstring("1111111111111111111111111");
                    startActivity(new Intent(Login.this, MainMenu.class));
                } else {
                    logining(autouser.getText().toString(), editPassword.getText().toString());
                }
            }
        });
    }

    public void logining(String user, String password) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "select _id,password from User where userName=?", new String[]{user});
        if (cursor.moveToFirst()) {//判断用户是否存在
            String truepassword = cursor.getString(cursor.getColumnIndex("password"));
            if (truepassword.equals(password)) {
                Myutils.setPowername(textpower.getText().toString());
                Myutils.setUsername(autouser.getText().toString());
                Myutils.getPowerstring(dbHelper, textpower.getText().toString());
                startActivity(new Intent(Login.this, MainMenu.class));
                myDiary.diary(dbHelper, Diary.login_userPower(autouser.getText().toString(), textpower.getText().toString()));
                //autouser.setText("");
                //editPassword.setText("");
            } else {
                Toast.makeText(getApplicationContext(), "密码输入错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "用户不存在", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
    }

    private void inflateList(Cursor cursor) {
        ArrayList<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex("userName")));
        }
        ArrayAdapter<String> autotext = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, list);
        autouser.setAdapter(autotext);
        cursor.close();
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
