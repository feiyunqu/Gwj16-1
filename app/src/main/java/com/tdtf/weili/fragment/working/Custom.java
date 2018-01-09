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

public class Custom extends Fragment {
    View rootView;
    FileOutputStream mOutputStream;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();

    SharedPreferences perferences;
    SharedPreferences jianceshezhi;

    ArrayList<String> lijing = new ArrayList<>();
    ArrayList<String> jifen = new ArrayList<>();
    ArrayList<String> weifen = new ArrayList<>();
    MyDatabaseHelper dbhelper;
    MyDiary myDiary = new MyDiary();

    TextView[] ttv;//粒径
    TextView[] tv;//积分
    TextView[] wtv;//微分
    TextView textPress;
    TextView textnum;
    Button btnPressure;
    Button btnRelease;
    Button btnstop;
    Button btngogo;
    BigDecimal num_jishu;
    BigDecimal num_quyang;
    int num, num_initial;
    int x = 0;
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
                Log.d("very", "run: "+msg);
                stringBuffer.delete(0, stringBuffer.length());
                if (flag_pressure){
                    if (msg.equals(SerialOrder.ORDER_OKPRESS)) {//加压
                        try {
                            for (int i = 0; i < SerialOrder.start("01").length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.start("01").substring(i, i + 2), 16));
                            }
                            Log.d("start", "run: 01");
                            flag = true;
                            flag_start = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // TODO: 2016/12/25 多次检测
                if (msg.equals(SerialOrder.ORDER_OKSTART)) {
                    if (mode.equals("自动")) {
                        if (num != num_initial) {
                            for (int m = 0; m < 16; m++) {
                                jifen.add(tv[m].getText().toString());
                                weifen.add(wtv[m].getText().toString());
                                lijing.add(ttv[m].getText().toString());
                            }
                        }
                    } else {
                        for (int m = 0; m < 16; m++) {
                            jifen.add(tv[m].getText().toString());
                            weifen.add(wtv[m].getText().toString());
                            lijing.add(ttv[m].getText().toString());
                        }
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
                                    Log.d("start", "run: 01");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }else {
                                try {
                                    for (int i = 0; i < SerialOrder.start("11").length(); i = i + 2) {
                                        mOutputStream.write(Integer.parseInt(SerialOrder.start("11").substring(i, i + 2), 16));
                                    }
                                    Log.d("start", "run: 11");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            btngogo.setVisibility(View.VISIBLE);
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
                            intent.putExtra("passnum", x);
                            intent.putStringArrayListExtra("jifen", jifen);
                            intent.putStringArrayListExtra("weifen", weifen);
                            intent.putStringArrayListExtra("lijing", lijing);
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

                //TODO 检测异常中止或加压异常后排气(startwork)
                if (msg.equals(SerialOrder.ORDER_UNOKSTART)||msg.equals(SerialOrder.ORDER_UNOKPRESS)) {
                    myDiary.diary(dbhelper, Diary.stop_normal);
                    flag_start = false;
                    timer.cancel();
                    try {
                        for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                        }

                        if (jifen.size() < 1) {
                            startActivity(new Intent(getActivity(), DetectMenu.class));
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("passnum", x);
                            intent.putStringArrayListExtra("jifen", jifen);
                            intent.putStringArrayListExtra("weifen", weifen);
                            intent.putStringArrayListExtra("lijing", lijing);
                            intent.setClass(getActivity(), DataSearch.class);
                            startActivity(intent);
                        }
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
                    Log.d("qwer", "run: " + streamdata);
                    if (streamdata.startsWith("aa0000cc33c33c") || streamdata.startsWith("aa0021cc33c33c") ||
                            streamdata.startsWith("aa000dcc33c33c") || streamdata.startsWith("aa0022cc33c33c") || streamdata.startsWith("aa0023")) {
                        streamdata = "";
                    }
                    if (streamdata.startsWith("aa0010") && streamdata.endsWith("cc33c33c")) {
                        for (int i = 0; i < passway; i++) {
                            int number = Integer.parseInt(streamdata.substring(i * 6 + 6, i * 6 + 12), 16);
                            String str = String.valueOf((float) number);
                            tv[i].setText(str);
                        }

                        for (int k = 0; k < passway; k++) {
                            if (k == passway - 1) {
                                wtv[k].setText("0.0");
                            } else {
                                int number = Integer.parseInt(streamdata.substring(k * 6 + 6, k * 6 + 12), 16)
                                        - Integer.parseInt(streamdata.substring((k + 1) * 6 + 6, (k + 1) * 6 + 12), 16);
                                BigDecimal numberic = new BigDecimal(String.valueOf(number));
                                BigDecimal answer = numberic.multiply(num_jishu).divide(num_quyang, 1, RoundingMode.HALF_UP);
                                String str = String.valueOf(answer);
                                wtv[k].setText(str);
                            }
                        }
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
        rootView = inflater.inflate(R.layout.fragment_custom, container, false);
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
                                    Log.d("start", "run: 01");
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
                                    Log.d("start", "run: 11");
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
        flag_hasPressure=getActivity().getIntent().getExtras().getBoolean("hasPress",true);

        jianceshezhi = this.getActivity().getSharedPreferences("jianceshezhi", MODE_PRIVATE);
        //num = Integer.parseInt(jianceshezhi.getString("jccs", ""));
        num_initial = getActivity().getIntent().getExtras().getInt("numberic", 1);
        num = num_initial;
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
        ttv = new TextView[16];
        ttv[0] = (TextView) rootView.findViewById(R.id.textView33);
        ttv[1] = (TextView) rootView.findViewById(R.id.textView34);
        ttv[2] = (TextView) rootView.findViewById(R.id.textView35);
        ttv[3] = (TextView) rootView.findViewById(R.id.textView36);
        ttv[4] = (TextView) rootView.findViewById(R.id.textView37);
        ttv[5] = (TextView) rootView.findViewById(R.id.textView38);
        ttv[6] = (TextView) rootView.findViewById(R.id.textView39);
        ttv[7] = (TextView) rootView.findViewById(R.id.textView40);
        ttv[8] = (TextView) rootView.findViewById(R.id.textView60);
        ttv[9] = (TextView) rootView.findViewById(R.id.textView61);
        ttv[10] = (TextView) rootView.findViewById(R.id.textView62);
        ttv[11] = (TextView) rootView.findViewById(R.id.textView63);
        ttv[12] = (TextView) rootView.findViewById(R.id.textView64);
        ttv[13] = (TextView) rootView.findViewById(R.id.textView65);
        ttv[14] = (TextView) rootView.findViewById(R.id.textView66);
        ttv[15] = (TextView) rootView.findViewById(R.id.textView67);
        //TODO: 2016/12/14  将积分数据存入ArrayList中
        tv = new TextView[16];
        tv[0] = (TextView) rootView.findViewById(R.id.textView51);
        tv[1] = (TextView) rootView.findViewById(R.id.textView52);
        tv[2] = (TextView) rootView.findViewById(R.id.textView53);
        tv[3] = (TextView) rootView.findViewById(R.id.textView54);
        tv[4] = (TextView) rootView.findViewById(R.id.textView55);
        tv[5] = (TextView) rootView.findViewById(R.id.textView56);
        tv[6] = (TextView) rootView.findViewById(R.id.textView57);
        tv[7] = (TextView) rootView.findViewById(R.id.textView58);
        tv[8] = (TextView) rootView.findViewById(R.id.textView78);
        tv[9] = (TextView) rootView.findViewById(R.id.textView79);
        tv[10] = (TextView) rootView.findViewById(R.id.textView80);
        tv[11] = (TextView) rootView.findViewById(R.id.textView81);
        tv[12] = (TextView) rootView.findViewById(R.id.textView82);
        tv[13] = (TextView) rootView.findViewById(R.id.textView83);
        tv[14] = (TextView) rootView.findViewById(R.id.textView84);
        tv[15] = (TextView) rootView.findViewById(R.id.textView85);
        // TODO: 2016/12/14 微分
        wtv = new TextView[16];
        wtv[0] = (TextView) rootView.findViewById(R.id.textView42);
        wtv[1] = (TextView) rootView.findViewById(R.id.textView43);
        wtv[2] = (TextView) rootView.findViewById(R.id.textView44);
        wtv[3] = (TextView) rootView.findViewById(R.id.textView45);
        wtv[4] = (TextView) rootView.findViewById(R.id.textView46);
        wtv[5] = (TextView) rootView.findViewById(R.id.textView47);
        wtv[6] = (TextView) rootView.findViewById(R.id.textView48);
        wtv[7] = (TextView) rootView.findViewById(R.id.textView49);
        wtv[8] = (TextView) rootView.findViewById(R.id.textView69);
        wtv[9] = (TextView) rootView.findViewById(R.id.textView70);
        wtv[10] = (TextView) rootView.findViewById(R.id.textView71);
        wtv[11] = (TextView) rootView.findViewById(R.id.textView72);
        wtv[12] = (TextView) rootView.findViewById(R.id.textView73);
        wtv[13] = (TextView) rootView.findViewById(R.id.textView74);
        wtv[14] = (TextView) rootView.findViewById(R.id.textView75);
        wtv[15] = (TextView) rootView.findViewById(R.id.textView76);
        for (int i = 1; i < 17; i++) {
            if (perferences.getBoolean("cb" + String.valueOf(i), true)) {
                ttv[x].setText(perferences.getString("cbt" + String.valueOf(i), ""));
                x++;
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
                    try {
                        flag_start = false;
                        timer.cancel();
                        for (int i = 0; i < SerialOrder.ORDER_RELEASE.length(); i = i + 2) {
                            mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RELEASE.substring(i, i + 2), 16));
                        }

                        if (jifen.size() < 1) {
                            startActivity(new Intent(getActivity(), DetectMenu.class));
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("passnum", x);
                            intent.putStringArrayListExtra("jifen", jifen);
                            intent.putStringArrayListExtra("weifen", weifen);
                            intent.putStringArrayListExtra("lijing", lijing);
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
                btngogo.setVisibility(View.INVISIBLE);
                if (samplemode.equals("手动")) {
                    btnRelease.setVisibility(View.INVISIBLE);
                    btnPressure.setVisibility(View.INVISIBLE);
                }
                flag_start = true;
                time = 0;

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
                            Log.d("start", "run: 01");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        try {
                            for (int i = 0; i < SerialOrder.start("11").length(); i = i + 2) {
                                mOutputStream.write(Integer.parseInt(SerialOrder.start("11").substring(i, i + 2), 16));
                            }
                            Log.d("start", "run: 11");
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
                    flag_hasPressure=false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
