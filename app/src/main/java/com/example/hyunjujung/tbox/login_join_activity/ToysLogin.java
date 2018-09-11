package com.example.hyunjujung.tbox.login_join_activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.hyunjujung.tbox.FCM.FcmIntanceId;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.chatting.ChatService;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.example.hyunjujung.tbox.streaming_main.Streaming_main;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ 로그인을 위한 화면 ]
 *
 *  - 가입시에 입력했던 이메일과 비밀번호를 입력받아서 Retrofit 을 통해 서버로 값을 넘긴다
 *  - 유효성 검사는 PHP 파일에서 확인
 *  - SIGN IN 버튼 클릭하면 회원가입 화면으로 넘어간다
 *
 */

public class ToysLogin extends AppCompatActivity {

    /* 로그인 이메일, 비밀번호 EditText */
    EditText login_email_et, login_password_et;

    ChatService chatService;

    String socketMessage;

    String roomIds = "No Enter Room";

    FcmIntanceId fcmIntanceId = new FcmIntanceId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.toys_login);

        login_email_et = (EditText)findViewById(R.id.login_email_et);
        login_password_et = (EditText)findViewById(R.id.login_password_et);

    }

    /*
     *  [ SIGN IN & 회원가입 버튼 클릭 이벤트 ]
     *   - SIGN IN 버튼 누르면 입력된 이메일과 비밀번호를 가지고 PHP 파일에서 유효성 검사한다
     *   - 이메일과 비밀번호가 일치하면 PHP 에서 true 를 리턴하여 로그인 OK
     *   - 일치하지 않으면 PHP 에서 false 를 리턴한다
     *   - 회원가입 버튼 누르면 Join 액티비티로 넘어간다
     */
    public void login_onclick(View view) {
        int id = view.getId();
        switch (id) {
            /* SIGN IN 버튼
             * - 입력된 이메일과 비밀번호를 가지고 check_login.php 파일로 간다
             * - 데이터베이스에서 이메일과 비밀번호의 유효성을 검사하고 true, false 를 반환한다 */
            case R.id.login_ok_btn:
                Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
                Call<ServerResponse> call = apiConfig.check_Login(login_email_et.getText().toString(), login_password_et.getText().toString());
                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        ServerResponse serverResponse = response.body();
                        if(serverResponse.getMessage().equals("true")) {
                            /* 유효성 검사 확인 - 로그인 성공하면 Streaming_main 화면으로 넘어간다 */
                            Toast.makeText(getApplicationContext(), "환영합니다", Toast.LENGTH_SHORT).show();
                            //  로그인 아이디 Shared 에 저장
                            SharedPreferences loginIdShared = getSharedPreferences("logins", MODE_PRIVATE);
                            final SharedPreferences.Editor loginEdit = loginIdShared.edit();
                            loginEdit.putString("loginId", login_email_et.getText().toString());
                            loginEdit.putString("login_pro", serverResponse.getUserProfile());
                            loginEdit.putString("loginAge", serverResponse.getUserAge());
                            loginEdit.putString("loginGender", serverResponse.getUserGender());

                            //  로그인하고 shared 에 저장 한 후 메인 화면으로 이동
                            Intent go_stream_main = new Intent(getApplication().getApplicationContext(), Streaming_main.class);
                            startActivity(go_stream_main);

                            Intent serviceIntent = new Intent(getApplicationContext(), ChatService.class);
                            startService(serviceIntent);

                            JSONObject jsonObject = new JSONObject();
                            try{
                                jsonObject.put("chatFlag", false);
                                jsonObject.put("roomId", "");
                                jsonObject.put("sendUser", login_email_et.getText().toString());
                                jsonObject.put("getUser", "");
                                jsonObject.put("LatestChat", "");
                                jsonObject.put("newRoomFlag", false);
                                jsonObject.put("getUserName", "");
                                jsonObject.put("chatPCount", 0);
                                jsonObject.put("messageDate", "");
                            }catch (Exception e) {
                                e.printStackTrace();
                            }

                            /* FCM 기기 토큰 생성하고 데이터베이스 업데이트 */
                            final String fcmToken = FirebaseInstanceId.getInstance().getToken();
                            Log.e("FCM 토큰", fcmToken);
                            Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
                            retrofit2.Call<ServerResponse> call_f = apiConfig.update_token(login_email_et.getText().toString(), fcmToken);
                            call_f.enqueue(new Callback<ServerResponse>() {
                                @Override
                                public void onResponse(retrofit2.Call<ServerResponse> call, Response<ServerResponse> response) {
                                    loginEdit.putString("login_token", fcmToken);
                                    loginEdit.commit();
                                }

                                @Override
                                public void onFailure(retrofit2.Call<ServerResponse> call, Throwable t) {

                                }
                            });

                            finish();
                            chatService.sendMessageSocket(jsonObject);
                        }else {
                            /* 이메일 또는 비밀번호가 일치하지 않는다 */
                            Toast.makeText(getApplicationContext(), "이메일 또는 비밀번호를 확인하세요", Toast.LENGTH_SHORT).show();
                            login_email_et.setText("");
                            login_password_et.setText("");
                            login_email_et.requestFocus();
                        }
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {

                    }
                });
                break;

            /* 회원가입 버튼 */
            case R.id.go_join_btn:
                Intent goJoin = new Intent(this, Join.class);
                startActivity(goJoin);
                break;
        }
    }

    /* 서비스 bind */
    @Override
    protected void onStart() {
        super.onStart();
        Intent bindIntent = new Intent(this, ChatService.class);
        bindService(bindIntent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    /* binding service callback 메소드 */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ChatService.LocalBinder binder = (ChatService.LocalBinder) iBinder;
            chatService = binder.getService();
            chatService.registerCallback(chatCallBack);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    /* 서비스 콜백메소드
     * 소켓에서 받은 메시지 출력 */
    private ChatService.ChatCallBack chatCallBack = new ChatService.ChatCallBack() {
        @Override
        public void receivedMessage() {
            socketMessage = chatService.getReceiveMessage();
        }
    };
}
