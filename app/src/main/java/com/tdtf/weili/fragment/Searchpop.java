package com.tdtf.weili.fragment;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tdtf.weili.R;
import com.tdtf.weili.Utils.Diary;
import com.tdtf.weili.activity.Search;
import com.tdtf.weili.database.MyDatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class Searchpop extends Fragment {
    MyDatabaseHelper dbHelper;
    Button btndatepickstart;
    Button btndatepickend;
    Button btnpower;
    Button btnquery;
    AutoCompleteTextView autouser;
    String startdatepick;
    String enddatepick;
    ArrayList<String> powerList;
    ListPopupWindow popupWindow;
    LinearLayout linearLayout;
    Cursor cursor, mCursor;
    View rootView;

    public Searchpop() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_searchpop2, container, false);
        dbHelper = new MyDatabaseHelper(getActivity());
        try {
            mCursor = dbHelper.getReadableDatabase().rawQuery(
                    "select _id,userName from User", null);
            inflateList_auto(mCursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        btndatepickstart = (Button) rootView.findViewById(R.id.btn_dataStart);
        btndatepickend = (Button) rootView.findViewById(R.id.btn_dataEnd);
        btnpower = (Button) rootView.findViewById(R.id.btn_power);
        btnquery = (Button) rootView.findViewById(R.id.btn_quary);
        autouser = (AutoCompleteTextView) rootView.findViewById(R.id.edit_autoUser);

        btndatepickstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
                        btndatepickstart.setText(year + "年" + (month + 1) + "月" + dayOfMonth + "日");
                        String mm, dd;
                        if (month < 9) {
                            mm = "0" + (month + 1);
                        } else {
                            mm = String.valueOf(month + 1);
                        }
                        if (dayOfMonth < 9) {
                            dd = "0" + dayOfMonth;
                        } else {
                            dd = String.valueOf(dayOfMonth);
                        }
                        startdatepick = year + "-" + mm + "-" + dd;
                    }
                }
                        , c.get(Calendar.YEAR)
                        , c.get(Calendar.MONTH)
                        , c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        btndatepickend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker dp, int year, int month, int dayOfMonth) {
                        btndatepickend.setText(year + "年" + (month + 1) + "月" + dayOfMonth + "日");
                        String mm, dd;
                        if (month < 9) {
                            mm = "0" + (month + 1);
                        } else {
                            mm = String.valueOf(month + 1);
                        }
                        if (dayOfMonth < 9) {
                            dd = "0" + dayOfMonth;
                        } else {
                            dd = String.valueOf(dayOfMonth);
                        }
                        enddatepick = year + "-" + mm + "-" + dd;
                    }
                }
                        , c.get(Calendar.YEAR)
                        , c.get(Calendar.MONTH)
                        , c.get(Calendar.DAY_OF_MONTH)).show();
            }
        });//

        powerList = new ArrayList<>();
        powerList.add("管理员");
        powerList.add("操作员");
        powerList.add("维护员");

        popupWindow = new ListPopupWindow(getActivity());
        popupWindow.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, powerList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(Color.parseColor("#ff000000"));
                tv.setTextSize(24);
                return tv;
            }
        });
        popupWindow.setAnchorView(btnpower);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setModal(true);
        popupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                btnpower.setText(powerList.get(position));
                popupWindow.dismiss();
            }
        });
        btnpower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.show();
            }
        });

        btnquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sql = "select * from Diary where 1=1";
                String sqlstartdate = "00:00:00";
                String sqlenddate = "23:59:59";
                String[] strings = null;
                try {
                    if (TextUtils.isEmpty(autouser.getText()) && btnpower.getText().toString().equals("选择权限")) {
                        sql += " and dateTime between ? and ?";
                        strings = new String[]{startdatepick + " " + sqlstartdate, enddatepick + " " + sqlenddate};
                    }
                    if (!TextUtils.isEmpty(autouser.getText()) && btnpower.getText().toString().equals("选择权限")) {
                        sql += " and userName like ? and dateTime between ? and ?";
                        strings = new String[]{autouser.getText().toString(), startdatepick + " " + sqlstartdate, enddatepick + " " + sqlenddate};
                    }
                    if (TextUtils.isEmpty(autouser.getText()) && !btnpower.getText().toString().equals("选择权限")) {
                        sql += " and powerName= ? and dateTime between ? and ?";
                        strings = new String[]{btnpower.getText().toString(), startdatepick + " " + sqlstartdate, enddatepick + " " + sqlenddate};
                    }
                    if (!TextUtils.isEmpty(autouser.getText()) && !btnpower.getText().toString().equals("选择权限")) {
                        sql += " and powerName= ? and userName like ? and dateTime between ? and ?";
                        strings = new String[]{btnpower.getText().toString(), autouser.getText().toString(), startdatepick + " " + sqlstartdate, enddatepick + " " + sqlenddate};
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "检索条件输入有误，请重新输入", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                cursor = dbHelper.getReadableDatabase().rawQuery(
                        sql, strings);//cursor里必须包含主键"_id"

                inflateList(cursor);
                linearLayout = (LinearLayout) getActivity().findViewById(R.id.fragLayout);
                linearLayout.setVisibility(View.INVISIBLE);
            }
        });
        return rootView;
    }

    private void inflateList(Cursor cursor) {
        final ListView diarylist = (ListView) getActivity().findViewById(R.id.diarylist);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.partner,
                cursor,
                new String[]{"_id", "dateTime", "context", "powerName", "userName"},
                new int[]{
                        R.id.item_layout,
                        R.id.item_dateTime,
                        R.id.item_context,
                        R.id.item_userName,
                        R.id.item_powerName},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        diarylist.setAdapter(adapter);
    }

    private void inflateList_auto(Cursor cursor) {
        ArrayList<String> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            list.add(cursor.getString(cursor.getColumnIndex("userName")));
        }
        ArrayAdapter<String> autotext = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, list);
        autouser.setAdapter(autotext);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
        mCursor.close();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
