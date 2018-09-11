package com.example.hyunjujung.tbox.data_vo.faceDetect;

import com.example.hyunjujung.tbox.R;

import java.util.ArrayList;

public class EmoticonArray {
    public EmoticonArray() {
    }
    public ArrayList<Integer> setEmoticon() {
        ArrayList<Integer> eArray = new ArrayList<>();
        eArray.add(R.drawable.ic_cap);
        eArray.add(R.drawable.flower_crown);
        eArray.add(R.drawable.cat_mask);
        eArray.add(R.drawable.unicorn_mask);
        eArray.add(R.drawable.dogs_mask);
        eArray.add(R.drawable.hoho_mask);
        return eArray;
    }
}
