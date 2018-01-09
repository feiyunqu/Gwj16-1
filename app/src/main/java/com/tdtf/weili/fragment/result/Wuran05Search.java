package com.tdtf.weili.fragment.result;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.Utils.Netprinter;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.activity.DataSearch;
import com.tdtf.weili.activity.DetectMenu;
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
public class Wuran05Search extends Fragment {
    View rootView;
    MyDatabaseHelper dbHelper;
    ArrayList<String> wuran25;
    ArrayList<String> wuran50;
    ArrayList<String> wuran100;
    ArrayList<String> biaozhi = new ArrayList<>();
    SerialPort sp;
    FileOutputStream pOutputStream;
    Netprinter netprinter = new Netprinter();
    int n = 1;
    int cishu = 0;
    int ciShu_initial = 0;
    boolean flag = true;
    boolean once, all, info;

    Button btnnext;
    Button btnaverage;
    Button btndelet;
    Button btnclear;
    Button btnsave;
    Button btnprint;
    Button btnprintall;
    Button btnback;
    TextView texttimes;
    TextView textunit;
    TextView texttiji;
    TextView text25;
    TextView text50;
    TextView text100;
    TextView textAll_25, textAll_50, textAll_100;
    TextView textAnswer, textBlank;
    EditText edit_name, edit_pihao;

    MyDiary myDiary = new MyDiary();
    SharedPreferences information;
    SharedPreferences perferences;
    SharedPreferences option;
    String pathless, testid, username, print_time;
    String strAverage25, strAverage50, strAverage100;

    TextView textPress;
    ServiceConnection serviceConnection;
    MyService.MyBinder myBinder;
    MyService myService;
    StringBuffer stringBuffer = new StringBuffer();
    Handler handler = new Handler();
    DataReceived dataReceived = new DataReceived();
    FileOutputStream mOutputStream;
    CallBacks callbacks;

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
        rootView = inflater.inflate(R.layout.fragment_wuran05_search, container, false);
        biaozhi.add("a");
        biaozhi.add("b");
        biaozhi.add("c");
        biaozhi.add("d");
        biaozhi.add("e");
        biaozhi.add("f");
        biaozhi.add("g");
        biaozhi.add("h");
        biaozhi.add("i");
        biaozhi.add("j");
        biaozhi.add("k");
        biaozhi.add("l");
        biaozhi.add("z");//用于数据库标志分类

