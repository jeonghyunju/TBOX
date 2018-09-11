package com.example.hyunjujung.tbox.streaming_main;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.BottomNavi_VpAdapter;
import com.example.hyunjujung.tbox.fragment.FragSettings_main;
import com.example.hyunjujung.tbox.fragment.FragStream_main;
import com.example.hyunjujung.tbox.fragment.FragUser_main;

/**
 *  [ 스트리밍 메인 화면 : 프래그먼트 3개 가지고 있음 ]
 *
 *  - BottomNavigation 사용하여 하단에 탭버튼 3개 (HOME, ME, SETTINGS) 만들었음
 *  - HOME 버튼 : 실시간 급상승 중인 방송과 현재 방송중인 목록들을 보여준다
 *  - ME 버튼 : 사용자의 타임라인을 보여주고, 방송을 하는 사람이라면 시청자 통계를 보여준다.
 *             그리고 방송을 시작할 수 있음
 *  - SETTINGS 버튼 : 어플에 관련한 설정 화면
 */

public class Streaming_main extends AppCompatActivity {
    AHBottomNavigation streamMain_bottom;
    AHBottomNavigationViewPager streamMain_viewPager;
    BottomNavi_VpAdapter stream_pgAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streaming_main);

        streamMain_bottom = (AHBottomNavigation)findViewById(R.id.streamMain_bottom);
        streamMain_viewPager = (AHBottomNavigationViewPager)findViewById(R.id.streamMain_viewPager);

        Fragment[] fragmentArrays = new Fragment[3];
        fragmentArrays[0] = new FragStream_main();
        fragmentArrays[1] = new FragUser_main();
        fragmentArrays[2] = new FragSettings_main();

        stream_pgAdapter = new BottomNavi_VpAdapter(getSupportFragmentManager(), fragmentArrays);
        streamMain_viewPager.setAdapter(stream_pgAdapter);

        // BottomNavigation 의 탭 색상을 저장할 변수
        int[] bottom_colors = getApplicationContext().getResources().getIntArray(R.array.bottom_navi_colors);

        // BottomNavigation 의 백그라운드 색상과 BottomNavigation 에 3개의 탭버튼을 붙인다
        // - 3개의 버튼 : HOME, ME, SETTINGS
        streamMain_bottom.setDefaultBackgroundColor(Color.parseColor("#FEB729"));
        AHBottomNavigationAdapter navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_item);
        navigationAdapter.setupWithBottomNavigation(streamMain_bottom, bottom_colors);

        //BottomNavigation 탭버튼이 눌렸을때의 색상과 눌리지 않았을때 색상을 지정한다
        streamMain_bottom.setAccentColor(Color.parseColor("#00275E"));
        streamMain_bottom.setInactiveColor(Color.parseColor("#FEE9BE"));

        //하단 3개의 탭 클릭 이벤트
        streamMain_bottom.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                Log.e("탭 포지션", position + "");

                streamMain_viewPager.setCurrentItem(position);
                return true;
            }
        });

    }
}
