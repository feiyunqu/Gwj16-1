package com.tdtf.weili.fragment.passopt;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class LvchuOpt extends Fragment {
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    SharedPreferences perferences;
    RadioGroup rg_detetive, rg_compare, rg_save;
    LinearLayout linear_compare, linear_save;
    RadioButton rBt_sample, rBt_blank;
    RadioButton rBtn_compare_0, rBtn_compare_1, rBtn_compare_2, rBtn_compare_3, rBtn_compare_4;
    RadioButton rBtn_save_0, rBtn_save_1, rBtn_save_2, rBtn_save_3, rBtn_save_4;
    TextView textCompare_0, textCompare_1, textCompare_2, textCompare_3, textCompare_4;
    TextView textSave_0, textSave_1, textSave_2, textSave_3, textSave_4;
    int[] ints;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ints = new int[15];
        View view = inflater.inflate(R.layout.fragment_lvchu, container, false);
        dbHelper = new MyDatabaseHelper(getActivity());
        rg_detetive = (RadioGroup) view.findViewById(R.id.rg_detetive);
        rg_compare = (RadioGroup) view.findViewById(R.id.rg_compare);
        rg_save = (RadioGroup) view.findViewById(R.id.rg_save);
        rBt_sample = (RadioButton) view.findViewById(R.id.radioButton_sample);
        rBt_blank = (RadioButton) view.findViewById(R.id.radioButton_filter);
        textCompare_0 = (TextView) view.findViewById(R.id.text_compare_0);
        textCompare_1 = (TextView) view.findViewById(R.id.text_compare_1);
        textCompare_2 = (TextView) view.findViewById(R.id.text_compare_2);
        textCompare_3 = (TextView) view.findViewById(R.id.text_compare_3);
        textCompare_4 = (TextView) view.findViewById(R.id.text_compare_4);
        textSave_0 = (TextView) view.findViewById(R.id.text_save_0);
        textSave_1 = (TextView) view.findViewById(R.id.text_save_1);
        textSave_2 = (TextView) view.findViewById(R.id.text_save_2);
        textSave_3 = (TextView) view.findViewById(R.id.text_save_3);
        textSave_4 = (TextView) view.findViewById(R.id.text_save_4);

        perferences = getActivity().getSharedPreferences("checkBoxState", MODE_PRIVATE);
        rg_detetive.check(perferences.getInt("radioParentLvchu", 0));
        rg_compare.check(perferences.getInt("radioCompareLvchu", 0));
        rg_save.check(perferences.getInt("radioSaveLvchu", 0));
        textCompare_0.setText(perferences.getString("lvchu_save_0", ""));
        textCompare_1.setText(perferences.getString("lvchu_save_1", ""));
        textCompare_2.setText(perferences.getString("lvchu_save_2", ""));
        textCompare_3.setText(perferences.getString("lvchu_save_3", ""));
        textCompare_4.setText(perferences.getString("lvchu_save_4", ""));
        textSave_0.setText(perferences.getString("lvchu_save_0", ""));
        textSave_1.setText(perferences.getString("lvchu_save_1", ""));
        textSave_2.setText(perferences.getString("lvchu_save_2", ""));
        textSave_3.setText(perferences.getString("lvchu_save_3", ""));
        textSave_4.setText(perferences.getString("lvchu_save_4", ""));

        linear_compare = (LinearLayout) view.findViewById(R.id.linear_compare);
        linear_save = (LinearLayout) view.findViewById(R.id.linear_save);
        rBtn_compare_0 = (RadioButton) view.findViewById(R.id.rBtn_compare_0);
        rBtn_compare_1 = (RadioButton) view.findViewById(R.id.rBtn_compare_1);
        rBtn_compare_2 = (RadioButton) view.findViewById(R.id.rBtn_compare_2);
        rBtn_compare_3 = (RadioButton) view.findViewById(R.id.rBtn_compare_3);
        rBtn_compare_4 = (RadioButton) view.findViewById(R.id.rBtn_compare_4);
        rBtn_save_0 = (RadioButton) view.findViewById(R.id.rBtn_save_0);
        rBtn_save_1 = (RadioButton) view.findViewById(R.id.rBtn_save_1);
        rBtn_save_2 = (RadioButton) view.findViewById(R.id.rBtn_save_2);
        rBtn_save_3 = (RadioButton) view.findViewById(R.id.rBtn_save_3);
        rBtn_save_4 = (RadioButton) view.findViewById(R.id.rBtn_save_4);

        if (rBt_sample.isChecked()) {
            rBtn_compare_0.setEnabled(true);
            rBtn_compare_1.setEnabled(true);
            rBtn_compare_2.setEnabled(true);
            rBtn_compare_3.setEnabled(true);
            rBtn_compare_4.setEnabled(true);

            rBtn_save_0.setEnabled(false);
            rBtn_save_1.setEnabled(false);
            rBtn_save_2.setEnabled(false);
            rBtn_save_3.setEnabled(false);
            rBtn_save_4.setEnabled(false);
        } else {
            rBtn_compare_0.setEnabled(false);
            rBtn_compare_1.setEnabled(false);
            rBtn_compare_2.setEnabled(false);
            rBtn_compare_3.setEnabled(false);
            rBtn_compare_4.setEnabled(false);

            rBtn_save_0.setEnabled(true);
            rBtn_save_1.setEnabled(true);
            rBtn_save_2.setEnabled(true);
            rBtn_save_3.setEnabled(true);
            rBtn_save_4.setEnabled(true);
        }

        rg_detetive.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ints[0] = checkedId;
                switch (checkedId) {
                    case R.id.radioButton_sample:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_jianCeFangShi("样品检测"));
                        rBtn_compare_0.setEnabled(true);
                        rBtn_compare_1.setEnabled(true);
                        rBtn_compare_2.setEnabled(true);
                        rBtn_compare_3.setEnabled(true);
                        rBtn_compare_4.setEnabled(true);

                        rBtn_save_0.setEnabled(false);
                        rBtn_save_1.setEnabled(false);
                        rBtn_save_2.setEnabled(false);
                        rBtn_save_3.setEnabled(false);
                        rBtn_save_4.setEnabled(false);
                        break;
                    case R.id.radioButton_filter:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_jianCeFangShi("滤液检测"));
                        rBtn_compare_0.setEnabled(false);
                        rBtn_compare_1.setEnabled(false);
                        rBtn_compare_2.setEnabled(false);
                        rBtn_compare_3.setEnabled(false);
                        rBtn_compare_4.setEnabled(false);

                        rBtn_save_0.setEnabled(true);
                        rBtn_save_1.setEnabled(true);
                        rBtn_save_2.setEnabled(true);
                        rBtn_save_3.setEnabled(true);
                        rBtn_save_4.setEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        });
        rg_compare.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ints[1] = checkedId;
                switch (checkedId) {
                    case R.id.rBtn_compare_0:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_yangPinJianCe("对照组一"));
                        break;
                    case R.id.rBtn_compare_1:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_yangPinJianCe("对照组二"));
                        break;
                    case R.id.rBtn_compare_2:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_yangPinJianCe("对照组三"));
                        break;
                    case R.id.rBtn_compare_3:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_yangPinJianCe("对照组四"));
                        break;
                    case R.id.rBtn_compare_4:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_yangPinJianCe("对照组五"));
                        break;
                }
            }
        });
        rg_save.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                ints[2] = checkedId;
                switch (checkedId) {
                    case R.id.rBtn_save_0:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_lvYeJianCe("滤液一"));
                        break;
                    case R.id.rBtn_save_1:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_lvYeJianCe("滤液二"));
                        break;
                    case R.id.rBtn_save_2:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_lvYeJianCe("滤液三"));
                        break;
                    case R.id.rBtn_save_3:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_lvYeJianCe("滤液四"));
                        break;
                    case R.id.rBtn_save_4:
                        myDiary.diary(dbHelper, Diary.passOpt_lvChu_lvYeJianCe("滤液五"));
                        break;
                }
            }
        });
        ints[0] = rg_detetive.getCheckedRadioButtonId();
        ints[1] = rg_compare.getCheckedRadioButtonId();
        ints[2] = rg_save.getCheckedRadioButtonId();
        ints[3] = rBt_sample.getId();
        ints[4] = rBt_blank.getId();
        ints[5] = rBtn_compare_0.getId();
        ints[6] = rBtn_compare_1.getId();
        ints[7] = rBtn_compare_2.getId();
        ints[8] = rBtn_compare_3.getId();
        ints[9] = rBtn_compare_4.getId();
        ints[10] = rBtn_save_0.getId();
        ints[11] = rBtn_save_1.getId();
        ints[12] = rBtn_save_2.getId();
        ints[13] = rBtn_save_3.getId();
        ints[14] = rBtn_save_4.getId();
        return view;
    }

    public interface CallBacks {
        void process(int[] ints);
    }

    public void state(CallBacks callBacks) {
        callBacks.process(ints);
    }
}
