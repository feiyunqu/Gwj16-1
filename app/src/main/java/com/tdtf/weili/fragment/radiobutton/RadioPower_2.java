package com.tdtf.weili.fragment.radiobutton;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Myutils;
import com.tdtf.weili.database.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class RadioPower_2 extends Fragment {
    MyDatabaseHelper dbhelper;
    ExpandableListView treeview;
    List<String> jiancecaozuo = new ArrayList<>();
    List<String> jianceshezhi = new ArrayList<>();
    List<String> tongdaoshezhi = new ArrayList<>();
    List<String> qingxicaozuo = new ArrayList<>();
    List<String> tongdaobiaoding = new ArrayList<>();
    List<String> jiancecaozuo_num = new ArrayList<>();
    List<String> jianceshezhi_num = new ArrayList<>();
    List<String> tongdaoshezhi_num = new ArrayList<>();
    List<String> qingxicaozuo_num = new ArrayList<>();
    List<String> tongdaobiaoding_num = new ArrayList<>();
    Map<String, List<String>> dataset = new HashMap<>();
    Map<String, List<String>> stateset = new HashMap<>();
    String[] parentList = new String[]{"检测操作", "检测设置", "通道设置", "清洗操作", "通道标定"};

    String powerdata;
    StringBuilder strBuilder;
    int x;

    private class Adapter extends BaseExpandableListAdapter {
        //  获得某个父项的某个子项  
        @Override
        public Object getChild(int parentPos, int childPos) {
            return dataset.get(parentList[parentPos]).get(childPos);
        }

        //  获得父项的数量
        @Override
        public int getGroupCount() {
            return dataset.size();
        }

        //  获得某个父项的子项数目  
        @Override
        public int getChildrenCount(int parentPos) {
            return dataset.get(parentList[parentPos]).size();
        }

        //  获得某个父项  
        @Override
        public Object getGroup(int parentPos) {
            return dataset.get(parentList[parentPos]);
        }

        //  获得某个父项的id  
        @Override
        public long getGroupId(int parentPos) {
            return parentPos;
        }

        //  获得某个父项的某个子项的id  
        @Override
        public long getChildId(int parentPos, int childPos) {
            return childPos;
        }

        //  按函数的名字来理解应该是是否具有稳定的id，这个方法目前一直都是返回false，没有去改动过  
        @Override
        public boolean hasStableIds() {
            return false;
        }

        //  获得父项显示的view  
        @Override
        public View getGroupView(int parentPos, boolean b, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.treeparent, null);
            }
            view.setTag(R.layout.treeparent, parentPos);
            view.setTag(R.layout.treechild, -1);
            update();
            CheckedTextView checkedparent = (CheckedTextView) view.findViewById(R.id.checked_parent);
            checkedparent.setText(parentList[parentPos]);
            return view;
        }

        //  获得子项显示的view  
        @Override
        public View getChildView(final int parentPos, int childPos, boolean b, View view, ViewGroup viewGroup) {
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.treechild, null);
            }
            view.setTag(R.layout.treeparent, parentPos);
            view.setTag(R.layout.treechild, childPos);
            final CheckedTextView checkedchild = (CheckedTextView) view.findViewById(R.id.checked_child);
            checkedchild.setText(dataset.get(parentList[parentPos]).get(childPos));
            checkedchild.setChecked(stateset.get(parentList[parentPos]).get(childPos).equals("1"));
            checkedchild.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (checkedchild.getText().toString()) {
                        case "开始检测":x = 0;break;
                        case "打印设置":x = 1;break;
                        case "检测方式":x = 2;break;
                        case "取样方式":x = 3;break;
                        case "预走量":x = 4;break;
                        case "取样量":x = 5;break;
                        case "检测次数":x = 6;break;
                        case "取样位置":x = 7;break;
                        case "计数单位":x = 8;break;
                        case "麻醉器具":x = 9;break;
                        case "8386滤除":x = 10;break;
                        case "8386-05污染":x = 11;break;
                        case "8386-98污染":x = 12;break;
                        case "中国药典":x = 13;break;
                        case "自定义":x = 14;break;
                        case "自定义设置":x = 15;break;
                        case "开机清洗设置":x = 16;break;
                        case "关机清洗设置":x = 17;break;
                        case "反冲":x = 18;break;
                        case "标定操作":x = 19;break;
                        case "标尺设置":x = 20;break;
                        case "标定参数":x = 21;break;
                        case "噪声测定":x = 22;break;
                        case "用户设置":x = 23;break;
                        case "修正参数":x = 24;break;
                        default:break;
                    }
                    if (checkedchild.isChecked()) {
                        strBuilder.setCharAt(x, '0');
                        checkedchild.setChecked(false);
                    } else {
                        strBuilder.setCharAt(x, '1');
                        checkedchild.setChecked(true);
                    }
                }
            });
            return view;
        }

        //  子项是否可选中，如果需要设置子项的点击事件，需要返回true  
        @Override
        public boolean isChildSelectable(int i, int i1) {
            return false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio_power_2, container, false);
        treeview = (ExpandableListView) view.findViewById(R.id.treelist);
        dbhelper = new MyDatabaseHelper(getActivity());
        Cursor cursor = dbhelper.getReadableDatabase().rawQuery(
                "select powerData from Power where powerName=?", new String[]{"维护员"});
        if (cursor.moveToFirst()) {
            powerdata = cursor.getString(cursor.getColumnIndex("powerData"));
        } else {
            powerdata = "0000000000000000000000000";
        }
        cursor.close();
