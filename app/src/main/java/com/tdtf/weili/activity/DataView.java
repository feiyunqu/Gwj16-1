package com.tdtf.weili.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.fragment.history.CustomHistory;
import com.tdtf.weili.fragment.history.LvchuHistory;
import com.tdtf.weili.fragment.history.MazuiHistory;
import com.tdtf.weili.fragment.history.Wuran05History;
import com.tdtf.weili.fragment.history.Wuran98History;
import com.tdtf.weili.fragment.history.YaodianHistory;
import com.tdtf.weili.fragment.result.CustomSearch;

/**
 * 历史查询界面
 */
public class DataView extends AppCompatActivity {
    MyDatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_search);
        final TextClock textClock = (TextClock) findViewById(R.id.textClock5);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower=(TextView)findViewById(R.id.text_power_5);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser=(TextView)findViewById(R.id.text_user_5);
        textUser.setText("用户名:\n" + Myutils.getUsername());

        final String strname = getIntent().getStringExtra("name");
        dbHelper = new MyDatabaseHelper(this);
        String sqlstr = "select * from Dataname where name=?";
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sqlstr, new String[]{strname});
        int saber = cursor.getColumnIndex("fragcode");
        cursor.moveToFirst();
        String flyment = cursor.getString(saber);
        cursor.close();
        dbHelper.close();

        int archer = Integer.parseInt(flyment);
        Fragment[] frags = {new CustomHistory(), new MazuiHistory(), new LvchuHistory(), new Wuran05History(), new Wuran98History(), new YaodianHistory()};
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragsearch, frags[archer]);
        transaction.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //退出程序时关闭MyDatabaseHelper里的SQLiteDatabase
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                }
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