        dbHelper = new MyDatabaseHelper(getActivity());
        myDiary.diary(dbHelper, Diary.dataView_start);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.CHINA);
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间       
        print_time = formatter.format(curDate);
        // TODO: 2017/8/1 加载打印配置
        perferences = getActivity().getSharedPreferences("checkBoxState", MODE_PRIVATE);
        once = perferences.getBoolean("once", false);
        all = perferences.getBoolean("all", false);
        info = perferences.getBoolean("info", false);

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

        information = getActivity().getSharedPreferences("information", MODE_PRIVATE);
        pathless = information.getString("Data Folder", "");
        testid = information.getString("TestID", "");
        username = information.getString("username", "");

        edit_name = (EditText) rootView.findViewById(R.id.edit_name);
        edit_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                myDiary.diary(dbHelper, Diary.dataView_sampleName(s.toString()));
            }
        });
        edit_pihao = (EditText) rootView.findViewById(R.id.edit_pihao);
        edit_pihao.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                myDiary.diary(dbHelper, Diary.dataView_samplePiHao(s.toString()));
            }
        });
        wuran25 = getActivity().getIntent().getStringArrayListExtra("wuran25");
        wuran50 = getActivity().getIntent().getStringArrayListExtra("wuran50");
        wuran100 = getActivity().getIntent().getStringArrayListExtra("wuran100");
        cishu = wuran25.size();
        text25 = (TextView) rootView.findViewById(R.id.text_25);
        text50 = (TextView) rootView.findViewById(R.id.text_50);
        text100 = (TextView) rootView.findViewById(R.id.text_100);
        textAll_25 = (TextView) rootView.findViewById(R.id.text_25_all);
        textAll_50 = (TextView) rootView.findViewById(R.id.text_50_all);
        textAll_100 = (TextView) rootView.findViewById(R.id.text_100_all);
        textAnswer = (TextView) rootView.findViewById(R.id.text_answer);
        textBlank = (TextView) rootView.findViewById(R.id.text_blank);
        texttimes = (TextView) rootView.findViewById(R.id.text_times);
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

        if (perferences.getInt("radioParent", 0) == perferences.getInt("radioParent_0", 0)) {
            if (perferences.getInt("radioCompare", 0) == perferences.getInt("radioCompare_0", 0)) {
                textAnswer.setText(function_wuran(perferences.getString("save_0", "")));
            } else if (perferences.getInt("radioCompare", 0) == perferences.getInt("radioCompare_1", 0)) {
                textAnswer.setText(function_wuran(perferences.getString("save_1", "")));
            } else if (perferences.getInt("radioCompare", 0) == perferences.getInt("radioCompare_2", 0)) {
                textAnswer.setText(function_wuran(perferences.getString("save_2", "")));
            } else if (perferences.getInt("radioCompare", 0) == perferences.getInt("radioCompare_3", 0)) {
                textAnswer.setText(function_wuran(perferences.getString("save_3", "")));
            } else if (perferences.getInt("radioCompare", 0) == perferences.getInt("radioCompare_4", 0)) {
                textAnswer.setText(function_wuran(perferences.getString("save_4", "")));
            }
        } else if (perferences.getInt("radioParent", 0) == perferences.getInt("radioParent_1", 0)) {
            String str_blank = function();
            textBlank.setText(str_blank);
            SharedPreferences.Editor editor;
            editor = perferences.edit();
            if (perferences.getInt("radioSave", 0) == perferences.getInt("radioSave_0", 0)) {
                editor.putString("save_0", str_blank);
            } else if (perferences.getInt("radioSave", 0) == perferences.getInt("radioSave_1", 0)) {
                editor.putString("save_1", str_blank);
            } else if (perferences.getInt("radioSave", 0) == perferences.getInt("radioSave_2", 0)) {
                editor.putString("save_2", str_blank);
            } else if (perferences.getInt("radioSave", 0) == perferences.getInt("radioSave_3", 0)) {
                editor.putString("save_3", str_blank);
            } else if (perferences.getInt("radioSave", 0) == perferences.getInt("radioSave_4", 0)) {
                editor.putString("save_4", str_blank);
            }
            editor.apply();
        }

        strAverage25 = Method.average(wuran25);
        strAverage50 = Method.average(wuran50);
        strAverage100 = Method.average(wuran100);
        ciShu_initial = wuran25.size();
        // TODO: 2016/12/25 下一页
        btnnext = (Button) rootView.findViewById(R.id.btnnext);
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.dataView_next);
                cishu = wuran25.size();
                if (n < cishu) {
                    text25.setText(wuran25.get(n));
                    text50.setText(wuran50.get(n));
                    text100.setText(wuran100.get(n));
                    flag = true;
                    n++;
                    texttimes.setText(String.valueOf(n));
                } else if (n == cishu) {
                    text25.setText(strAverage25);
                    text50.setText(strAverage50);
                    text100.setText(strAverage100);
                    flag = false;
                    n++;
                    texttimes.setText("均值");
                } else if (n > cishu) {
                    text25.setText(wuran25.get(0));
                    text50.setText(wuran50.get(0));
                    text100.setText(wuran100.get(0));
                    flag = true;
                    n = 1;
                    texttimes.setText(String.valueOf(n));
                }
            }
        });
        // TODO: 2017/6/5 均值
        btnaverage = (Button) rootView.findViewById(R.id.btnaverage);
        btnaverage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_average);
                text25.setText(strAverage25);
                text50.setText(strAverage50);
                text100.setText(strAverage100);
                texttimes.setText("均值");
                flag = false;
            }
        });
        // TODO: 2017/1/3 删除
        btndelet = (Button) rootView.findViewById(R.id.btndelet);
        btndelet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    warning();
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "均值数据不能删除",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        // TODO: 2017/1/3 清空
        btnclear = (Button) rootView.findViewById(R.id.btnclear);
        btnclear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear_warning();
            }
        });

        // TODO: 2017/1/3 存储
        btnsave = (Button) rootView.findViewById(R.id.btnsave);
        btnsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(edit_name.getText())) {
                    Toast.makeText(getActivity(), "请先输入样品名称", Toast.LENGTH_SHORT).show();
                } else {
                    myDiary.diary(dbHelper, Diary.dataView_save);
                    int id;
                    Cursor cursor = dbHelper.getReadableDatabase()
                            .rawQuery("select _id from DataMazui", null);
                    if (cursor.moveToLast()) {
                        id = cursor.getInt(cursor.getColumnIndex("_id")) + 1;
                    } else {
                        id = 1;
                    }
                    int k = -1;
                    for (int i = 0; i < wuran25.size(); i++) {
                        k++;
                        dbHelper.getReadableDatabase().execSQL(
                                "insert into DataWuran values(null,?,?,?,?)", new String[]{
                                        wuran25.get(i),
                                        wuran50.get(i),
                                        wuran100.get(i),
                                        biaozhi.get(k) + String.valueOf(id)});
                    }
                    dbHelper.getReadableDatabase().execSQL(
                            "insert into DataWuran values(null,?,?,?,?)", new String[]{
                                    Method.average(wuran25),
                                    Method.average(wuran50),
                                    Method.average(wuran100),
                                    biaozhi.get(12) + String.valueOf(id)});
                    EditText editname = (EditText) rootView.findViewById(R.id.edit_name);
                    dbHelper.getReadableDatabase().execSQL(
                            "insert into Dataname values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            new String[]{editname.getText().toString(),
                                    edit_pihao.getText().toString(),
                                    "a" + String.valueOf(id),
                                    "b" + String.valueOf(id),
                                    "c" + String.valueOf(id),
                                    "d" + String.valueOf(id),
                                    "e" + String.valueOf(id),
                                    "f" + String.valueOf(id),
                                    "g" + String.valueOf(id),
                                    "h" + String.valueOf(id),
                                    "i" + String.valueOf(id),
                                    "j" + String.valueOf(id),
                                    "k" + String.valueOf(id),
                                    "l" + String.valueOf(id),
                                    "z" + String.valueOf(id),
                                    "8386-05污染",
                                    "3",
                                    textAnswer.getText().toString(),
                                    "",
                                    Myutils.formatDateTime(System.currentTimeMillis())
                            });
                    String str = "数据存储成功";
                    Toast.makeText(getActivity().getApplicationContext(), str,
                            Toast.LENGTH_SHORT).show();
                    cursor.close();
                }
            }
        });
        btnprint = (Button) rootView.findViewById(R.id.btnprint);
        btnprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_printOnce);
                cishu = wuran25.size();
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

                    if (info) {
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
                    }

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
                cishu = wuran25.size();
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
                    if (info) {
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
                    }

                    netprinter.printline(mOutputStream);
                    netprinter.printline(mOutputStream);
                    netprinter.printline(mOutputStream);
                    mOutputStream.write(0x0d);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // TODO: 2016/12/25 返回
        btnback = (Button) getActivity().findViewById(R.id.btnback);
        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.dataView_back);
                startActivity(new Intent(getActivity(), DetectMenu.class));
                getActivity().unbindService(serviceConnection);
                //getActivity().finish();
            }
        });
        // TODO: 2017/8/1 自动打印
        if (once) {
            btnprint.performClick();
        }
        if (all) {
            btnprintall.performClick();
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
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

        if (cishu < ciShu_initial && cishu > 0) {
            wuran25.addAll(getActivity().getIntent().getStringArrayListExtra("wuran25"));
            wuran50.addAll(getActivity().getIntent().getStringArrayListExtra("wuran50"));
            wuran100.addAll(getActivity().getIntent().getStringArrayListExtra("wuran100"));
            strAverage25 = Method.average(wuran25);
            strAverage50 = Method.average(wuran50);
            strAverage100 = Method.average(wuran100);
        }
    }

    public void warning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle("警告")
                .setIcon(R.drawable.warning)
                .setMessage("该数据删除后将无法还原\n请选择是否继续删除")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDiary.diary(dbHelper, Diary.dataView_delete);
                        cishu = wuran25.size();
                        if (cishu > 1) {
                            int m = 0;
                            for (int l = n - 1; l < wuran25.size(); l++) {
                                if (m < 1) {
                                    wuran25.remove(l);
                                    wuran50.remove(l);
                                    wuran100.remove(l);
                                    l--;
                                    m++;
                                } else {
                                    break;
                                }
                            }
                            n = n - 1;
                            btnnext.performClick();
                        } else {
                            String str = "当前为最后一组数据，若要全部清除请点击清空按键";
                            Toast.makeText(getActivity().getApplicationContext(), str,
                                    Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getActivity().getApplicationContext(), "数据已成功删除",
                                Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(), "删除操作已取消",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }

    public void clear_warning() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity())
                .setTitle("警告")
                .setIcon(R.drawable.warning)
                .setMessage("该操作将清空全部数据\n并且数据无法还原\n请选择是否继续清空")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myDiary.diary(dbHelper, Diary.dataView_clear);
                        text25.setText("");
                        text50.setText("");
                        text100.setText("");
                        btnback.performClick();
                        DataSearch.data_instance=null;
                        String str = "数据已清空，自动返回";
                        Toast.makeText(getActivity().getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity().getApplicationContext(), "清空操作已取消",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        builder1.create().show();
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

    public String function_wuran(String string) {
        BigDecimal big_25 = new BigDecimal(textAll_25.getText().toString());
        BigDecimal big_50 = new BigDecimal(textAll_50.getText().toString());
        BigDecimal big_100 = new BigDecimal(textAll_100.getText().toString());
        BigDecimal big_answer = big_25.multiply(new BigDecimal("0.1")).
                add(big_50.multiply(new BigDecimal("0.2"))).
                add(big_100.multiply(new BigDecimal("5"))).
                subtract(new BigDecimal(string));
        return String.valueOf(big_answer);
    }

    public interface CallBacks {
        void supplement(int ciShu_initial, int cishu);
    }

    public void setSupplement(CallBacks callBacks) {
        this.callbacks = callBacks;
        callBacks.supplement(ciShu_initial, cishu);
    }
}
