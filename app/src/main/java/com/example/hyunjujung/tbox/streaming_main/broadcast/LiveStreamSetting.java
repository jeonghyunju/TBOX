package com.example.hyunjujung.tbox.streaming_main.broadcast;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.hyunjujung.tbox.R;
//import com.example.hyunjujung.tbox.databinding.LiveStremSettingBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  [ 라이브 방송 제목과 공개/비공개 설정하는 화면 ]
 *
 *  - 하단 버튼을 누르면 방송을 시작하는 카메라 화면으로 넘어간다
 */

public class LiveStreamSetting extends AppCompatActivity {
    //LiveStremSettingBinding settingBinding;

    //  데이터베이스의 livestream_list 테이블에 들어갈 경로 저장 변수
    String live_path;

    EditText livestreamTitle;
    RadioGroup livestreamRG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.live_strem_setting);

        livestreamTitle = (EditText)findViewById(R.id.livestream_title);
        livestreamRG = (RadioGroup)findViewById(R.id.livestream_RG);
    }

    public void startLive(View view) {
        int id = view.getId();
        final SharedPreferences loginShared = getSharedPreferences("logins", MODE_PRIVATE);
        Log.e("로그인 아이디 있나요", loginShared.getString("loginId", null) + "");
        live_path = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + livestreamTitle.getText().toString();
        switch (id) {
            /* 라이브 방송 시작 */
            case R.id.startLive_Btn:
                if(livestreamTitle.getText().toString().equals("")) {
                    /* 제목 설정 안했을 경우 토스트 메시지 띄운다 */
                    Toast.makeText(this, "방송 타이틀을 설정하세요", Toast.LENGTH_SHORT).show();
                }else {
                    /* 제목 설정 후
                     *  - 비공개로 방을 만들었다면 다이얼로그를 통해 비밀번호를 입력받는다
                     *  - 데이터베이스의 livestream_list 테이블에 저장한다
                     *  - LiveStreamStart 화면으로 이동한다
                     */
                    RadioButton radio_set = (RadioButton)findViewById(livestreamRG.getCheckedRadioButtonId());
                    final String liveSet = radio_set.getText().toString(); // 공개/비공개 저장 변수

                    if(liveSet.equals("공개")) {
                        /* 설정 : 공개
                         * - 비밀번호를 입력받지 않고 LiveStreamStart 화면으로 이동해서 방송 시작한다 */
                        setIntentExtras(loginShared, liveSet, 0, null);

                    }else {
                        /* 설정 : 비공개
                         *  - 다이얼로그로 비밀번호를 입력 받는다*/
                        AlertDialog.Builder dialBuilder = new AlertDialog.Builder(this, R.style.dialogTheme);
                        final EditText passEdit = new EditText(this);
                        passEdit.setPadding(10, 10, 10, 10);
                        passEdit.setHint("password...");
                        passEdit.setInputType(0x00000081);
                        dialBuilder.setTitle("비공개 설정");
                        dialBuilder.setMessage("비밀번호를 입력하세요");
                        dialBuilder.setView(passEdit);
                        dialBuilder.setPositiveButton("입력", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                /* LiveStreamStart 화면으로 이동 */
                                setIntentExtras(loginShared, liveSet, 0, passEdit.getText().toString());

                            }
                        });
                        dialBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        dialBuilder.show();
                    }

                }
                break;

            /* 라이브 세팅 취소 */
            case R.id.cancel_setting:
                finish();
                break;
        }
    }

    public void setIntentExtras(SharedPreferences loginShared, String liveSet, int vodTag, String livepass) {
        Intent liveStart = new Intent(this, LiveStreamStart.class);
        liveStart.putExtra("liveTitle", livestreamTitle.getText().toString());
        liveStart.putExtra("liveSet", liveSet);
        liveStart.putExtra("pathURL", live_path);
        liveStart.putExtra("vod_tag", vodTag);
        liveStart.putExtra("livePass", livepass);
        liveStart.putExtra("vodThumb", live_path + ".png");
        startActivity(liveStart);
    }
}
