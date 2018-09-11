package com.example.hyunjujung.tbox.FCM;

import android.content.SharedPreferences;

import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ 로그인 후에 사용자 기기에 대한 토큰을 생성하고 생성한 토큰을 데이터베이스에 업데이트 한다 ]
 *
 */
public class FcmIntanceId extends FirebaseInstanceIdService {
    /* 기기에 대한 토큰 생성 */
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        sendRegistrationToServer(token);
    }

    /* 생성된 토큰을 데이터베이스에 업데이트하고 shared 에 토큰 넣어준다 */
    private void sendRegistrationToServer(String userToken) {
        SharedPreferences loginShared = getSharedPreferences("logins", MODE_PRIVATE);
        SharedPreferences.Editor tokenEdit = loginShared.edit();
        tokenEdit.putString("loginToken", userToken);

        /* 생성된 토큰 데이터베이스 (members 테이블의 userToken 테이블) 에 업데이트 */
        Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
        retrofit2.Call<ServerResponse> call = apiConfig.update_token(userToken, loginShared.getString("loginId", null));
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ServerResponse> call, Response<ServerResponse> response) {

            }

            @Override
            public void onFailure(retrofit2.Call<ServerResponse> call, Throwable t) {

            }
        });
    }
}
