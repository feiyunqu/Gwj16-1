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
public class LvchuSearch extends Fragment {
    View rootView;
    MyDatabaseHelper dbHelper;
    ArrayList<String> lvchu20;
    ArrayList<String> biaozhi = new ArrayList<>();
    SerialPort sp;
    FileOutputStream pOutputStream;
    Netprinter netprinter = new Netprinter();
    int n = 1;
    int cishu = 0;
    int ciShu_initial = 0;
    boolean flag = true;
    boolean once, all, info;
    String strAverage20;

    Button btnnext;
    Button btnaverage;
    Button btndelet;
    Button btnclear;
    Button btnsave;
    Button btnprint;
    Button btnprintall;
    Button btnback;
    TextView texttiji;
    TextView texttimes;
    TextView textunit;
    TextView text20;
    TextView textAnswer;
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
        rootView = inflater.inflate(R.layout.fragment_lvchu_search, container, false);
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
        lvchu20 = getActivity().getIntent().getStringArrayListExtra("lvchu20");
        text20 = (TextView) rootView.findViewById(R.id.text_20);
        text20.setText(lvchu20.get(0));
        textAnswer = (TextView) rootView.findViewById(R.id.text_answer);
        texttimes = (TextView) rootView.findViewById(R.id.text_times);

        if (perferences.getInt("radioParentLvchu", 0) == perferences.getInt("radioParentLvchu_0", 0)) {
            if (perferences.getInt("radioCompareLvchu", 0) == perferences.getInt("radioCompareLvchu_0", 0)) {
                textAnswer.setText(function(perferences.getString("lvchu_save_0", "")));
            } else if (perferences.getInt("radioCompareLvchu", 0) == perferences.getInt("radioCompareLvchu_1", 0)) {
                textAnswer.setText(function(perferences.getString("lvchu_save_1", "")));
            } else if (perferences.getInt("radioCompareLvchu", 0) == perferences.getInt("radioCompareLvchu_2", 0)) {
                textAnswer.setText(function(perferences.getString("lvchu_save_2", "")));
            } else if (perferences.getInt("radioCompareLvchu", 0) == perferences.getInt("radioCompareLvchu_3", 0)) {
                textAnswer.setText(function(perferences.getString("lvchu_save_3", "")));
            } else if (perferences.getInt("radioCompareLvchu", 0) == perferences.getInt("radioCompareLvchu_4", 0)) {
                textAnswer.setText(function(perferences.getString("lvchu_save_4", "")));
            }
        } else if (perferences.getInt("radioParentLvchu", 0) == perferences.getInt("radioParentLvchu_1", 0)) {
            SharedPreferences.Editor editor;
            editor = perferences.edit();
            if (perferences.getInt("radioSaveLvchu", 0) == perferences.getInt("radioSave_0", 0)) {
                editor.putString("lvchu_save_0", text20.getText().toString());
            } else if (perferences.getInt("radioSaveLvchu", 0) == perferences.getInt("radioSaveLvchu_1", 0)) {
                editor.putString("lvchu_save_1", text20.getText().toString());
            } else if (perferences.getInt("radioSaveLvchu", 0) == perferences.getInt("radioSaveLvchu_2", 0)) {
                editor.putString("lvchu_save_2", text20.getText().toString());
            } else if (perferences.getInt("radioSaveLvchu", 0) == perferences.getInt("radioSaveLvchu_3", 0)) {
                editor.putString("lvchu_save_3", text20.getText().toString());
            } else if (perferences.getInt("radioSaveLvchu", 0) == perferences.getInt("radioSaveLvchu_4", 0)) {
                editor.putString("lvchu_save_4", text20.getText().toString());
            }
            editor.apply();
        }
        strAverage20 = Method.average(lvchu20);
        ciShu_initial = lvchu20.size();
        // TODO: 2016/12/25 下一页
        btnnext = (Button) rootView.findViewById(R.id.btnnext);
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDiary.diary(dbHelper, Diary.dataView_next);
                cishu = lvchu20.size();
                if (n < cishu) {
                    text20.setText(lvchu20.get(n));
                    flag = true;
                    n++;
                    texttimes.setText(String.valueOf(n));
                } else if (n == cishu) {
                    text20.setText(strAverage20);
                    flag = false;
                    n++;
                    texttimes.setText("均值");
                } else if (n > cishu) {
                    text20.setText(lvchu20.get(0));
                    flag = true;
                    n = 1;
                    texttimes.setText(String.valueOf(n));
                }
            }
        });
        btnaverage = (Button) rootView.findViewById(R.id.btnaverage);
        btnaverage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.dataView_average);
                text20.setText(strAverage20);
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
                            .rawQuery("select _id from DataLvchu", null);
                    if (cursor.moveToLast()) {
                        id = cursor.getInt(cursor.getColumnIndex("_id")) + 1;
                    } else {
                        id = 1;
                    }
                    int k = -1;
                    for (int i = 0; i < lvchu20.size(); i++) {
                        k++;
                        dbHelper.getReadableDatabase().execSQL(
                                "insert into DataLvchu values(null,?,?)", new String[]{
                                        lvchu20.get(i),
                                        biaozhi.get(k) + String.valueOf(id)});
                    }
                    dbHelper.getReadableDatabase().execSQL(
                            "insert into DataLvchu values(null,?,?)", new String[]{
                                    strAverage20,
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
                                    "8386滤除",
                                    "2",
                                    "",
                                    textAnswer.getText().toString(),
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
                cishu = lvchu20.size();
                try {
                    if (n == cishu + 1) {
                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "20um:");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, Method.average(lvchu20));
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
                        netprinter.printtitle(mOutputStream, "20um:");
                        netprinter.printblank(mOutputStream);
                        netprinter.printtitle(mOutputStream, lvchu20.get(n - 1));
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
                    netprinter.printtitle(mOutputStream, "滤除率:");
                    netprinter.printline(mOutputStream);

                    if (info) {
                        netprinter.printindent(mOutputStream);
                        for (int x = 0; x < 30; x++) {
                            netprinter.printtitle(mOutputStream, "-");
                        }
                        netprinter.printline(mOutputStream);

                        netprinter.printindent(mOutputStream);
                        netprinter.printtitle(mOutputStream, "检测标准：8386滤除");
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
                cishu = lvchu20.size();
                try {
                    netprinter.printindent(mOutputStream);
                    netprinter.printtitle(mOutputStream, "20um:");
                    netprinter.printblank(mOutputStream);
                    netprinter.printtitle(mOutputStream, Method.average(lvchu20));
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
                            netprinter.printtitle(mOutputStream, "20um:");
                            netprinter.printblank(mOutputStream);
                            netprinter.printtitle(mOutputStream, lvchu20.get(l));
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
                        netprinter.printtitle(mOutputStream, "检测标准：8386滤除");
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
            lvchu20.addAll(getActivity().getIntent().getStringArrayListExtra("lvchu20"));
            strAverage20 = Method.average(lvchu20);
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
                        cishu = lvchu20.size();
                        if (cishu > 1) {
                            int m = 0;
                            for (int l = n - 1; l < lvchu20.size(); l++) {
                                if (m < 1) {
                                    lvchu20.remove(l);
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
                        text20.setText("");
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

    public interface CallBacks {
        void supplement(int ciShu_initial, int cishu);
    }

    public void setSupplement(CallBacks callBacks) {
        this.callbacks = callBacks;
        callBacks.supplement(ciShu_initial, cishu);
    }

    public String function(String string) {
        BigDecimal big_20 = new BigDecimal(text20.getText().toString());
        BigDecimal big_answer = big_20.multiply(new BigDecimal("100")).
                divide(new BigDecimal(text20.getText().toString()), 2, BigDecimal.ROUND_HALF_UP);
        return String.valueOf(big_answer);
    }
}
