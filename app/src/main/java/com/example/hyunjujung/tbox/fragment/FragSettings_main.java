package com.example.hyunjujung.tbox.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.hyunjujung.tbox.AR.drawLine.AR_DrawingLine;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.databinding.FragsettingsMainBinding;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.example.hyunjujung.tbox.streaming_main.ShowChart;
import com.example.hyunjujung.tbox.streaming_main.camera.faceDetect.FaceDetectActivity;
import com.example.hyunjujung.tbox.streaming_main.map.TmapSearchPath;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ SETTINGS 화면 ]
 *
 *  - 로그아웃
 *  - 정보 변경
 *  - 방송 알림 켜고, 끄기
 *  - 시청자 통계 보기
 *  - 길찾기
 *  - AR 체험하기
 */

public class FragSettings_main extends Fragment {
    FragsettingsMainBinding dataBindingSet;
    SettingClickEvent settingClickEvent = new SettingClickEvent();

    SharedPreferences loginShared;  // 로그인시에 Shared 에 저장했던 로그인아이디, 로그인프로필, 기기 토큰 데이터를 가져오기 위해서

    String loginId; // Shared 에 저장된 로그인 아이디 변수

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dataBindingSet = DataBindingUtil.inflate(inflater, R.layout.fragsettings_main, container, false);
        dataBindingSet.setSettingClick(settingClickEvent);

        loginShared = getActivity().getSharedPreferences("logins", Context.MODE_PRIVATE);
        loginId = loginShared.getString("loginId", null);

        /* 방송 알림 켜고 끈다
         *  - 데이터베이스의 bookmark_list 테이블에 isAlert 값을 업데이트 한다
         *  - 0 : 방송 알림 On
         *  - 1 : 방송 알림 Off */
        dataBindingSet.alertSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            int isAlert = 0;
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) { //  방송 알림 On
                    Toast.makeText(getActivity().getApplicationContext(), "방송 알림 ON", Toast.LENGTH_SHORT).show();
                    isAlert = 0;
                }else { // 방송 알림 Off
                    Toast.makeText(getActivity().getApplicationContext(), "방송 알림 OFF", Toast.LENGTH_SHORT).show();
                    isAlert = 1;
                }
                updateBookmark(isAlert, loginId);   // 방송 알림 데이터 베이스에 업데이트
            }
        });

        return dataBindingSet.getRoot();
    }

    public class SettingClickEvent {
        /* 로그아웃, 정보변경 버튼 클릭이벤트
         * !!!!!!! 아직 구현 안했음 !!!!!!!! */
        public void onClickAccount(View view) {
            switch (view.getId()) {
                case R.id.logout_tv:
                    Log.e("onClickAccount", "로그아웃");
                    break;

                case R.id.change_tv:
                    Log.e("onClickAccount", "정보 변경");
                    break;

                case R.id.mobileWalletRel:  // 모바일 이더리움 계정 연결
                    Intent mobileWalletIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.example.hyunjujung.tboxwallet");
                    startActivity(mobileWalletIntent);
                    break;
            }
        }

        /* 통계 보기 버튼 클릭 이벤트 */
        public void onClickChart(View view) {
            switch (view.getId()) {
                case R.id.chart_rel:    // 시청자 통계 화면으로 이동
                    Intent goChart = new Intent(getContext(), ShowChart.class);
                    startActivity(goChart);
                    break;
            }
        }

        /* 길찾기 버튼 클릭 이벤트 */
        public void onClickMap(View view) {
            switch (view.getId()) {
                case R.id.tmap_relative:    // Tmap 이용한 경로 탐색 화면으로 이동
                    Intent goTmap = new Intent(getContext(), TmapSearchPath.class);
                    startActivity(goTmap);
                    break;
            }
        }

        /* AR 체험하기 클릭 이벤트 */
        public void onClickArExperience(View view) {
            switch (view.getId()) {
                case R.id.drawingLine_rel: // Drawing Line 화면으로 이동
                    Intent goDrawingLine = new Intent(getContext(), AR_DrawingLine.class);
                    startActivity(goDrawingLine);
                    break;
                case R.id.arPortal_rel: //  AR Portal 앱 실행
                    Intent arPortalIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.hyunjujung.parkportals");
                    startActivity(arPortalIntent);
                    break;
                case R.id.runPengu_rel: //  3D 게임 실행
                    Intent runPenguIntent = getActivity().getPackageManager().getLaunchIntentForPackage("com.hyunjujung.pengurun");
                    startActivity(runPenguIntent);
                    break;

            }
        }

        /* 카메라를 이용한 사진찍기 버튼 클릭 이벤트 */
        public void onClickCamera(View view) {
            switch (view.getId()) {
                case R.id.faceDetection_rel:    //  사진 찍기 화면으로 이동
                    Intent goFaceDetect = new Intent(getContext(), FaceDetectActivity.class);
                    startActivity(goFaceDetect);
                    break;
            }
        }
    }

    /* 방송 알림 업데이트 메소드 */
    public void updateBookmark(int isAlert, String loginId) {
        Retrofit_ApiConfig updateApi = Retrofit_Creator.getApiConfig();
        Call<ServerResponse> updateCall = updateApi.update_bookmark(loginId, isAlert);
        updateCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                Log.e("북마크 알림 업데이트", response.body().getMessage());
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                Log.e("북마크 알림 업데이트", t.getMessage());
            }
        });
    }
}
