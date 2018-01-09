package com.tdtf.weili.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.fragment.working.Custom;
import com.tdtf.weili.fragment.working.Lvchuzero;
import com.tdtf.weili.fragment.working.Mazuiqiju;
import com.tdtf.weili.fragment.working.Wuran_five;
import com.tdtf.weili.fragment.working.Wuran_nintyeight;
import com.tdtf.weili.fragment.working.Yaodian;

public class StartWork extends AppCompatActivity {
    SharedPreferences perferences;
    SharedPreferences mmpreferences;
    TextView textquyang;
    int fff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_work);
        final TextClock textClock = (TextClock) findViewById(R.id.textClock11);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower=(TextView)findViewById(R.id.text_power_15);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser=(TextView)findViewById(R.id.text_user_15);
        textUser.setText("用户名:\n"+Myutils.getUsername());
        perferences =getSharedPreferences("checkBoxState",MODE_PRIVATE);
        if(perferences.getInt("rg",0)==perferences.getInt("rbtn0",0)){
            fff=1;
        }else if(perferences.getInt("rg",0)==perferences.getInt("rbtn1",0)){
            fff=2;
        }else if(perferences.getInt("rg",0)==perferences.getInt("rbtn2",0)){
            fff=3;
        }else if(perferences.getInt("rg",0)==perferences.getInt("rbtn3",0)){
            fff=4;
        }else if(perferences.getInt("rg",0)==perferences.getInt("rbtn4",0)){
            fff=5;
        }else if(perferences.getInt("rg",0)==perferences.getInt("rbtn5",0)){
            fff=0;
        }

        Fragment[] frags={new Custom(),new Mazuiqiju(),new Lvchuzero(),new Wuran_five(),new Wuran_nintyeight(),new Yaodian()};
        FragmentManager fragmentManager=getFragmentManager();
        FragmentTransaction transaction=fragmentManager.beginTransaction();
        transaction.replace(R.id.fragscan,frags[fff]);
        transaction.commit();

        mmpreferences =getSharedPreferences("jianceshezhi",MODE_PRIVATE);
        textquyang=(TextView)findViewById(R.id.text_quyang);
        textquyang.setText(mmpreferences.getString("quyang","")+"ml");
    }
}
