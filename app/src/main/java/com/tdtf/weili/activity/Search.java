package com.tdtf.weili.activity;

import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Search extends AppCompatActivity {
    MyDatabaseHelper dbHelper;
    ListView partner;
    MyDiary myDiary = new MyDiary();
    ListPopupWindow popupWindow;
    ArrayList<String> biaozhun;

    String startdatepick;
    String enddatepick;

    Button btndatepickstart;
    Button btndatepickend;
    Button btnbiaozhun;
    Button btnquery;
    Button btnback;
    EditText editname;
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
                        textPress = (TextView) findViewById(R.id.text_press_10);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure / 10));
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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

        final TextClock textClock = (TextClock) findViewById(R.id.textClock10);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_12);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_12);
        textUser.setText("用户名:\n" + Myutils.getUsername());
        dbHelper = new MyDatabaseHelper(this);
        myDiary.diary(dbHelper, Diary.search_start);
        editname = (EditText) findViewById(R.id.edit_name);
        editname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                myDiary.diary(dbHelper, Diary.search_sampleName(s.toString()));
            }
        });

        partner = (ListView) findViewById(R.id.partner_list);
        partner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // TODO: 2017/2/21 点击item后进入数据显示界面
                Cursor wrapper = (Cursor) partner.getItemAtPosition(i);
                String string_name = wrapper.getString(wrapper.getColumnIndex("name"));
                String string_pihao = wrapper.getString(wrapper.getColumnIndex("pihao"));
                myDiary.diary(dbHelper, Diary.search_sampleSelect(string_name));
                Intent intent = new Intent(Search.this, DataView.class);
                intent.putExtra("name", string_name);
                intent.putExtra("pihao", string_pihao);
                unbindService(serviceConnection);
                startActivity(intent);
            }
        });

        String sql = "select * from Dataname order by dateTime desc limit 20";
//        String sqlstartdate = "00:00:00";
//        String sqlenddate = "23:59:59";
//        String[] strings = new String[]{startdatepick + " " + sqlstartdate, enddatepick + " " + sqlenddate};
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sql, null);//cursor里必须包含主键"_id"
        inflateList(cursor);

        btndatepickstart = (Button) findViewById(R.id.btndatepickstart);
        btndatepickstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(Search.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
                        btndatepickstart.setText(year + "年" + (month + 1) + "月" + dayOfMonth + "日");
                        String mm, dd;
                        if (month < 9) {
                            mm = "0" + (month + 1);
                        } else {
                            mm = String.valueOf(month + 1);
                        }
                        if (dayOfMonth < 9) {
                            dd = "0" + dayOfMonth;
                        } else {
                            dd = String.valueOf(dayOfMonth);
                        }
                        startdatepick = year + "-" + mm + "-" + dd;
                        myDiary.diary(dbHelper, Diary.search_timeStart(startdatepick));
                    }
                }
                        , c.get(Calendar.YEAR)
                        , c.get(Calendar.MONTH)
                        , c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btndatepickend = (Button) findViewById(R.id.btndatepickend);
        btndatepickend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(Search.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
                        btndatepickend.setText(year + "年" + (month + 1) + "月" + dayOfMonth + "日");
                        String mm, dd;
                        if (month < 9) {
                            mm = "0" + (month + 1);
                        } else {
                            mm = String.valueOf(month + 1);
                        }
                        if (dayOfMonth < 9) {
                            dd = "0" + dayOfMonth;
                        } else {
                            dd = String.valueOf(dayOfMonth);
                        }
                        enddatepick = year + "-" + mm + "-" + dd;
                        myDiary.diary(dbHelper, Diary.search_timeEnd(enddatepick));
                    }
                }
                        , c.get(Calendar.YEAR)
                        , c.get(Calendar.MONTH)
                        , c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnbiaozhun = (Button) findViewById(R.id.btnbiaozhun);
        biaozhun = new ArrayList<>();
        biaozhun.add("自定义");
        biaozhun.add("麻醉药剂");
        biaozhun.add("8386滤除");
        biaozhun.add("8386-05污染");
        biaozhun.add("8386-98污染");
        biaozhun.add("中国药典");

        popupWindow = new ListPopupWindow(this);
        popupWindow.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, biaozhun) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#ff000000"));
                tv.setTextSize(24);
                return tv;
            }
        });
        popupWindow.setAnchorView(btnbiaozhun);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setModal(true);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                btnbiaozhun.setText(biaozhun.get(position));
                myDiary.diary(dbHelper, Diary.search_biaoZhun(biaozhun.get(position)));
                popupWindow.dismiss();
            }
        });
        btnbiaozhun.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.show();
            }
        });

        btnquery = (Button) findViewById(R.id.btnquery);
        btnquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.search_btn_go);
                String sql = "select * from Dataname where 1=1";
                String sqlstartdate = "00:00:00";
                String sqlenddate = "23:59:59";
                ArrayList<String> arrays = new ArrayList<>();
                String quaryCombo = "";

                if (!TextUtils.isEmpty(editname.getText())) {
                    Pattern pattern = Pattern.compile("[0-9]*");
                    Matcher isNum = pattern.matcher(editname.getText().toString());
                    if (!isNum.matches()) {
                        quaryCombo = " and name like ?";
                    } else {
                        quaryCombo = " and pihao like ?";
                    }
                    arrays.add("%" + editname.getText().toString() + "%");
                }
                if (!btnbiaozhun.getText().toString().equals("选择标准")) {
                    if (TextUtils.isEmpty(quaryCombo)) {
                        quaryCombo = " and fragment= ?";
                        arrays.add(btnbiaozhun.getText().toString());
                    } else {
                        quaryCombo = quaryCombo + " and fragment= ?";
                        arrays.add(btnbiaozhun.getText().toString());
                    }
                }
                if (!btndatepickstart.getText().toString().equals("选择起始日期")) {
                    arrays.add(startdatepick + " " + sqlstartdate);
                    if (TextUtils.isEmpty(quaryCombo)) {
                        quaryCombo = " and dateTime between ? and ?";
                    } else {
                        quaryCombo = quaryCombo + " and dateTime between ? and ?";
                    }

                    if (!btndatepickend.getText().toString().equals("选择结束日期")) {
                        arrays.add(enddatepick + " " + sqlenddate);
                    }else {
                        Toast.makeText(Search.this,"请选择结束日期",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    if (!btndatepickend.getText().toString().equals("选择结束日期")) {
                        Toast.makeText(Search.this,"请选择起始日期",Toast.LENGTH_SHORT).show();
                    }
                }

                sql = sql + quaryCombo;
                String[] strings = arrays.toArray(new String[arrays.size()]);
                Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                        sql, strings);//cursor里必须包含主键"_id"
                inflateList(cursor);
            }
        });

        btnback = (Button) findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.search_back);
                startActivity(new Intent(Search.this, DetectMenu.class));
                unbindService(serviceConnection);
                finish();
            }
        });
    }

    private void inflateList(Cursor cursor) {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.partner,
                cursor,
                new String[]{"name", "pihao", "dateTime", "fragment"},
                new int[]{R.id.item_layout, R.id.item_dateTime, R.id.item_context, R.id.item_userName},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        partner.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent bindIntent = new Intent(this, MyService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
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
