package com.tdtf.weili.information;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.tdtf.weili.database.MyDatabaseHelper;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by a on 2017/5/26.
 */

public class Initial {
    int j = 0;
    private String[] jianceshezhi = {"yuzou", "quyang", "jccs", "qywz", "jishu", "jcfs", "qyfs"};
    private String[] jiance_value = {"0.5", "1.0", "5", "10", "5.0", "自动", "自动"};
    private String[] checkBoxState = {"rg", "rbtn0", "rbtn1", "rbtn2", "rbtn3", "rbtn4", "rbtn5",
            "cb1", "cb2", "cb3", "cb4", "cb5", "cb6", "cb7", "cb8", "cb9", "cb10", "cb11", "cb12", "cb13", "cb14", "cb15", "cb16",
            "cbt1", "cbt2", "cbt3", "cbt4", "cbt5", "cbt6", "cbt7", "cbt8", "cbt9", "cbt10", "cbt11", "cbt12", "cbt13", "cbt14", "cbt15", "cbt16",
            "serial", "passway", "bc1", "bc2", "bc3", "bc4", "bc5", "bc6", "bc7", "bc8", "bc9", "bc10", "bc11", "bc12", "bc13", "bc14", "bc15", "bc16"};
    private String[] check_value = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "0a4c0cd60f0d", "03",};
    private String[] check_bc = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    private int[] check_int = {2131558660, 2131558660, 2131558661, 2131558662, 2131558663, 2131558664, 2131558665};
    private boolean[] check_boolean = {false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false};
    private String[] arrayxishu = {"坐标X", "坐标Y", "系数", "坐标XX", "坐标YY", "selector", "change",
            "state_1", "state_2", "speeds", "noise", "std_lj", "std_bc"};
    private String[] array_value = {
            "1.00#1.01#1.02#1.03#1.04#1.05#2.00#5.10#8.00#10.00#12.00#15.10#21.30#25.00#51.00#100.00#",
            "56.8#57.5#58.1#58.8#59.5#60.2#124.6#334.8#477.8#558.3#638.4#764.3#1006.3#1136.3#2166.3#3832.2#",
            "-33972.167#69860.800#-45471.033#12020.167#-2606.150#2.646#-0.799#0.412#0.265#-0.015#-0.022" +
                    "#-0.030#0.088#-0.007#0.001#101916.500#-212697.389#140218.021#-37429.787#8204.321" +
                    "#-13.386#7.281#-11.236#-7.711#0.672#0.948#1.310#-6.240#0.834#-0.350#-101843.103#" +
                    "215916.925#-144056.793#38920.449#-8539.022#89.559#48.226#142.663#114.460#30.635#" +
                    "27.322#21.861#182.673#5.821#66.197#33955.570#-73023.640#49367.424#-13454.762#2997.855" +
                    "#-22.143#5.414#-155.135#-79.920#199.500#212.750#240.233#-901.522#572.313#-454.146#",
            "1.00#1.01#1.02#1.03#1.04#1.05#2.00#5.10#8.00#10.00#12.00#15.10#21.30#25.00#51.00#100.00#",
            "56.8#57.5#58.1#58.8#59.5#60.2#124.6#334.8#477.8#558.3#638.4#764.3#1006.3#1136.3#2166.3#3832.2#",
            "标尺一",
            "0.0#0.0#0.0#0.0#0.0#0.0#0.0#0.0#0.0#0.0#0.0#0.0#0.0#0.0#0.0#0.0#5000.0",
            "008#15#乳胶球",
            "006#10#乳胶球",
            "15#15#15#15",
            "10.0#10.0#10.0#10.0#10.0#10.0#10.0#10.0#10.0#10.0#10.0#10.0#10.0#10.0#10.0#10.0",
            "2.0#5.0#10.0#15.0#20.0#25.0#30.0#100.0#120.0#120.0#120.0#120.0#120.0#120.0#120.0#120.0",
            "118.0#196.0#316.0#430.0#550.0#700.0#894.0#2800.0#5000.0#5000.0#5000.0#5000.0#5000.0#5000.0#5000.0#5000.0"
    };

    public void initialization(Context context) {
        SharedPreferences preferences;
        preferences = context.getSharedPreferences("jianceshezhi", MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = preferences.edit();
        for (int i = 0; i < jianceshezhi.length; i++) {
            editor.putString(jianceshezhi[i], jiance_value[i]);
        }
        editor.apply();

        SharedPreferences preferences_1;
        preferences_1 = context.getSharedPreferences("checkBoxState", MODE_PRIVATE);
        SharedPreferences.Editor editor_1;
        editor_1 = preferences_1.edit();
        for (int i = 0; i < 7; i++) {
            editor_1.putInt(checkBoxState[j], check_int[i]);
            j++;
        }
        for (int i = 0; i < 16; i++) {
            editor_1.putBoolean(checkBoxState[j], check_boolean[i]);
            j++;
        }
        for (int i = 0; i < 18; i++) {
            editor_1.putString(checkBoxState[j], check_value[i]);
            j++;
        }
        for (int i = 0; i < 16; i++) {
            editor_1.putString(checkBoxState[j], check_bc[i]);
            j++;
        }

        editor_1.apply();

        SharedPreferences preferences_2;
        preferences_2 = context.getSharedPreferences("arrayxishu", MODE_PRIVATE);
        SharedPreferences.Editor editor_2;
        editor_2 = preferences_2.edit();
        for (int i = 0; i < arrayxishu.length; i++) {
            editor_2.putString(arrayxishu[i], array_value[i]);
        }
        editor_2.apply();
    }

    public void datainitial(MyDatabaseHelper myDatabaseHelper) {
        myDatabaseHelper.getReadableDatabase().execSQL(
                "insert into Power values(null,?,?)",
                new String[]{"管理员", "1111111111111111111111111"});
        myDatabaseHelper.getReadableDatabase().execSQL(
                "insert into Power values(null,?,?)",
                new String[]{"操作员", "0000000000000000000000000"});
        myDatabaseHelper.getReadableDatabase().execSQL(
                "insert into Power values(null,?,?)",
                new String[]{"维护员", "0000000000000000000000000"});
    }

//    public void updatainitial(MyDatabaseHelper myDatabaseHelper) {
//        myDatabaseHelper.getReadableDatabase().execSQL(
//                "update Power set powerData = ? where powerName = ?",
//                new String[]{"1111111111111111111111111", "管理员"});
//        myDatabaseHelper.getReadableDatabase().execSQL(
//                "update Power set powerData = ? where powerName = ?",
//                new String[]{"0000000000000000000000000", "操作员"});
//        myDatabaseHelper.getReadableDatabase().execSQL(
//                "update Power set powerData = ? where powerName = ?",
//                new String[]{"0000000000000000000000000", "维护员"});
//    }
}