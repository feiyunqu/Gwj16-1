package com.tdtf.weili.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tdtf.weili.R;

import static android.content.Context.MODE_PRIVATE;

public class BlankFragment extends Fragment {

    SharedPreferences perferences;
    int fff;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        perferences =getActivity().getSharedPreferences("checkBoxState",MODE_PRIVATE);
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
        int[] layout={R.layout.fragment_custom,R.layout.fragment_mazuiqiju,R.layout.fragment_lvchuzero,
        R.layout.fragment_wuran_five,R.layout.fragment_wuran_nintyeight,R.layout.fragment_yaodian};
        return inflater.inflate(layout[fff], container, false);
    }
    @Override
    public void onStart(){
        super.onStart();

    }
}
