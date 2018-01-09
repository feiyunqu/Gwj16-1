package com.tdtf.weili.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import com.tdtf.weili.Utils.Power;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.Utils.Transform;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.fragment.CleanStop;
import com.tdtf.weili.service.MyService;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.text.InputType.TYPE_NULL;

public class CleanOption extends AppCompatActivity {
    SharedPreferences preferences;
    FileOutputStream mOutputStream;

    EditText inputnum;
    int num = 1;//多次清洗的次数标识
    int ciShu=1;

    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();

    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();

    CleanStop fragstop = new CleanStop();
    FragmentManager fragmentManager;
    FragmentTransaction transaction;
    Bundle bundle;

    EditText editkj;
    EditText editgj;
    Button btnonce;
    Button btnkjqx;
    Button btngjqx;
    Button btnfc;
    Button btnback;
    TextView textPress;
    TextView textNote;

    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;

    /*TODO 读取串口的数据处理*/
    private class DataReceived implements Runnable {
        @Override
        public void run() {
            if (stringBuffer.length() != 0) {
                String msg = stringBuffer.toString();
                stringBuffer.delete(0, stringBuffer.length());
                Log.d("msg", "run: "+msg);
                switch (msg) {
                    case SerialOrder.ORDER_OKPRESS://压力正常
                        // TODO: 2017/4/20 中止和继续的flagment
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        bundle = new Bundle();
                        bundle.putString("str", "管路第1次清洗进行中...\n可点击中止退出");
                        fragstop.setArguments(bundle);
                        transaction.replace(R.id.linestop, fragstop);
                        transaction.commit();
                        try {
                            for (int i = 0; i < SerialOrder.ORDER_CLEAN.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_CLEAN.substring(i, i + 2), 16));
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SerialOrder.ORDER_CLEAN://第一次清洗完后判断是否再次清洗
                        if (num > 1) {
                            try {
                                for (int i = 0; i < SerialOrder.ORDER_CLEAN.length(); i = i + 2) {
                                    mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_CLEAN.substring(i, i + 2), 16));
                                }
                                num = num - 1;
                                ciShu++;
                                textNote=(TextView)findViewById(R.id.text_t);
                                textNote.setText("管路第"+ciShu+"次清洗进行中...\n可点击中止退出");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {/*TODO 排气*/
                            try {
                                for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                                    mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case SerialOrder.ORDER_UNOKCLEAN://中止后排气
                        try {
                            for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SerialOrder.ORDER_DESCLEAN://反冲成功后删除flagment
                        btnback.setVisibility(View.VISIBLE);
                        anticontrol();
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragstop);
                        transaction.commit();
                        break;
                    case SerialOrder.ORDER_RELEASE://排气成功后删除flagment
                        btnback.setVisibility(View.VISIBLE);
                        anticontrol();
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragstop);
                        transaction.commit();
                        try {
                            for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ciShu=1;
                        break;
                    case SerialOrder.ORDER_UNOKDESCLEAN://反冲异常后删除fragment
                        btnback.setVisibility(View.VISIBLE);
                        anticontrol();
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragstop);
                        transaction.commit();
                        break;
                    case SerialOrder.ORDER_UNOKSAMPLE://取样异常后删除fragment
                        btnback.setVisibility(View.VISIBLE);
                        anticontrol();
                        fragmentManager = getFragmentManager();
                        transaction = fragmentManager.beginTransaction();
                        transaction.remove(fragstop);
                        transaction.commit();
                        break;
                    default:
                        break;
                }
//                /*TODO 下降成功后发送清洗指令*/
//                if (msg.equals(SerialOrder.ORDER_OKPRESS)) {
//                    // TODO: 2017/4/20 中止和继续的flagment
//                    fragmentManager = getFragmentManager();
//                    transaction = fragmentManager.beginTransaction();
//                    bundle = new Bundle();
//                    bundle.putString("str", "管路清洗进行中...\n可点击中止退出");
//                    fragstop.setArguments(bundle);
//                    transaction.replace(R.id.linestop, fragstop);
//                    transaction.commit();
//                    try {
//                        for (int i = 0; i < SerialOrder.ORDER_CLEAN.length(); i = i + 2) {
//                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_CLEAN.substring(i, i + 2), 16));
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                /*TODO 第一次清洗完成后判断是否再次清洗*/
//                if (msg.equals(SerialOrder.ORDER_CLEAN)) {
//                    if (num > 1) {
//                        try {
//                            for (int i = 0; i < SerialOrder.ORDER_CLEAN.length(); i = i + 2) {
//                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_CLEAN.substring(i, i + 2), 16));
//                            }
//                            num = num - 1;
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    } else {/*TODO 收回取样器*/
//                        try {
//                            for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
//                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//                //TODO 中止后排气(clean)
//                if (msg.equals(SerialOrder.ORDER_UNOKCLEAN)) {
//                    try {
//                        for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
//                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                /*TODO 反冲成功后删除fragment*/
//                if (msg.equals(SerialOrder.ORDER_DESCLEAN)) {
//                    btnback.setVisibility(View.VISIBLE);
//                    anticontrol();
//                    FragmentManager fragmentManager = getFragmentManager();
//                    FragmentTransaction transaction = fragmentManager.beginTransaction();
//                    transaction.remove(fragstop);
//                    transaction.commit();
//                }
//                // TODO: 2017/4/20 排气后删除fragment
//                if (msg.equals(SerialOrder.ORDER_RELEASE)) {
//                    btnback.setVisibility(View.VISIBLE);
//                    anticontrol();
//                    FragmentManager fragmentManager = getFragmentManager();
//                    FragmentTransaction transaction = fragmentManager.beginTransaction();
//                    transaction.remove(fragstop);
//                    transaction.commit();
//                }
//                // TODO: 2017/4/20 反冲异常后删除fragment
//                if (msg.equals(SerialOrder.ORDER_UNOKDESCLEAN)) {
//                    btnback.setVisibility(View.VISIBLE);
//                    anticontrol();
//                    FragmentManager fragmentManager = getFragmentManager();
//                    FragmentTransaction transaction = fragmentManager.beginTransaction();
//                    transaction.remove(fragstop);
//                    transaction.commit();
//
//                }
                if (msg.length() == 16) {//压力检测
                    if (msg.substring(0, 6).equals("aa0023") &&
                            msg.substring(msg.length() - 8).equals("cc33c33c")) {
                        int pressure = Integer.parseInt(msg.substring(6, 8), 16);
                        textPress = (TextView) findViewById(R.id.textpressclean);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                        if (!(pressure < 5)) {
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

//                if (msg.equals(SerialOrder.ORDER_RELEASE)) {
//                    // TODO: 2017/7/10  排气成功
//                    try {
//                        //十六进制字符串
//                        for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
//                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_option);
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
                        fragstop.setOutputStream(mOutputStream);
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
        myDiary.diary(dbHelper, Diary.clearOpt_start);
        final TextClock textClock = (TextClock) findViewById(R.id.textClock4);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_3);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_3);
        textUser.setText("用户名:\n" + Myutils.getUsername());

        preferences = getSharedPreferences("jianceshezhi", MODE_PRIVATE);

        editkj = (EditText) findViewById(R.id.editkj);
        editgj = (EditText) findViewById(R.id.editgj);
        editkj.setText(preferences.getString("kaiJi","2"));
        editgj.setText(preferences.getString("guanJi","2"));
        if (!Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[16])) {
            editkj.setInputType(TYPE_NULL);
        }
        if (!Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[17])) {
            editgj.setInputType(TYPE_NULL);
        }
        editkj.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start >= 0) {//从一输入就开始判断，
                    int min = 1, max = 10;
                    try {
                        int num = Integer.parseInt(s.toString());
                        //判断当前edittext中的数字(可能一开始Edittext中有数字)是否大于max
                        if (num > max) {
                            s = String.valueOf(max);//如果大于max，则内容为max
                            editkj.setText(s);
                        } else if (num < min) {
                            s = String.valueOf(min);//如果小于min,则内容为min
                            editkj.setText(s);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                myDiary.diary(dbHelper, Diary.clearOpt_kaiJiQingXi_input(s.toString()));
            }
        });
        editgj.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (start >= 0) {//从一输入就开始判断，
                    int min = 1, max = 10;
                    try {
                        int num = Integer.parseInt(s.toString());
                        //判断当前edittext中的数字(可能一开始Edittext中有数字)是否大于max
                        if (num > max) {
                            s = String.valueOf(max);//如果大于max，则内容为max
                            editgj.setText(s);
                        } else if (num < min) {
                            s = String.valueOf(min);//如果小于min,则内容为min
                            editgj.setText(s);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                myDiary.diary(dbHelper, Diary.clearOpt_guanJiQingXi_input(s.toString()));
            }
        });

        /*TODO 单次清洗*/
        btnonce = (Button) findViewById(R.id.btnonce);
        btnonce.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.clearOpt_once);
                btnback.setVisibility(View.INVISIBLE);
                control();
                try {
                    String down = SerialOrder.press(Transform.dec2hexTwo(preferences.getString("qywz", "")));
                    Log.d("msg", "onClick: "+preferences.getString("qywz", ""));
                    for (int i = 0; i < down.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(down.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        /*TODO 开机清洗*/
        btnkjqx = (Button) findViewById(R.id.btnkjqx);
        btnkjqx.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.clearOpt_kaiJiQingXi);
                inputnum = (EditText) findViewById(R.id.editkj);
                num = Integer.parseInt(inputnum.getText().toString());
                btnback.setVisibility(View.INVISIBLE);
                control();
                try {
                    //十六进制字符串
                    String down = SerialOrder.press(Transform.dec2hexTwo(preferences.getString("qywz", "")));
                    for (int i = 0; i < down.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(down.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        /*TODO 关机清洗*/
        btngjqx = (Button) findViewById(R.id.btngjqx);
        btngjqx.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.clearOpt_guanJiQingXi);
                inputnum = (EditText) findViewById(R.id.editgj);
                num = Integer.parseInt(inputnum.getText().toString());
                btnback.setVisibility(View.INVISIBLE);
                control();
                try {
                    //十六进制字符串
                    String down = SerialOrder.press(Transform.dec2hexTwo(preferences.getString("qywz", "")));
                    for (int i = 0; i < down.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(down.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        /*TODO 反冲   不能加压*/
        btnfc = (Button) findViewById(R.id.btnfc);
        btnfc.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.clearOpt_fanChongQingXi);
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[18])) {
                    btnback.setVisibility(View.INVISIBLE);
                    control();
                    fragmentManager = getFragmentManager();
                    transaction = fragmentManager.beginTransaction();
                    bundle = new Bundle();
                    bundle.putString("str", "正在进行反冲排堵...\n可点击中止退出");
                    fragstop.setArguments(bundle);
                    transaction.replace(R.id.linestop, fragstop);
                    transaction.commit();
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_DESCLEAN.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_DESCLEAN.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(CleanOption.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnback = (Button) findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.clearOpt_back);
                SharedPreferences.Editor editor;
                editor = preferences.edit();
                editor.putString("kaiJi", editkj.getText().toString());
                editor.putString("guanJi", editgj.getText().toString());
                editor.apply();
                startActivity(new Intent(CleanOption.this, MainMenu.class));
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
        unbindService(serviceConnection);
    }

    private void control() {
        btnonce.setClickable(false);
        btnkjqx.setClickable(false);
        btngjqx.setClickable(false);
        btnfc.setClickable(false);
    }

    private void anticontrol() {
        btnonce.setClickable(true);
        btnkjqx.setClickable(true);
        btngjqx.setClickable(true);
        btnfc.setClickable(true);
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
