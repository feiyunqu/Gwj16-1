package com.tdtf.weili.fragment.working;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.Utils.Transform;
import com.tdtf.weili.activity.DataSearch;
import com.tdtf.weili.activity.DetectMenu;
import com.tdtf.weili.api.SerialPort;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.service.MyService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class Mazuiqiju extends Fragment {
    View rootView;
    FileOutputStream mOutputStream;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();

    SharedPreferences perferences;
    SharedPreferences jianceshezhi;

    ArrayList<String> mazui46 = new ArrayList<>();
    ArrayList<String> mazui05 = new ArrayList<>();
    MyDatabaseHelper dbhelper;
    MyDiary myDiary = new MyDiary();
    BigDecimal num_jishu;
    BigDecimal num_quyang;
    Button btnstop;
    Button btngogo;
    TextView textPress;
    TextView text46;
    TextView text5;
    TextView textnum;
    Button btnPressure;
    Button btnRelease;
    int num,num_initial;
    int math = 0;
    Timer timer;
    Boolean flag = false;
    boolean flag_start = false;
    boolean flag_pressure=true;//点击加压按钮时，不接受串口回馈
    boolean flag_hasPressure=true;//判断是否处于打压状态检测
    int time = 0;
    int passway;
    String mode, samplemode, press_value, jishu, quyang, streamdata = "";
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
                Log.d("cccc", "run: "+msg);
                if (flag_pressure){
                    if (msg.equals(SerialOrder.ORDER_OKPRESS)) {//加压成功
                        if (samplemode.equals("自动")) {
                            try {
                                for (int i = 0; i < SerialOrder.start("01").length(); i = i + 2) {
                                    mOutputStream.write(Integer.parseInt(SerialOrder.start("01").substring(i, i + 2), 16));
                                }
                                flag = true;
                                flag_start = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                // TODO: 2016/12/25 多次检测 
                if (msg.equals(SerialOrder.ORDER_OKSTART)) {
                    //TODO: 2016/12/14  将数据存入ArrayList中
                    if (mode.equals("自动")){
                        if (num != num_initial) {
                            mazui46.add(text46.getText().toString());
                            mazui05.add(text5.getText().toString());
                        }
                    }else {
                        mazui46.add(text46.getText().toString());
                        mazui05.add(text5.getText().toString());
                    }


                    if (num > 0) {
                        if (mode.equals("自动")) {
                            time = 0;
                            TextView textnum = (TextView) getActivity().findViewById(R.id.text_num);
                            textnum.setText(String.valueOf(++math));
                            if (flag_hasPressure){
                                try {
                                    for (int i = 0; i < SerialOrder.start("01").length(); i = i + 2) {
                                        mOutputStream.write(Integer.parseInt(SerialOrder.start("01").substring(i, i + 2), 16));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                try {
                                    for (int i = 0; i < SerialOrder.start("11").length(); i = i + 2) {
                                        mOutputStream.write(Integer.parseInt(SerialOrder.start("11").substring(i, i + 2), 16));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            if (samplemode.equals("手动")) {
                                btnRelease.setVisibility(View.VISIBLE);
                                btnPressure.setVisibility(View.VISIBLE);
                            }
                            timer.cancel();
                            try {
                                for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                                    mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            btngogo.setVisibility(View.VISIBLE);
                        }
                        num = num - 1;
                    } else {
                        try {
                            flag_start = false;
                            timer.cancel();
                            for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                            }
                            Intent intent = new Intent();
                            intent.putStringArrayListExtra("mazui46", mazui46);
                            intent.putStringArrayListExtra("mazui05", mazui05);
                            intent.setClass(getActivity(), DataSearch.class);
                            startActivity(intent);
                            myDiary.diary(dbhelper, Diary.startWork_finish);
                            getActivity().unbindService(serviceConnection);
                            getActivity().finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //TODO 检测异常中止后收回取样器(startwork)
                if (msg.equals(SerialOrder.ORDER_UNOKSTART)||msg.equals(SerialOrder.ORDER_UNOKPRESS)) {
                    flag_start = false;
                    timer.cancel();
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                        }
                        if (mazui46.size() < 1) {
                            startActivity(new Intent(getActivity(), DetectMenu.class));
                        } else {
                            Intent intent = new Intent();
                            intent.putStringArrayListExtra("mazui46", mazui46);
                            intent.putStringArrayListExtra("mazui05", mazui05);
                            intent.setClass(getActivity(), DataSearch.class);
                            startActivity(intent);
                        }
                        myDiary.diary(dbhelper, Diary.stop_normal);
                        getActivity().unbindService(serviceConnection);
                        getActivity().finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (msg.equals(SerialOrder.ORDER_RELEASE)) {
                    // TODO: 2017/7/10  排气成功
                    flag_pressure=true;
                    btnPressure.setClickable(true);
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_PRESSURE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_PRESSURE.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (flag_start) {
                    streamdata = streamdata + msg;
                    if (streamdata.startsWith("aa0000cc33c33c") || streamdata.startsWith("aa0021cc33c33c") || streamdata.startsWith("aa000dcc33c33c")) {
                        streamdata = "";
                    }
                    if (streamdata.startsWith("aa0010") && streamdata.endsWith("cc33c33c")) {
                        // TODO: 2016/12/14 积分
                        int number1 = Integer.parseInt(streamdata.substring(6 + 6, 6 + 12), 16);
                        BigDecimal numberic = new BigDecimal(String.valueOf(number1));
                        BigDecimal answer = numberic.multiply(num_jishu).divide(num_quyang, 1, RoundingMode.HALF_UP);
                        String str = String.valueOf(answer);
                        int number2 = Integer.parseInt(streamdata.substring(6, 12), 16)
                                - Integer.parseInt(streamdata.substring(2 * 6 + 6, 2 * 6 + 12), 16);
                        BigDecimal numberic2 = new BigDecimal(String.valueOf(number2));
                        BigDecimal answer2 = numberic2.multiply(num_jishu).divide(num_quyang, 1, RoundingMode.HALF_UP);
                        String str2 = String.valueOf(answer2);
                        text5.setText(String.valueOf(str));
                        text46.setText(String.valueOf(str2));
                        streamdata = "";
                    }
                }

                if (msg.length() == 16) {//压力检测
                    if (msg.substring(0, 6).equals("aa0023") &&
                            msg.substring(msg.length() - 8).equals("cc33c33c")) {
                        int pressure = Integer.parseInt(msg.substring(6, 8), 16);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure / 10));
                    }
                }
                if (msg.equals(SerialOrder.ORDER_UNOKSAMPLE)){
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_SAMPLEBACK.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_SAMPLEBACK.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(getActivity(), DetectMenu.class));
                    getActivity().unbindService(serviceConnection);
                    getActivity().finish();
                    Toast.makeText(getActivity(),"取样器异常中止",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mazuiqiju, container, false);
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
                        if (samplemode.equals("自动")) {
                            try {
                                // TODO: 2017/7/10 加压
                                String cabin = SerialOrder.press(Transform.dec2hexTwo(press_value));
                                for (int i = 0; i < cabin.length(); i = i + 2) {
                                    mOutputStream.write(Integer.parseInt(cabin.substring(i, i + 2), 16));
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (flag_hasPressure){
                                try {
                                    for (int i = 0; i < SerialOrder.start("01").length(); i = i + 2) {
                                        mOutputStream.write(Integer.parseInt(SerialOrder.start("01").substring(i, i + 2), 16));
                                    }
                                    flag = true;
                                    flag_start = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                try {
                                    for (int i = 0; i < SerialOrder.start("11").length(); i = i + 2) {
                                        mOutputStream.write(Integer.parseInt(SerialOrder.start("11").substring(i, i + 2), 16));
                                    }
                                    flag = true;
                                    flag_start = true;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        Intent bindIntent = new Intent(getActivity(), MyService.class);
        getActivity().bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        dbhelper = new MyDatabaseHelper(getActivity());
        myDiary.diary(dbhelper, Diary.startWork_start);
        myDiary.diary(dbhelper, Diary.startWork_running);

        text46 = (TextView) rootView.findViewById(R.id.text_46);
        text5 = (TextView) rootView.findViewById(R.id.text_5);
        jianceshezhi = this.getActivity().getSharedPreferences("jianceshezhi", MODE_PRIVATE);
        //num = Integer.parseInt(jianceshezhi.getString("jccs", ""));
        num_initial=getActivity().getIntent().getExtras().getInt("numberic",1);
        num=num_initial;
        mode = jianceshezhi.getString("jcfs", "");
        samplemode = jianceshezhi.getString("qyfs", "");
        press_value = jianceshezhi.getString("qywz", "");
        jishu = jianceshezhi.getString("jishu", "");
        quyang = jianceshezhi.getString("quyang", "");
        num_jishu = new BigDecimal(jishu);
        num_quyang = new BigDecimal(quyang);
        perferences = this.getActivity().getSharedPreferences("checkBoxState", MODE_PRIVATE);
        passway = Integer.parseInt(perferences.getString("passway", ""));

        textnum = (TextView) getActivity().findViewById(R.id.text_num);
        if (mode.equals("手动")) {
            num = num - 1;
            math = math + 1;
            textnum.setText(String.valueOf(math));
        }
        textPress = (TextView) getActivity().findViewById(R.id.textpress);
        if (samplemode.equals("手动")) {
            textPress.setText(getActivity().getIntent().getExtras().getString("压力", "0.0"));
            if (textPress.getText().toString().equals("压力值:\n0.0")){
                textPress.setText("压力值:\n0.0");
            }
        }
        //////////////////////////////////////////////////////////////////////////////
        final TextView textView = (TextView) getActivity().findViewById(R.id.textTime);
        final Handler handler_time = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String str = String.valueOf(msg.what) + "s";
                textView.setText(str);
            }
        };
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = time;
                handler_time.sendMessage(message);
                if (flag) {
                    time++;
                }
            }
        };
        timer = new Timer();
        timer.schedule(task, 0, 1000);

        // TODO: 2017/4/10 中止
        btnstop = (Button) getActivity().findViewById(R.id.btnstop);
        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbhelper, Diary.startWork_btn_stop);
                myDiary.diary(dbhelper, Diary.startWork_stop);
                try {
                    for (int i = 0; i < SerialOrder.ORDER_STOP.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_STOP.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (btngogo.isShown()) {
                    flag_start = false;
                    timer.cancel();
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                        }
                        if (mazui46.size() < 1) {
                            startActivity(new Intent(getActivity(), DetectMenu.class));
                        } else {
                            Intent intent = new Intent();
                            intent.putStringArrayListExtra("mazui46", mazui46);
                            intent.putStringArrayListExtra("mazui05", mazui05);
                            intent.setClass(getActivity(), DataSearch.class);
                            startActivity(intent);
                        }
                        getActivity().unbindService(serviceConnection);
                        getActivity().finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        // TODO: 2017/5/2 继续
        btngogo = (Button) getActivity().findViewById(R.id.btngogo);
        btngogo.setVisibility(View.INVISIBLE);
        btngogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbhelper, Diary.gogogo);
                flag_start = true;
                time = 0;
                btngogo.setVisibility(View.INVISIBLE);
                if (samplemode.equals("手动")) {
                    btnRelease.setVisibility(View.INVISIBLE);
                    btnPressure.setVisibility(View.INVISIBLE);
                }
                final TextView textView = (TextView) getActivity().findViewById(R.id.textTime);
                final Handler handler_time = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        String str = String.valueOf(msg.what) + "s";
                        textView.setText(str);
                    }
                };
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        Message message = new Message();
                        message.what = time;
                        handler_time.sendMessage(message);
                        if (flag) {
                            time++;
                        }
                    }
                };
                timer = new Timer();
                timer.schedule(task, 0, 1000);

                textnum.setText(String.valueOf(++math));
                if (samplemode.equals("自动")) {
                    try {
                        // TODO: 2017/7/10 加压
                        String cabin = SerialOrder.press(Transform.dec2hexTwo(press_value));
                        for (int i = 0; i < cabin.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(cabin.substring(i, i + 2), 16));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (flag_hasPressure){//手动加压情况下判断是否加压，加压true，不加压false
                        try {
                            for (int i = 0; i < SerialOrder.start("01").length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.start("01").substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            for (int i = 0; i < SerialOrder.start("11").length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.start("11").substring(i, i + 2), 16));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        btnPressure = (Button) getActivity().findViewById(R.id.btnPressure);
        btnPressure.setVisibility(View.INVISIBLE);
        btnPressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPressure.setClickable(false);
                flag_pressure=false;
                try {
                    // TODO: 2017/7/10 加压
                    String cabin = SerialOrder.press(Transform.dec2hexTwo(press_value));
                    for (int i = 0; i < cabin.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(cabin.substring(i, i + 2), 16));
                    }
                    flag_hasPressure=true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btnRelease = (Button) getActivity().findViewById(R.id.btnRelease);
        btnRelease.setVisibility(View.INVISIBLE);
        btnRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                flag_hasPressure=false;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbhelper != null) {
            dbhelper.close();
        }
    }
}