//        Log.d("ddd", powerdata);
        strBuilder = new StringBuilder(powerdata);
        initialData();
        Adapter adapter = new Adapter();
        treeview.setAdapter(adapter);

        return view;
    }

    public interface CallBacks {
        void process(String str);
    }

    public void setpower(CallBacks callBacks) {
        callBacks.process(strBuilder.toString());
    }

    private void initialData() {
        jiancecaozuo.add(Myutils.BUTTON_NAME[0]);
        jiancecaozuo.add(Myutils.BUTTON_NAME[1]);
        jianceshezhi.add(Myutils.BUTTON_NAME[2]);
        jianceshezhi.add(Myutils.BUTTON_NAME[3]);
        jianceshezhi.add(Myutils.BUTTON_NAME[4]);
        jianceshezhi.add(Myutils.BUTTON_NAME[5]);
        jianceshezhi.add(Myutils.BUTTON_NAME[6]);
        jianceshezhi.add(Myutils.BUTTON_NAME[7]);
        jianceshezhi.add(Myutils.BUTTON_NAME[8]);
        tongdaoshezhi.add(Myutils.BUTTON_NAME[9]);
        tongdaoshezhi.add(Myutils.BUTTON_NAME[10]);
        tongdaoshezhi.add(Myutils.BUTTON_NAME[11]);
        tongdaoshezhi.add(Myutils.BUTTON_NAME[12]);
        tongdaoshezhi.add(Myutils.BUTTON_NAME[13]);
        tongdaoshezhi.add(Myutils.BUTTON_NAME[14]);
        tongdaoshezhi.add(Myutils.BUTTON_NAME[15]);
        qingxicaozuo.add(Myutils.BUTTON_NAME[16]);
        qingxicaozuo.add(Myutils.BUTTON_NAME[17]);
        qingxicaozuo.add(Myutils.BUTTON_NAME[18]);
        tongdaobiaoding.add(Myutils.BUTTON_NAME[19]);
        tongdaobiaoding.add(Myutils.BUTTON_NAME[20]);
        tongdaobiaoding.add(Myutils.BUTTON_NAME[21]);
        tongdaobiaoding.add(Myutils.BUTTON_NAME[22]);
        tongdaobiaoding.add(Myutils.BUTTON_NAME[23]);
        tongdaobiaoding.add(Myutils.BUTTON_NAME[24]);
        jiancecaozuo_num.add(strBuilder.substring(0, 1));
        jiancecaozuo_num.add(strBuilder.substring(1, 2));
        jianceshezhi_num.add(strBuilder.substring(2, 3));
        jianceshezhi_num.add(strBuilder.substring(3, 4));
        jianceshezhi_num.add(strBuilder.substring(4, 5));
        jianceshezhi_num.add(strBuilder.substring(5, 6));
        jianceshezhi_num.add(strBuilder.substring(6, 7));
        jianceshezhi_num.add(strBuilder.substring(7, 8));
        jianceshezhi_num.add(strBuilder.substring(8, 9));
        tongdaoshezhi_num.add(strBuilder.substring(9, 10));
        tongdaoshezhi_num.add(strBuilder.substring(10, 11));
        tongdaoshezhi_num.add(strBuilder.substring(11, 12));
        tongdaoshezhi_num.add(strBuilder.substring(12, 13));
        tongdaoshezhi_num.add(strBuilder.substring(13, 14));
        tongdaoshezhi_num.add(strBuilder.substring(14, 15));
        tongdaoshezhi_num.add(strBuilder.substring(15, 16));
        qingxicaozuo_num.add(strBuilder.substring(16, 17));
        qingxicaozuo_num.add(strBuilder.substring(17, 18));
        qingxicaozuo_num.add(strBuilder.substring(18, 19));
        tongdaobiaoding_num.add(strBuilder.substring(19, 20));
        tongdaobiaoding_num.add(strBuilder.substring(20, 21));
        tongdaobiaoding_num.add(strBuilder.substring(21, 22));
        tongdaobiaoding_num.add(strBuilder.substring(22, 23));
        tongdaobiaoding_num.add(strBuilder.substring(23, 24));
        tongdaobiaoding_num.add(strBuilder.substring(24, 25));
        dataset.put(parentList[0], jiancecaozuo);
        dataset.put(parentList[1], jianceshezhi);
        dataset.put(parentList[2], tongdaoshezhi);
        dataset.put(parentList[3], qingxicaozuo);
        dataset.put(parentList[4], tongdaobiaoding);
        stateset.put(parentList[0], jiancecaozuo_num);
        stateset.put(parentList[1], jianceshezhi_num);
        stateset.put(parentList[2], tongdaoshezhi_num);
        stateset.put(parentList[3], qingxicaozuo_num);
        stateset.put(parentList[4], tongdaobiaoding_num);
    }

    private void update() {
        stateset.clear();
        jiancecaozuo_num.clear();
        jianceshezhi_num.clear();
        tongdaobiaoding_num.clear();
        tongdaoshezhi_num.clear();
        qingxicaozuo_num.clear();
        jiancecaozuo_num.add(strBuilder.substring(0, 1));
        jiancecaozuo_num.add(strBuilder.substring(1, 2));
        jianceshezhi_num.add(strBuilder.substring(2, 3));
        jianceshezhi_num.add(strBuilder.substring(3, 4));
        jianceshezhi_num.add(strBuilder.substring(4, 5));
        jianceshezhi_num.add(strBuilder.substring(5, 6));
        jianceshezhi_num.add(strBuilder.substring(6, 7));
        jianceshezhi_num.add(strBuilder.substring(7, 8));
        jianceshezhi_num.add(strBuilder.substring(8, 9));
        tongdaoshezhi_num.add(strBuilder.substring(9, 10));
        tongdaoshezhi_num.add(strBuilder.substring(10, 11));
        tongdaoshezhi_num.add(strBuilder.substring(11, 12));
        tongdaoshezhi_num.add(strBuilder.substring(12, 13));
        tongdaoshezhi_num.add(strBuilder.substring(13, 14));
        tongdaoshezhi_num.add(strBuilder.substring(14, 15));
        tongdaoshezhi_num.add(strBuilder.substring(15, 16));
        qingxicaozuo_num.add(strBuilder.substring(16, 17));
        qingxicaozuo_num.add(strBuilder.substring(17, 18));
        qingxicaozuo_num.add(strBuilder.substring(18, 19));
        tongdaobiaoding_num.add(strBuilder.substring(19, 20));
        tongdaobiaoding_num.add(strBuilder.substring(20, 21));
        tongdaobiaoding_num.add(strBuilder.substring(21, 22));
        tongdaobiaoding_num.add(strBuilder.substring(22, 23));
        tongdaobiaoding_num.add(strBuilder.substring(23, 24));
        tongdaobiaoding_num.add(strBuilder.substring(24, 25));
        stateset.put(parentList[0], jiancecaozuo_num);
        stateset.put(parentList[1], jianceshezhi_num);
        stateset.put(parentList[2], tongdaoshezhi_num);
        stateset.put(parentList[3], qingxicaozuo_num);
        stateset.put(parentList[4], tongdaobiaoding_num);
    }
}
