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
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class Wuran05History extends Fragment {
    View rootView;
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    int n = 1;
    int cishu;
    boolean flag = true;
    String string_name, string_pihao;

    ArrayList<String> wuran25 = new ArrayList<>();
    ArrayList<String> wuran50 = new ArrayList<>();
    ArrayList<String> wuran100 = new ArrayList<>();

    Button btnnext;
    Button btnaverage;
    Button btndelet;
    Button btnclear;
    Button btnsave;
    Button btnback;
    EditText edit_name, edit_pihao;
    TextView text_name, text_pihao;
    TextView text25;
    TextView text50;
    TextView text100;
    TextView textAll_25, textAll_50, textAll_100;
    TextView textAnswer;
    TextView textPress;
    TextView textTimes;
    TextView textBlank;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    FileOutputStream mOutputStream;

    Button btnprint;
    Button btnprintall;
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
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure/10));
                    }
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_wuran05_search, container, false);
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

        text25 = (TextView) rootView.findViewById(R.id.text_25);
        text50 = (TextView) rootView.findViewById(R.id.text_50);
        text100 = (TextView) rootView.findViewById(R.id.text_100);
        textAll_25 = (TextView) rootView.findViewById(R.id.text_25_all);
        textAll_50 = (TextView) rootView.findViewById(R.id.text_50_all);
        textAll_100 = (TextView) rootView.findViewById(R.id.text_100_all);
        textTimes = (TextView) rootView.findViewById(R.id.text_times);
        textAnswer = (TextView) rootView.findViewById(R.id.text_answer);
        textBlank = (TextView) rootView.findViewById(R.id.text_blank);

        for (int k = 0; k < 13; k++) {
            dataList(wuran25, "number25", Myutils.DATA_TIMES[k], string_name);
            dataList(wuran50, "number50", Myutils.DATA_TIMES[k], string_name);
            dataList(wuran100, "number100", Myutils.DATA_TIMES[k], string_name);
        }

        cishu = wuran25.size() - 1;
        text25.setText(wuran25.get(0));
        text50.setText(wuran50.get(0));
        text100.setText(wuran100.get(0));

        BigDecimal bD_all_25 = new BigDecimal("0");
        BigDecimal bD_all_50 = new BigDecimal("0");
        BigDecimal bD_all_100 = new BigDecimal("0");
        for (int b = 0; b < cishu; b++) {
            bD_all_25 = bD_all_25.add(new BigDecimal(wuran25.get(b)));
            bD_all_50 = bD_all_50.add(new BigDecimal(wuran50.get(b)));
            bD_all_100 = bD_all_100.add(new BigDecimal(wuran100.get(b)));
        }
        textAll_25.setText(String.valueOf(bD_all_25));
        textAll_50.setText(String.valueOf(bD_all_50));
        textAll_100.setText(String.valueOf(bD_all_100));

        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(
                "select wuran from Dataname where name=?", new String[]{string_name});
        ArrayList<String> arrayList = new ArrayList<>();
        while (cursor.moveToNext()) {
            arrayList.add(cursor.getString(cursor.getColumnIndex("wuran")));
        }
        textAnswer.setText(arrayList.get(0));
        cursor.close();

        textBlank.setText(function());

        btnnext = (Button) rootView.findViewById(R.id.btnnext);
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_next);
                if (n < cishu) {
                    text25.setText(wuran25.get(n));
                    text50.setText(wuran50.get(n));
                    text100.setText(wuran100.get(n));
                    flag = true;
                    n++;
                    textTimes.setText(String.valueOf(n));
                } else if (n == cishu) {
                    text25.setText(wuran25.get(cishu));
                    text50.setText(wuran50.get(cishu));
                    text100.setText(wuran100.get(cishu));
                    flag = false;
                    n++;
                    textTimes.setText("均值");
                } else if (n > cishu) {
                    text25.setText(wuran25.get(0));
                    text50.setText(wuran50.get(0));
                    text100.setText(wuran100.get(0));
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
                text25.setText(wuran25.get(cishu));
                text50.setText(wuran50.get(cishu));
                text100.setText(wuran100.get(cishu));
                textTimes.setText("均值");
                flag = false;
            }
        });
        btnprint = (Button) rootView.findViewById(R.id.btnprint);
        btnprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_printOnce);
                try {
                    if (n == cishu + 1) {
                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "100um:");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, Method.average(wuran100));
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "50~100um:");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, Method.average(wuran50));
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "25~50um");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, Method.average(wuran25));
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "粒径");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, "计数");
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "平均值");
                        netprinter.printline(mOutputStream);
                    } else {
                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "100um:");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, wuran100.get(n - 1));
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "50~100um:");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, wuran50.get(n - 1));
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "25~50um");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, wuran25.get(n - 1));
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "粒径");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, "计数");
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "第" + n + "次");
                        netprinter.printline(mOutputStream);
                        netprinter.printline(mOutputStream);
                    }

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + textAnswer.getText().toString());
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "污染限值");
                    netprinter.printline(mOutputStream);


                    netprinter.printindent(mOutputStream);
                    for (int x = 0; x < 30; x++) {
                        netprinter.printtitle(mOutputStream, "-");
                    }
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "检测标准：8386-05污染");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + print_time);
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "检测时间：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + edit_pihao.getText().toString());
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "样品批号：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + edit_name.getText().toString());
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
        btnprintall.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_printAll);
                try {
                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "100um:");
                    netprinter.printblank(mOutputStream);
                    netprinter.printtitle(mOutputStream, Method.average(wuran100));
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "50~100um:");
                    netprinter.printblank(mOutputStream);
                    netprinter.printtitle(mOutputStream, Method.average(wuran50));
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "25~50um");
                    netprinter.printblank(mOutputStream);
                    netprinter.printtitle(mOutputStream, Method.average(wuran25));
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "粒径");
                    netprinter.printblank(mOutputStream);
                    netprinter.printtitle(mOutputStream, "计数");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "平均值");
                    netprinter.printline(mOutputStream);
                    netprinter.printline(mOutputStream);

                    for (int m = cishu - 1; m > -1; m--) {
                        for (int l = m; l < 1 + m; l++) {
                            netprinter.printindent(mOutputStream);
                            netprinter.printtitle(mOutputStream, "100um:");
                            netprinter.printblank(mOutputStream);
                            netprinter.printtitle(mOutputStream, wuran100.get(l));
                            netprinter.printline(mOutputStream);

                            netprinter.printindent(mOutputStream);
                            netprinter.printtitle(mOutputStream, "50~100um:");
                            netprinter.printblank(mOutputStream);
                            netprinter.printtitle(mOutputStream, wuran50.get(l));
                            netprinter.printline(mOutputStream);

                            netprinter.printindent(mOutputStream);
                            netprinter.printtitle(mOutputStream, "25~50um");
                            netprinter.printblank(mOutputStream);
                            netprinter.printtitle(mOutputStream, wuran25.get(l));
                            netprinter.printline(mOutputStream);

                            netprinter.printindent(mOutputStream);
                            netprinter.printtitle(mOutputStream, "粒径");
                            netprinter.printblank(mOutputStream);
                            netprinter.printtitle(mOutputStream, "计数");
                            netprinter.printline(mOutputStream);

                            netprinter.printindent(mOutputStream);
                            netprinter.printtitle(mOutputStream, "第" + (m + 1) + "次");
                            netprinter.printline(mOutputStream);
                            netprinter.printline(mOutputStream);
                        }
                    }

                    netprinter.printindent(mOutputStream);
                    for (int x = 0; x < 30; x++) {
                        netprinter.printtitle(mOutputStream, "-");
                    }
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "检测标准：8386-05污染");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + print_time);
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "检测时间：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + edit_pihao.getText().toString());
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "样品批号：");
                    netprinter.printline(mOutputStream);

                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "" + edit_name.getText().toString());
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
        String sqlstr = "select number25,number50,number100 from DataWuran " +
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

    public String function() {
        BigDecimal big_25 = new BigDecimal(textAll_25.getText().toString());
        BigDecimal big_50 = new BigDecimal(textAll_50.getText().toString());
        BigDecimal big_100 = new BigDecimal(textAll_100.getText().toString());
        BigDecimal big_answer = big_25.multiply(new BigDecimal("0.1")).
                add(big_50.multiply(new BigDecimal("0.2"))).
                add(big_100.multiply(new BigDecimal("5")));
        return String.valueOf(big_answer);
    }
}
