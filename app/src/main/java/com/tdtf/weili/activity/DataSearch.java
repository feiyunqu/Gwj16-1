package com.tdtf.weili.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.fragment.result.CustomSearch;
import com.tdtf.weili.fragment.result.LvchuSearch;
import com.tdtf.weili.fragment.result.MazuiSearch;
import com.tdtf.weili.fragment.result.Wuran05Search;
import com.tdtf.weili.fragment.result.Wuran98Search;
import com.tdtf.weili.fragment.result.YaodianSearch;
import com.tdtf.weili.fragment.working.Yaodian;

public class DataSearch extends AppCompatActivity {
    public static DataSearch data_instance = null;
    public static int numberic = 0;
    SharedPreferences perferences;
    int fff;
    CustomSearch customSearch;
    MazuiSearch mazuiSearch;
    LvchuSearch lvchuSearch;
    Wuran05Search wuran05Search;
    Wuran98Search wuran98Search;
    YaodianSearch yaodianSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_search);
        Log.d("rrrr", "onCreate: A");
        data_instance = this;
        final TextClock textClock = (TextClock) findViewById(R.id.textClock5);
        textClock.setFormat24Hour("yyyy-MM-dd\nHH:mm:ss");
        final TextView textPower = (TextView) findViewById(R.id.text_power_5);
        textPower.setText("权限:\n" + Myutils.getPowername());
        final TextView textUser = (TextView) findViewById(R.id.text_user_5);
        textUser.setText("用户名:\n" + Myutils.getUsername());

        perferences = getSharedPreferences("checkBoxState", MODE_PRIVATE);
        if (perferences.getInt("rg", 0) == perferences.getInt("rbtn0", 0)) {
            fff = 1;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn1", 0)) {
            fff = 2;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn2", 0)) {
            fff = 3;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn3", 0)) {
            fff = 4;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn4", 0)) {
            fff = 5;
        } else if (perferences.getInt("rg", 0) == perferences.getInt("rbtn5", 0)) {
            fff = 0;
        }

        customSearch = new CustomSearch();
        mazuiSearch = new MazuiSearch();
        lvchuSearch = new LvchuSearch();
        wuran05Search = new Wuran05Search();
        wuran98Search = new Wuran98Search();
        yaodianSearch = new YaodianSearch();

        Fragment[] frags = {customSearch, mazuiSearch, lvchuSearch, wuran05Search, wuran98Search, yaodianSearch};
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragsearch, frags[fff]);
        transaction.commit();

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("rrrr", "onStart: A");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("rrrr", "onRestart: A");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("rrrr", "onResume: A");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("rrrr", "onPause: A");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent!=null){
            setIntent(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("rrrr", "onStop: A");
        if (customSearch != null) {
            customSearch.setSupplement(new CustomSearch.CallBacks() {
                @Override
                public void supplement(int ciShu_initial, int cishu) {
                    if (cishu > 0 && cishu < ciShu_initial) {
                        numberic = ciShu_initial - cishu;
                    } else {
                        numberic = 0;
                    }
                }
            });
        }
        if (mazuiSearch != null) {
            mazuiSearch.setSupplement(new MazuiSearch.CallBacks() {
                @Override
                public void supplement(int ciShu_initial, int cishu) {
                    if (cishu > 0 && cishu < ciShu_initial) {
                        numberic = ciShu_initial - cishu;
                    } else {
                        numberic = 0;
                    }
                }
            });
        }
        if (lvchuSearch != null) {
            lvchuSearch.setSupplement(new LvchuSearch.CallBacks() {
                @Override
                public void supplement(int ciShu_initial, int cishu) {
                    if (cishu > 0 && cishu < ciShu_initial) {
                        numberic = ciShu_initial - cishu;
                    } else {
                        numberic = 0;
                    }
                }
            });
        }
        if (wuran05Search != null) {
            wuran05Search.setSupplement(new Wuran05Search.CallBacks() {
                @Override
                public void supplement(int ciShu_initial, int cishu) {
                    if (cishu > 0 && cishu < ciShu_initial) {
                        numberic = ciShu_initial - cishu;
                    } else {
                        numberic = 0;
                    }
                }
            });
        }
        if (wuran98Search != null) {
            wuran98Search.setSupplement(new Wuran98Search.CallBacks() {
                @Override
                public void supplement(int ciShu_initial, int cishu) {
                    if (cishu > 0 && cishu < ciShu_initial) {
                        numberic = ciShu_initial - cishu;
                    } else {
                        numberic = 0;
                    }
                }
            });
        }
        if (yaodianSearch != null) {
            yaodianSearch.setSupplement(new YaodianSearch.CallBacks() {
                @Override
                public void supplement(int ciShu_initial, int cishu) {
                    if (cishu > 0 && cishu < ciShu_initial) {
                        numberic = ciShu_initial - cishu;
                    } else {
                        numberic = 0;
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("rrrr", "onDestroy: A");
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
