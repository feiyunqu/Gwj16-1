package com.tdtf.weili.fragment.passopt;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.database.MyDatabaseHelper;
import com.tdtf.weili.database.MyDiary;
import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CustomOpt extends Fragment {
    MyDatabaseHelper dbHelper;
    MyDiary myDiary = new MyDiary();
    SharedPreferences perferences;
    CheckBox[] checkBoxes;
    Boolean[] booleen;
    String[] strings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customs, container, false);
        dbHelper = new MyDatabaseHelper(getActivity());
        checkBoxes = new CheckBox[16];
        checkBoxes[0] = (CheckBox) view.findViewById(R.id.checkBox);
        checkBoxes[1] = (CheckBox) view.findViewById(R.id.checkBox2);
        checkBoxes[2] = (CheckBox) view.findViewById(R.id.checkBox3);
        checkBoxes[3] = (CheckBox) view.findViewById(R.id.checkBox4);
        checkBoxes[4] = (CheckBox) view.findViewById(R.id.checkBox5);
        checkBoxes[5] = (CheckBox) view.findViewById(R.id.checkBox6);
        checkBoxes[6] = (CheckBox) view.findViewById(R.id.checkBox7);
        checkBoxes[7] = (CheckBox) view.findViewById(R.id.checkBox8);
        checkBoxes[8] = (CheckBox) view.findViewById(R.id.checkBox9);
        checkBoxes[9] = (CheckBox) view.findViewById(R.id.checkBox10);
        checkBoxes[10] = (CheckBox) view.findViewById(R.id.checkBox11);
        checkBoxes[11] = (CheckBox) view.findViewById(R.id.checkBox12);
        checkBoxes[12] = (CheckBox) view.findViewById(R.id.checkBox13);
        checkBoxes[13] = (CheckBox) view.findViewById(R.id.checkBox14);
        checkBoxes[14] = (CheckBox) view.findViewById(R.id.checkBox15);
        checkBoxes[15] = (CheckBox) view.findViewById(R.id.checkBox16);

        booleen=new Boolean[16];
        strings=new String[16];

        checkBoxes[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[0]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[0].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[0].getText().toString()));
                }
            }
        });
        checkBoxes[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[1]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[1].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[1].getText().toString()));
                }
            }
        });
        checkBoxes[2].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[2]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[2].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[2].getText().toString()));
                }
            }
        });
        checkBoxes[3].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[3]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[3].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[3].getText().toString()));
                }
            }
        });
        checkBoxes[4].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[4]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[4].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[4].getText().toString()));
                }
            }
        });
        checkBoxes[5].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[5]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[5].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[5].getText().toString()));
                }
            }
        });
        checkBoxes[6].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[6]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[6].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[6].getText().toString()));
                }
            }
        });
        checkBoxes[7].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[7]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[7].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[7].getText().toString()));
                }
            }
        });
        checkBoxes[8].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[8]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[8].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[8].getText().toString()));
                }
            }
        });
        checkBoxes[9].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[9]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[9].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[9].getText().toString()));
                }
            }
        });
        checkBoxes[10].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[10]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[10].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[10].getText().toString()));
                }
            }
        });
        checkBoxes[11].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[11]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[11].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[11].getText().toString()));
                }
            }
        });
        checkBoxes[12].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[12]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[12].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[12].getText().toString()));
                }
            }
        });
        checkBoxes[13].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[13]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[13].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[13].getText().toString()));
                }
            }
        });
        checkBoxes[14].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[14]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[14].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[14].getText().toString()));
                }
            }
        });
        checkBoxes[15].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                booleen[15]=isChecked;
                if (isChecked) {
                    myDiary.diary(dbHelper, Diary.passOpt_select(checkBoxes[15].getText().toString()));
                } else {
                    myDiary.diary(dbHelper, Diary.passOpt_cancel(checkBoxes[15].getText().toString()));
                }
            }
        });

        perferences = getActivity().getSharedPreferences("checkBoxState", MODE_PRIVATE);
        checkBoxes[0].setChecked(perferences.getBoolean("cb1", false));
        checkBoxes[1].setChecked(perferences.getBoolean("cb2", false));
        checkBoxes[2].setChecked(perferences.getBoolean("cb3", false));
        checkBoxes[3].setChecked(perferences.getBoolean("cb4", false));
        checkBoxes[4].setChecked(perferences.getBoolean("cb5", false));
        checkBoxes[5].setChecked(perferences.getBoolean("cb6", false));
        checkBoxes[6].setChecked(perferences.getBoolean("cb7", false));
        checkBoxes[7].setChecked(perferences.getBoolean("cb8", false));
        checkBoxes[8].setChecked(perferences.getBoolean("cb9", false));
        checkBoxes[9].setChecked(perferences.getBoolean("cb10", false));
        checkBoxes[10].setChecked(perferences.getBoolean("cb11", false));
        checkBoxes[11].setChecked(perferences.getBoolean("cb12", false));
        checkBoxes[12].setChecked(perferences.getBoolean("cb13", false));
        checkBoxes[13].setChecked(perferences.getBoolean("cb14", false));
        checkBoxes[14].setChecked(perferences.getBoolean("cb15", false));
        checkBoxes[15].setChecked(perferences.getBoolean("cb16", false));
        checkBoxes[0].setText(perferences.getString("cbt1", ""));
        checkBoxes[1].setText(perferences.getString("cbt2", ""));
        checkBoxes[2].setText(perferences.getString("cbt3", ""));
        checkBoxes[3].setText(perferences.getString("cbt4", ""));
        checkBoxes[4].setText(perferences.getString("cbt5", ""));
        checkBoxes[5].setText(perferences.getString("cbt6", ""));
        checkBoxes[6].setText(perferences.getString("cbt7", ""));
        checkBoxes[7].setText(perferences.getString("cbt8", ""));
        checkBoxes[8].setText(perferences.getString("cbt9", ""));
        checkBoxes[9].setText(perferences.getString("cbt10", ""));
        checkBoxes[10].setText(perferences.getString("cbt11", ""));
        checkBoxes[11].setText(perferences.getString("cbt12", ""));
        checkBoxes[12].setText(perferences.getString("cbt13", ""));
        checkBoxes[13].setText(perferences.getString("cbt14", ""));
        checkBoxes[14].setText(perferences.getString("cbt15", ""));
        checkBoxes[15].setText(perferences.getString("cbt16", ""));
        for (int i=0;i<16;i++){
            booleen[i]=checkBoxes[i].isChecked();
            strings[i]=checkBoxes[i].getText().toString();
        }
        return view;
    }

    public interface CallBacks {
        void process(Boolean[] booleen);
        void values(String[] strings);
    }
    public void state(CallBacks callBacks) {
        callBacks.process(booleen);
        callBacks.values(strings);
    }

}
