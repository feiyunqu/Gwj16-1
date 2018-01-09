package com.tdtf.weili.fragment;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.Utils.SerialOrder;
import com.tdtf.weili.activity.CleanOption;
import com.tdtf.weili.api.SerialPort;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import com.tdtf.weili.service.MyService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class CleanStop extends Fragment {
    FileOutputStream mOutputStream;
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    Button btngo;
    Button btnstop;
    Button back;
    TextView textView;
    Bundle bundle;

    public void setOutputStream(FileOutputStream mOutputStream){
        this.mOutputStream = mOutputStream;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_clean_stop, container, false);
        dbHelper = new MyDatabaseHelper(getActivity());
        bundle = getArguments();//从activity传过来的Bundle  
        textView = (TextView) view.findViewById(R.id.text_t);
        textView.setText(bundle.getString("str"));

        btngo = (Button) view.findViewById(R.id.btn_go);
        btngo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.clearOpt_go);
                try {
                    btngo.setVisibility(View.GONE);
                    btnstop.setVisibility(View.VISIBLE);
                    textView.setText("正在进行反冲排堵.../n可点击中止退出");
                    for (int i = 0; i < SerialOrder.ORDER_RESUME.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_RESUME.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnstop = (Button) view.findViewById(R.id.btn_stop);
        btnstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDiary.diary(dbHelper, Diary.clearOpt_stop);
                try {
                    for (int i = 0; i < SerialOrder.ORDER_STOP.length(); i = i + 2) {
                        mOutputStream.write(Integer.parseInt(SerialOrder.ORDER_STOP.substring(i, i + 2), 16));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                textView.setText("正在中止清洗...");
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        back = (Button) getActivity().findViewById(R.id.btnback);
        back.setVisibility(View.VISIBLE);
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
