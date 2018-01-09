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
import android.support.annotation.Nullable;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CustomSearch extends Fragment {
    View rootView;
    MyDatabaseHelper dbHelper;
    ArrayList<String> jifen;
    ArrayList<String> weifen;
    ArrayList<String> lijing;
    ArrayList<String> biaozhi = new ArrayList<>();
    ArrayList<String> arr_jiFen = new ArrayList<>();
    ArrayList<String> arr_weiFen = new ArrayList<>();
    SerialPort sp;
    FileOutputStream pOutputStream;
    Netprinter netprinter = new Netprinter();
    int passnum;
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
    TextView[] ttv;
    TextView[] tv;
    TextView[] wtv;
    EditText edit_name, edit_pihao;

    MyDiary myDiary = new MyDiary();
    SharedPreferences information;
    SharedPreferences perferences;
    SharedPreferences option;
    String pathless, testid, username, print_time;

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
                        textPress = (TextView) (CustomSearch.this.getActivity().findViewById(R.id.text_press_4));
                        textPress.setText("压力值:\n" + String.valueOf((float) pressure / 10));
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_custom_search, container, false);
        Log.d("rrrr", "onCreateView: F");
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
        // TODO: 2017/8/1 时间格式
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
        jifen = getActivity().getIntent().getStringArrayListExtra("jifen");
        weifen = getActivity().getIntent().getStringArrayListExtra("weifen");
        lijing = getActivity().getIntent().getStringArrayListExtra("lijing");
        passnum = getActivity().getIntent().getIntExtra("passnum", 1);

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

        texttimes = (TextView) rootView.findViewById(R.id.text_times);

        try {
            for (int l = 0; l < 16; l++) {
                wtv[l].setText(weifen.get(l));
                tv[l].setText(jifen.get(l));
                ttv[l].setText(lijing.get(l));
            }
            arr_jiFen = Method.mathaverage(jifen);
            arr_weiFen = Method.mathaverage(weifen);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ciShu_initial = jifen.size() / 16;

        // TODO: 2016/12/25 下一页
        btnnext = (Button) rootView.findViewById(R.id.btnnext);
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.dataView_next);
                cishu = jifen.size() / 16;
                if (n < cishu) {
                    for (int l = 16 * n; l < 16 + 16 * n; l++) {
                        wtv[l - 16 * n].setText(weifen.get(l));
                        tv[l - 16 * n].setText(jifen.get(l));
                        ttv[l - 16 * n].setText(lijing.get(l));
                    }
                    flag = true;
                    n++;
                    texttimes.setText(String.valueOf(n));
                } else if (n == cishu) {
                    for (int l = 0; l < 16; l++) {
                        wtv[l].setText(arr_weiFen.get(l));
                        tv[l].setText(arr_jiFen.get(l));
                        ttv[l].setText(lijing.get(l));
                    }
                    flag = false;
                    n++;
                    texttimes.setText("均值");
                } else if (n > cishu) {
                    for (int l = 0; l < 16; l++) {
                        wtv[l].setText(weifen.get(l));
                        tv[l].setText(jifen.get(l));
                        ttv[l].setText(lijing.get(l));
                    }
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
                for (int l = 0; l < 16; l++) {
                    wtv[l].setText(arr_weiFen.get(l));
                    tv[l].setText(arr_jiFen.get(l));
                    ttv[l].setText(lijing.get(l));
                }
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
                cishu = jifen.size() / 16;
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
                            .rawQuery("select _id from Data", null);
                    if (cursor.moveToLast()) {
                        id = cursor.getInt(cursor.getColumnIndex("_id")) + 1;
                    } else {
                        id = 1;
                    }
                    int k = -1;
                    for (int i = 0; i < jifen.size(); i++) {
                        if (i % 16 == 0) {
                            k++;
                        }
                        dbHelper.getReadableDatabase().execSQL(
                                "insert into Data values(null,?,?,?,?)", new String[]{
                                        lijing.get(i),
                                        weifen.get(i),
                                        jifen.get(i),
                                        biaozhi.get(k) + String.valueOf(id)});
                    }
                    for (int m = 0; m < 16; m++) {
                        dbHelper.getReadableDatabase().execSQL(
                                "insert into Data values(null,?,?,?,?)", new String[]{
                                        lijing.get(m),
                                        Method.mathaverage(weifen).get(m),
                                        Method.mathaverage(jifen).get(m),
                                        biaozhi.get(12) + String.valueOf(id)});
                    }

                    dbHelper.getReadableDatabase().execSQL(
                            "insert into Dataname values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            new String[]{edit_name.getText().toString(),
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
                                    "自定义",
                                    "0",
                                    "",
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
                cishu = jifen.size() / 16;
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
                    if (info) {
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
        btnprintall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_printAll);
                cishu = jifen.size() / 16;
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
                    }
                    if (info) {
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
        Log.d("rrrr", "onStart: F");
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
            jifen.addAll(getActivity().getIntent().getStringArrayListExtra("jifen"));
            weifen.addAll(getActivity().getIntent().getStringArrayListExtra("weifen"));
            lijing.addAll(getActivity().getIntent().getStringArrayListExtra("lijing"));
            arr_jiFen = Method.mathaverage(jifen);
            arr_weiFen = Method.mathaverage(weifen);
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
                        cishu = jifen.size() / 16;
                        if (cishu > 1) {
                            int m = 0;
                            for (int l = 16 * (n - 1); l < weifen.size(); l++) {
                                if (m < 16) {
                                    weifen.remove(l);
                                    jifen.remove(l);
                                    lijing.remove(l);
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
                        for (int l = 0; l < 16; l++) {
                            wtv[l].setText("");
                            tv[l].setText("");
                            ttv[l].setText("");
                        }
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
        Log.d("rrrr", "onDestroy: F");
        //退出程序时关闭MyDatabaseHelper里的SQLiteDatabase
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public interface CallBacks {
        void supplement(int ciShu_initial, int cishu);
    }

    public void setSupplement(CallBacks callBacks) {
        this.callbacks = callBacks;
        callBacks.supplement(ciShu_initial, cishu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("rrrr", "onActivityCreated: F");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("rrrr", "onResume: F");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("rrrr", "onPause: F");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("rrrr", "onStop: F");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("rrrr", "onDestroyView: F");
    }
}
