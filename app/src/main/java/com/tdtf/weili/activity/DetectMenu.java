package com.tdtf.weili.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.TextClock;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Handler;
import android.widget.Toast;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.Utils.Power;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.Utils.Transform;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.service.MyService;

public class DetectMenu extends AppCompatActivity {
    SharedPreferences perferences;
    FileOutputStream mOutputStream;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();

    String str = "";
    String serial, passway;
    String pressure;
    int press_value;
    int fff;
    boolean once, all, info;

    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();

    Button btnStart;
    Button btnhistory;
    Button btnprint;
    Button btnView;
    Button btnPressure;
    Button btnRelease;
    Button btnback;
    TextView textPress;
    TextView tv2;
    TextView tv3;
    TextView tv4;
    TextView tv5;
    TextView tv6;
    TextView tv7;
    TextView tv8;
    CheckBox printonce;
    CheckBox printall;
    CheckBox printinfo;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;

    Intent intentStart;
    boolean pressFlag = false;

    /*TODO 读取串口的数据处理*/
    private class DataReceived implements Runnable {
        @Override
        public void run() {
            String msg = stringBuffer.toString();
            if (stringBuffer.length() != 0) {
                stringBuffer.delete(0, stringBuffer.length());
                Log.d("msg", "run: " + msg);
                switch (msg) {
                    case SerialOrder.ORDER_OKPASSWAY:
                        try {
                            String order = "aa0008" + serial + "cc33c33c";
                            //"032003e809c40fa017701f4027104e20c350c350c350c350c350c350c350c350"
                            for (int i = 0; i < order.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(order.substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "aa0022cc33c33caa0007cc33c33c"://okpassway的特殊情况
                        try {
                            String order = "aa0008" + serial + "cc33c33c";
                            //"032003e809c40fa017701f4027104e20c350c350c350c350c350c350c350c350"
                            for (int i = 0; i < order.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(order.substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SerialOrder.ORDER_OKSAMPLING:
                        // TODO: 2016/12/22 发送预走量的指令
                        try {
                            String down = SerialOrder.preQuantity(Transform.float2hex(str));//设置预走量
                            for (int i = 0; i < down.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(down.substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SerialOrder.ORDER_OKTHRESHOLD:
                        if (tv3.getText().equals("手动")) {
                            intentStart.putExtra("压力", textPress.getText().toString());
                        }
                        intentStart.putExtra("hasPress", pressFlag);
                        startActivity(intentStart);
                        unbindService(serviceConnection);
                        finish();
                        break;
                    case SerialOrder.ORDER_RELEASE:
                        // TODO: 2017/7/10  排气成功
                        try {
                            for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }

//                if (msg.equals(SerialOrder.ORDER_OKPASSWAY)) {
//                    try {
//                        String order = "aa0008" + serial + "cc33c33c";
//                        //"032003e809c40fa017701f4027104e20c350c350c350c350c350c350c350c350"
//                        for (int i = 0; i < order.length(); i = i + 2) {
//                            mOutputStream.write(Integer.parseInt(order.substring(i, i + 2), 16));
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

//                if (msg.equals(SerialOrder.ORDER_OKSAMPLING)) {
//                    // TODO: 2016/12/22 发送预走量的指令
//                    try {
//                        String down = SerialOrder.preQuantity(Transform.float2hex(str));//设置预走量
//                        for (int i = 0; i < down.length(); i = i + 2) {
//                            mOutputStream.write(Integer.parseInt(down.substring(i, i + 2), 16));
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }

//                if (msg.equals(SerialOrder.ORDER_OKTHRESHOLD)) {
//                    startActivity(new Intent(DetectMenu.this, StartWork.class));
//                    unbindService(serviceConnection);
//                    finish();
//                }

//                if (msg.equals(SerialOrder.ORDER_RELEASE)) {
//                    // TODO: 2017/7/10  排气成功
//                    try {
//                        for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
//                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
                if (msg.length() == 16) {//压力检测
                    if (msg.substring(0, 6).equals("aa0023") &&
                            msg.substring(msg.length() - 8).equals("cc33c33c")) {
                        press_value = Integer.parseInt(msg.substring(6, 8), 16);
                        textPress.setText("压力值:\n" + String.valueOf((float) press_value / 10));
                        //TODO: 2016/12/22  发送取样量指令
                        try {
                            String down = SerialOrder.sampling(Transform.double2hex(tv6.getText().toString()));//设置取样位置
                            for (int i = 0; i < down.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(down.substring(i, i + 2), 16));
                            }
                            str = tv5.getText().toString();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_menu);
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
                        try {
                            for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
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
        myDiary.diary(dbHelper, Diary.detectMenu_start);
        final TextClock textClock = (TextClock) findViewById(R.id.textClock);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_6);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_6);
        textUser.setText("用户名:\n" + Myutils.getUsername());

        textPress = (TextView) findViewById(R.id.text_press);

        perferences = getSharedPreferences("jianceshezhi", MODE_PRIVATE);

        tv2 = (TextView) findViewById(R.id.textView20);
        tv3 = (TextView) findViewById(R.id.textView19);
        tv4 = (TextView) findViewById(R.id.textView18);
        tv5 = (TextView) findViewById(R.id.textView29);
        tv6 = (TextView) findViewById(R.id.textView28);
        tv7 = (TextView) findViewById(R.id.textView27);
        tv8 = (TextView) findViewById(R.id.textView26);

        pressure = perferences.getString("qywz", "");
        tv2.setText(perferences.getString("jcfs", ""));
        tv3.setText(perferences.getString("qyfs", ""));
        tv4.setText(String.valueOf((float) Integer.parseInt(pressure) / 10));
        tv5.setText(perferences.getString("yuzou", ""));
        tv6.setText(perferences.getString("quyang", ""));
        tv7.setText(perferences.getString("jccs", ""));
        tv8.setText(perferences.getString("jishu", ""));

        // TODO: 2016/12/22
        perferences = getSharedPreferences("checkBoxState", MODE_PRIVATE);
        final TextView tv = (TextView) findViewById(R.id.textView21);
        if (perferences.getInt("rg", 0) == perferences.getInt("rbtn0", 0)) {
            tv.setText(R.string.passopt2);
            fff = 1;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn1", 0)) {
            tv.setText(R.string.passopt3);
            fff = 2;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn2", 0)) {
            tv.setText(R.string.passopt4);
            fff = 3;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn3", 0)) {
            tv.setText(R.string.passopt5);
            fff = 4;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn4", 0)) {
            tv.setText(R.string.passopt6);
            fff = 5;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn5", 0)) {
            tv.setText(R.string.passopt7);
            fff = 0;
        }
        passway = perferences.getString("passway", "");
        serial = perferences.getString("serial", "");
        int l = serial.length();
        for (int i = 0; i < (64 - l) / 4; i++) {
            serial = serial + "c350";
        }

        ///////////////////////////////////////////////////////////////////////////////////////
        View root = this.getLayoutInflater().inflate(R.layout.fragment_printpop, null);
        final PopupWindow popup = new PopupWindow(root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        printonce = (CheckBox) root.findViewById(R.id.print_once);
        printall = (CheckBox) root.findViewById(R.id.print_all);
        printinfo = (CheckBox) root.findViewById(R.id.print_info);
        once = perferences.getBoolean("once", false);
        all = perferences.getBoolean("all", false);
        info = perferences.getBoolean("info", false);
        printonce.setChecked(once);
        printall.setChecked(all);
        printinfo.setChecked(info);
        printonce.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (printonce.isChecked()) {
                    myDiary.diary(dbHelper, Diary.detectMenu_printOnce);
                    printall.setChecked(!printonce.isChecked());
                }
            }
        });
        printall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (printall.isChecked()) {
                    myDiary.diary(dbHelper, Diary.detectMenu_printAll);
                    printonce.setChecked(!printall.isChecked());
                }
            }
        });
        printinfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                myDiary.diary(dbHelper, Diary.detectMenu_printInfo);
            }
        });
        ///////////////////////////////////////////////////////////////////////////////////////
        // TODO: 2016/12/8 开始检测按钮
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.detectMenu_doWork);
                intentStart = new Intent(DetectMenu.this, StartWork.class);
                if (DataSearch.data_instance != null) {
                    if (fff == DataSearch.data_instance.fff) {
                        if (DataSearch.numberic != 0) {
                            intentStart.putExtra("numberic", DataSearch.numberic);
                            startWorking();
                        } else {
                            //是否重新开始实验
                            start_warning();
                        }
                    } else {
                        //是否重新开始实验
                        start_warning();
                    }
                } else {
                    intentStart.putExtra("numberic", Integer.parseInt(tv7.getText().toString()));
                    startWorking();
                }
            }
        });
        btnhistory = (Button) findViewById(R.id.btnData);
        btnhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.detectMenu_history);
                SharedPreferences.Editor editor;
                editor = perferences.edit();
                editor.putBoolean("once", printonce.isChecked());
                editor.putBoolean("all", printall.isChecked());
                editor.putBoolean("info", printinfo.isChecked());
                editor.apply();
                startActivity(new Intent(DetectMenu.this, Search.class));
                unbindService(serviceConnection);
                finish();
            }
        });
        btnprint = (Button) findViewById(R.id.btnPrint);
        btnprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[1])) {
                    myDiary.diary(dbHelper, Diary.detectMenu_print);
                    if (popup.isShowing()) {
                        popup.dismiss();
                    } else {
                        popup.showAsDropDown(v, 0, 20);
                    }
                } else {
                    Toast.makeText(DetectMenu.this, "权限不匹配", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnView = (Button) findViewById(R.id.btnView);
        btnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.detectMenu_dataview);
                if (DataSearch.data_instance != null) {
                    Intent intent = new Intent(DetectMenu.this, DataSearch.class);
                    startActivity(intent);
                    unbindService(serviceConnection);
                    finish();
                } else {
                    Toast.makeText(DetectMenu.this, "没有样品测试数据", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnPressure = (Button) findViewById(R.id.btnPressure);
        btnPressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPressure.setClickable(false);
                try {
                    // TODO: 2017/7/10 加压
                    String cabin = SerialOrder.press(Transform.dec2hexTwo(pressure));
                    for (int i = 0; i < cabin.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(cabin.substring(i, i + 2), 16));
                    }
                    pressFlag = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btnRelease = (Button) findViewById(R.id.btnRelease);
        btnRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPressure.setClickable(true);
                try {
                    for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                    }
                    pressFlag = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // TODO: 2016/12/8 返回按钮
        btnback = (Button) findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.detectMenu_back);
                SharedPreferences.Editor editor;
                editor = perferences.edit();
                editor.putBoolean("once", printonce.isChecked());
                editor.putBoolean("all", printall.isChecked());
                editor.putBoolean("info", printinfo.isChecked());
                editor.apply();
                startActivity(new Intent(DetectMenu.this, MainMenu.class));
                unbindService(serviceConnection);
                finish();
            }
        });
        if (tv3.getText().equals("自动")) {
            pressFlag = true;
            btnRelease.setVisibility(View.INVISIBLE);
            btnPressure.setVisibility(View.INVISIBLE);
        }
    }

    public void start_warning() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this)
                .setTitle("警告")
                .setIcon(R.drawable.warning)
                .setMessage("该操作将开始新的检测\n新的检测结果会覆盖当前数据\n请选择是否开启新实验")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        intentStart.putExtra("numberic", Integer.parseInt(tv7.getText().toString()));
                        DataSearch.data_instance.finish();
                        startWorking();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder1.create().show();
    }

    public void startWorking() {
        if (Power.getFlag(Myutils.getPowerstring(dbHelper, Myutils.getPowername()), Myutils.BUTTON_NAME[0])) {
            try {
                String mpass = SerialOrder.passWay(Transform.dec2hexTwo(passway));//设置检测通道数
                for (int i = 0; i < mpass.length(); i = i + 2) {
                    mOutputStream.write(Integer.parseInt(mpass.substring(i, i + 2), 16));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(DetectMenu.this, "权限不匹配", Toast.LENGTH_SHORT).show();
        }
        SharedPreferences.Editor editor;
        editor = perferences.edit();//checkboxstate
        editor.putBoolean("once", printonce.isChecked());
        editor.putBoolean("all", printall.isChecked());
        editor.putBoolean("info", printinfo.isChecked());
        editor.apply();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
