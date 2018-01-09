package com.tdtf.weili.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextClock;
import android.widget.TextView;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.fragment.Searchpop;
import com.tdtf.weili.service.MyService;

import java.io.FileOutputStream;
import java.io.IOException;

public class Option extends AppCompatActivity {
    MyDatabaseHelper dbHelper;
    MyDiary myDiary=new MyDiary();
    TextView textPress;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    FileOutputStream mOutputStream;
    Button btnDiaryQuary;
    Button btnback;
    Searchpop searchpop;
    FragmentManager fragmentManager;
    FragmentTransaction transaction;
    LinearLayout linearLayout;
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
                        textPress = (TextView) findViewById(R.id.text_press_7);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);
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
        final TextClock textClock = (TextClock) findViewById(R.id.textClock7);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower=(TextView)findViewById(R.id.text_power_9);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser=(TextView)findViewById(R.id.text_user_9);
        textUser.setText("用户名:\n"+Myutils.getUsername());

        dbHelper=new MyDatabaseHelper(this);
        linearLayout=(LinearLayout)findViewById(R.id.fragLayout);
        myDiary.diary(dbHelper,Diary.option_start);
        Cursor cursor=dbHelper.getReadableDatabase().rawQuery("select * from Diary where dateTime like ?",
                new String[]{Myutils.formatDate(System.currentTimeMillis())+"%"});//cursor里必须包含主键"_id"
        inflateList(cursor);

        btnDiaryQuary=(Button)findViewById(R.id.btn_diary_quary);
        btnDiaryQuary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchpop==null){
                    fragmentManager=getFragmentManager();
                    transaction=fragmentManager.beginTransaction();
                    searchpop=new Searchpop();
                    transaction.replace(R.id.fragLayout,searchpop);
                    transaction.commit();
                }
                if (linearLayout.getVisibility()==View.INVISIBLE){
                    linearLayout.setVisibility(View.VISIBLE);
                }else {
                    linearLayout.setVisibility(View.INVISIBLE);
                }
            }
        });

        btnback=(Button)findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDiary.diary(dbHelper,Diary.option_back);
                startActivity(new Intent(Option.this, MainMenu.class));
                unbindService(serviceConnection);
                finish();
            }
        });
    }
    private void inflateList(Cursor cursor){
        final ListView diarylist=(ListView)findViewById(R.id.diarylist);
        SimpleCursorAdapter adapter=new SimpleCursorAdapter(
                this,
                R.layout.partner,
                cursor,
                new String[]{"_id","dateTime","context","powerName","userName"},
                new int[]{
                        R.id.item_layout,
                        R.id.item_dateTime,
                        R.id.item_context,
                        R.id.item_userName,
                        R.id.item_powerName},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        diarylist.setAdapter(adapter);
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if(dbHelper!=null){
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
