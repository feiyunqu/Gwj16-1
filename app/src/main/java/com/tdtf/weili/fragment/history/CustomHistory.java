package com.tdtf.weili.fragment.history;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.Utils.Netprinter;
import com.tdtf.weili.api.SerialPort;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.information.Method;
import com.tdtf.weili.service.MyService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomHistory extends Fragment {
    View rootView;
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    int n = 0;
    int cishu;
    boolean flag = true;
    String string_name, string_pihao;
    ArrayList<String> jifen = new ArrayList<>();
    ArrayList<String> weifen = new ArrayList<>();
    ArrayList<String> lijing = new ArrayList<>();
    Button btnnext;
    Button btnaverage;
    Button btndelet;
    Button btnclear;
    Button btnsave;
    Button btnprint;
    Button btnprintall;
    Button btnback;
    EditText edit_name, edit_pihao;
    TextView text_name, text_pihao;
    TextView[] ttv;
    TextView[] tv;
    TextView[] wtv;
    TextView textPress;
    TextView textTimes;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    FileOutputStream mOutputStream;

    String print_time;
    SerialPort sp;
    FileOutputStream pOutputStream;
    Netprinter netprinter = new Netprinter();
    SharedPreferences option;
    TextView texttiji;
    TextView textunit;

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
                        textPress = (TextView) getActivity().findViewById(R.id.text_press_4);
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure / 10));
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_custom_search, container, false);
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
        Intent bindIntent = new Intent(getActivity(), MyService.class);
        getActivity().bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        dbHelper = new MyDatabaseHelper(getActivity());
        myDiary.diary(dbHelper, Diary.dataView_start);
        string_name = getActivity().getIntent().getStringExtra("name");
        string_pihao = getActivity().getIntent().getStringExtra("pihao");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
        print_time = formatter.format(curDate);
        option = getActivity().getSharedPreferences("jianceshezhi", MODE_PRIVATE);
        textunit = (TextView) rootView.findViewById(R.id.text_unit);
        textunit.setText("个/" + option.getString("jishu", "") + "ml");
        texttiji = (TextView) rootView.findViewById(R.id.text_tiji);
        texttiji.setText(option.getString("quyang", "") + "ml");
        /*TODO 打开串口*/
        try {
            sp = new SerialPort(new File("/dev/ttyAMA2"), 9600, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOutputStream = (FileOutputStream) sp.getOutputStream();

        btndelet = (Button) rootView.findViewById(R.id.btndelet);
        btnclear = (Button) rootView.findViewById(R.id.btnclear);
        btnsave = (Button) rootView.findViewById(R.id.btnsave);
        edit_name = (EditText) rootView.findViewById(R.id.edit_name);
        text_name = (TextView) rootView.findViewById(R.id.text_name);
        edit_pihao = (EditText) rootView.findViewById(R.id.edit_pihao);
        text_pihao = (TextView) rootView.findViewById(R.id.text_pihao);

        btndelet.setVisibility(View.GONE);
        btnclear.setVisibility(View.GONE);
        btnsave.setVisibility(View.GONE);
        edit_name.setVisibility(View.GONE);
        text_name.setText(string_name);
        edit_pihao.setVisibility(View.GONE);
        text_pihao.setText(string_pihao);

        textTimes = (TextView) rootView.findViewById(R.id.text_times);
        // TODO: 2016/12/15  粒径
        ttv = new TextView[16];
        ttv[0] = (TextView) rootView.findViewById(R.id.textView136);
        ttv[1] = (TextView) rootView.findViewById(R.id.textView130);
        ttv[2] = (TextView) rootView.findViewById(R.id.textView124);
        ttv[3] = (TextView) rootView.findViewById(R.id.textView118);
        ttv[4] = (TextView) rootView.findViewById(R.id.textView112);
        ttv[5] = (TextView) rootView.findViewById(R.id.textView106);
        ttv[6] = (TextView) rootView.findViewById(R.id.textView100);
        ttv[7] = (TextView) rootView.findViewById(R.id.textView94);
        ttv[8] = (TextView) rootView.findViewById(R.id.textView133);
        ttv[9] = (TextView) rootView.findViewById(R.id.textView127);
        ttv[10] = (TextView) rootView.findViewById(R.id.textView121);
        ttv[11] = (TextView) rootView.findViewById(R.id.textView115);
        ttv[12] = (TextView) rootView.findViewById(R.id.textView109);
        ttv[13] = (TextView) rootView.findViewById(R.id.textView103);
        ttv[14] = (TextView) rootView.findViewById(R.id.textView97);
        ttv[15] = (TextView) rootView.findViewById(R.id.textView91);
        //TODO: 2016/12/14 积分
        tv = new TextView[16];
        tv[0] = (TextView) rootView.findViewById(R.id.textView134);
        tv[1] = (TextView) rootView.findViewById(R.id.textView128);
        tv[2] = (TextView) rootView.findViewById(R.id.textView122);
        tv[3] = (TextView) rootView.findViewById(R.id.textView116);
        tv[4] = (TextView) rootView.findViewById(R.id.textView110);
        tv[5] = (TextView) rootView.findViewById(R.id.textView104);
        tv[6] = (TextView) rootView.findViewById(R.id.textView98);
        tv[7] = (TextView) rootView.findViewById(R.id.textView92);
        tv[8] = (TextView) rootView.findViewById(R.id.textView131);
        tv[9] = (TextView) rootView.findViewById(R.id.textView125);
        tv[10] = (TextView) rootView.findViewById(R.id.textView119);
        tv[11] = (TextView) rootView.findViewById(R.id.textView113);
        tv[12] = (TextView) rootView.findViewById(R.id.textView107);
        tv[13] = (TextView) rootView.findViewById(R.id.textView101);
        tv[14] = (TextView) rootView.findViewById(R.id.textView95);
        tv[15] = (TextView) rootView.findViewById(R.id.textView89);
        // TODO: 2016/12/14 微分
        wtv = new TextView[16];
        wtv[0] = (TextView) rootView.findViewById(R.id.textView135);
        wtv[1] = (TextView) rootView.findViewById(R.id.textView129);
        wtv[2] = (TextView) rootView.findViewById(R.id.textView123);
        wtv[3] = (TextView) rootView.findViewById(R.id.textView117);
        wtv[4] = (TextView) rootView.findViewById(R.id.textView111);
        wtv[5] = (TextView) rootView.findViewById(R.id.textView105);
        wtv[6] = (TextView) rootView.findViewById(R.id.textView99);
        wtv[7] = (TextView) rootView.findViewById(R.id.textView93);
        wtv[8] = (TextView) rootView.findViewById(R.id.textView132);
        wtv[9] = (TextView) rootView.findViewById(R.id.textView126);
        wtv[10] = (TextView) rootView.findViewById(R.id.textView120);
        wtv[11] = (TextView) rootView.findViewById(R.id.textView114);
        wtv[12] = (TextView) rootView.findViewById(R.id.textView108);
        wtv[13] = (TextView) rootView.findViewById(R.id.textView102);
        wtv[14] = (TextView) rootView.findViewById(R.id.textView96);
        wtv[15] = (TextView) rootView.findViewById(R.id.textView90);

        for (int k = 0; k < 13; k++) {
            dataList(lijing, "particle", Myutils.DATA_TIMES[k], string_name);
            dataList(weifen, "differential", Myutils.DATA_TIMES[k], string_name);
            dataList(jifen, "integral", Myutils.DATA_TIMES[k], string_name);
        }
        cishu = (lijing.size() - 16) / 16;

        for (int l = 0; l < 16; l++) {
            ttv[l].setText(lijing.get(l));
            tv[l].setText(jifen.get(l));
            wtv[l].setText(weifen.get(l));
        }

        btnnext = (Button) rootView.findViewById(R.id.btnnext);
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_next);
                if (n < cishu) {
                    for (int l = 16 * n; l < 16 + 16 * n; l++) {
                        wtv[l - 16 * n].setText(weifen.get(l));
                        tv[l - 16 * n].setText(jifen.get(l));
                        ttv[l - 16 * n].setText(lijing.get(l));
                    }
                    flag = true;
                    n++;
                    textTimes.setText(String.valueOf(n));
                } else if (n == cishu) {
                    for (int l = 0; l < 16; l++) {
                        wtv[l].setText(weifen.get(weifen.size() - 16 + l));
                        tv[l].setText(jifen.get(jifen.size() - 16 + l));
                        ttv[l].setText(lijing.get(l));
                    }
                    flag = false;
                    n++;
                    textTimes.setText("均值");
                } else if (n > cishu) {
                    for (int l = 0; l < 16; l++) {
                        wtv[l].setText(weifen.get(l));
                        tv[l].setText(jifen.get(l));
                        ttv[l].setText(lijing.get(l));
                    }
                    flag = true;
                    n = 1;
                    textTimes.setText(String.valueOf(n));
                }
            }
        });
        btnaverage = (Button) rootView.findViewById(R.id.btnaverage);
        btnaverage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_average);
                for (int l = 0; l < 16; l++) {
                    wtv[l].setText(weifen.get(weifen.size() - 16 + l));
                    tv[l].setText(jifen.get(jifen.size() - 16 + l));
                    ttv[l].setText(lijing.get(l));
                }
                flag = false;
                textTimes.setText("均值");
            }
        });

        btnprint = (Button) rootView.findViewById(R.id.btnprint);
        btnprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_printOnce);
                try {
                    if (n == cishu + 1) {
                        for (int l = 15; l > -1; l--) {
                            netprinter.printindent(mOutputStream);
                            netprinter.printdatalijing(mOutputStream, lijing.get(l), 6);
                            netprinter.printdatajifen(mOutputStream, Method.mathaverage(jifen).get(l), 13);
                            netprinter.printdataweifen(mOutputStream, Method.mathaverage(weifen).get(l), 12);
                            netprinter.printline(mOutputStream);
                        }
                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "粒径");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, "积分");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, "微分");
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "平均值");
                        netprinter.printline(mOutputStream);
                    } else {
                        for (int l = 15 + 16 * (n - 1); l > 16 * (n - 1) - 1; l--) {
                            if (!TextUtils.isEmpty(lijing.get(l))) {
                                netprinter.printindent(mOutputStream);
                                netprinter.printdatalijing(mOutputStream, lijing.get(l), 6);
                                netprinter.printdatajifen(mOutputStream, jifen.get(l), 13);
                                netprinter.printdataweifen(mOutputStream, weifen.get(l), 12);
                                netprinter.printline(mOutputStream);
                            }
                        }

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "粒径");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, "积分");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, "微分");
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "第" + n + "次");
                        netprinter.printline(mOutputStream);
                        netprinter.printline(mOutputStream);
                    }

                    netprinter.printindent(mOutputStream);
                    for (int x = 0; x < 30; x++) {
                        netprinter.printtitle(mOutputStream, "-");
                    }
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "检测标准：自定义");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + print_time);
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "检测时间：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + text_pihao.getText().toString());
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "样品批号：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + text_name.getText().toString());
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "样品名称：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    for (int x = 0; x < 30; x++) {
                        netprinter.printtitle(mOutputStream, "-");
                    }
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "微粒检测仪检测结果");
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        btnprintall = (Button) rootView.findViewById(R.id.btnprintall);
        btnprintall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_printAll);
                try {
                    for (int l = 15; l > -1; l--) {
                        netprinter.printindent(mOutputStream);
                        netprinter.printdatalijing(mOutputStream, lijing.get(l), 6);
                        netprinter.printdatajifen(mOutputStream, Method.mathaverage(jifen).get(l), 13);
                        netprinter.printdataweifen(mOutputStream, Method.mathaverage(weifen).get(l), 12);
                        netprinter.printline(mOutputStream);
                    }
                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "粒径");
                    netprinter.printblank(mOutputStream);
                    netprinter.printtitle(mOutputStream, "积分");
                    netprinter.printblank(mOutputStream);
                    netprinter.printtitle(mOutputStream, "微分");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "平均值");
                    netprinter.printline(mOutputStream);
                    netprinter.printline(mOutputStream);

                    for (int x = cishu - 1; x > -1; x--) {
                        for (int l = 15 + 16 * x; l > 16 * x - 1; l--) {
                            if (!TextUtils.isEmpty(lijing.get(l))) {
                                netprinter.printindent(mOutputStream);
                                netprinter.printdatalijing(mOutputStream, lijing.get(l), 6);
                                netprinter.printdatajifen(mOutputStream, jifen.get(l), 13);
                                netprinter.printdataweifen(mOutputStream, weifen.get(l), 12);
                                netprinter.printline(mOutputStream);
                            }
                        }
                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "粒径");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, "积分");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, "微分");
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "第" + (x + 1) + "次");
                        netprinter.printline(mOutputStream);
                        netprinter.printline(mOutputStream);
                    }

                    netprinter.printindent(mOutputStream);
                    for (int x = 0; x < 30; x++) {
                        netprinter.printtitle(mOutputStream, "-");
                    }
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "检测标准：自定义");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + print_time);
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "检测时间：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + text_pihao.getText().toString());
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "样品批号：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + text_name.getText().toString());
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "样品名称：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    for (int x = 0; x < 30; x++) {
                        netprinter.printtitle(mOutputStream, "-");
                    }
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "微粒检测仪检测结果");
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

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        btnback = (Button) getActivity().findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_back);
                getActivity().unbindService(serviceConnection);
                getActivity().finish();
            }
        });
    }

    private void dataList(ArrayList<String> array_data, String string, String times, String names) {
        String sqlstr = "select particle,differential,integral from Data " +
                "where sid=(select " + times + " from Dataname where name=?)";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sqlstr, new String[]{names});
        while (cursor.moveToNext()) {
            array_data.add(cursor.getString(cursor.getColumnIndex(string)));
        }
        cursor.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //退出程序时关闭MyDatabaseHelper里的SQLiteDatabase
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
