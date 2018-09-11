package com.example.hyunjujung.tbox.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by hyunjujung on 2018. 5. 24..
 *
 *  [ 어플 하단의 HOME, ME, SETTINGS 버튼 ]
 */

public class BottomNavi_VpAdapter extends FragmentPagerAdapter{
    private Fragment[] fragArrays;

    public BottomNavi_VpAdapter(FragmentManager fm, Fragment[] fragArrays) {
        super(fm);
        this.fragArrays = fragArrays;
    }

    @Override
    public Fragment getItem(int position) {
        return fragArrays[position];
    }

    @Override
    public int getCount() {
        return fragArrays.length;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
