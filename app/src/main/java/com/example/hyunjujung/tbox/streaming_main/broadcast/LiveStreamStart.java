package com.example.hyunjujung.tbox.streaming_main.broadcast;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.chatting.Chat_Adapter;
import com.example.hyunjujung.tbox.chatting.ChatService;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.takusemba.rtmppublisher.Publisher;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ 라이브 스트리밍 방송 시작 화면 ]
 *
 *  - LiveStreaminSetting 화면에서 방송 제목과 공개/비공개 여부를 전달받는다
 *  - 하단 동그라미 버튼을 누르면 RTMP_URL 을 통해 카메라 영상을 RTMP 서버로 송출한다
 *  - 라이브 방송 시작하면 하단에 채팅 메세지 보낼 수 있는 EditText 와 버튼을 VISIBLE 한다
 *
 */

public class LiveStreamStart extends AppCompatActivity {

    static final String RTMP_URL = "rtmp://52.78.51.174:1935/toys/";    //  영상이 송출되는 서버 URL
    Publisher rtmp_publisher;   //  RTMP 이용하여 영상 송출하기 위한 Publisher

    RelativeLayout liveStart_Relative;
    GLSurfaceView live_surfaceView;
    ImageButton start_camera;
    EditText writeChat_et;
    ImageButton chatSend_btn;

    /* 채팅 RecyclerView */
    RecyclerView chat_recycler;
    LinearLayoutManager chatLayoutManager;
    ArrayList<JSONObject> liveChatArray = new ArrayList<>();
    Chat_Adapter chatAdapter;

    /* 채팅 서비스 */
    ChatService chatService;

    /* 소켓에서 넘어온 메세지 저장 */
    static String socketMessage;
    Handler messageHandler;

    JSONObject sendJSON = new JSONObject();
    JSONObject receiveJSON = new JSONObject();
    String host_rtmp_url = "";

    /* 로그인 아이디, 프로필 저장 변수 */
    String loginIds, loginPro = "";

    Intent titleIntnet;
    boolean ispublising = true;

    static int AERO_PK = 0;

    static long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_stream_start);
        messageHandler = new Handler();

        /* 채팅 서비스 스타트 */
        Intent serviceIntent = new Intent(this, ChatService.class);
        startService(serviceIntent);

        liveStart_Relative = (RelativeLayout)findViewById(R.id.liveStart_Relative);
        live_surfaceView = (GLSurfaceView)findViewById(R.id.live_surfaceView);
        start_camera = (ImageButton)findViewById(R.id.start_camera);
        chat_recycler = (RecyclerView)findViewById(R.id.chat_recycler);
        writeChat_et = (EditText)findViewById(R.id.writeChat_et);
        chatSend_btn = (ImageButton)findViewById(R.id.chatSend_btn);

        /* 채팅 RecyclerView 선언 */
        chatLayoutManager = new LinearLayoutManager(this);
        chat_recycler.setLayoutManager(chatLayoutManager);
        chatAdapter = new Chat_Adapter(this, true, liveChatArray);
        chat_recycler.setAdapter(chatAdapter);

        titleIntnet = getIntent();
        /* 로그인 아이디 저장 */
        SharedPreferences loginShared = getSharedPreferences("logins", MODE_PRIVATE);
        loginIds = loginShared.getString("loginId", null);
        loginPro = loginShared.getString("login_pro", null);

        host_rtmp_url = RTMP_URL + titleIntnet.getStringExtra("pathURL");

        rtmp_publisher = new Publisher.Builder(this)
                .setGlView(live_surfaceView)
                .setUrl(host_rtmp_url)
                .setSize(Publisher.Builder.DEFAULT_WIDTH, Publisher.Builder.DEFAULT_HEIGHT)
                .setAudioBitrate(Publisher.Builder.DEFAULT_AUDIO_BITRATE)
                .setVideoBitrate(1152000)   // 비디오 화질 좋게 하려면 이 값을 바꿔줘야함 ( 1152000 : SD 보다 높은 해상도, 2048000 : HD급 해상도 )
                .setCameraMode(Publisher.Builder.DEFAULT_MODE)
                .build();

        chatSend_btn.setEnabled(false);
        chatSend_btn.setColorFilter(Color.rgb(206, 206, 204));

        /* 채팅 edittext 글자 입력했을때 버튼 활성화 */
        writeChat_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().equals("")) {
                    chatSend_btn.setEnabled(false);
                    chatSend_btn.setColorFilter(Color.rgb(206, 206, 204));
                }else {
                    chatSend_btn.setEnabled(true);
                    chatSend_btn.setColorFilter(Color.rgb(254, 183, 41));
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        ispublising = true;
        writeChat_et.setVisibility(View.INVISIBLE);
        chatSend_btn.setVisibility(View.INVISIBLE);
    }

    public void startCamera(View view) {
        int id = view.getId();
        switch (id) {

            /* 스트리밍 방송 시작 */
            case R.id.start_camera:
                if(!ispublising) {

                    /* 영상 송출 중지
                     *  - 채팅 Service 중지 */
                    ispublising = true;
                    rtmp_publisher.stopPublishing();
                    start_camera.setColorFilter(Color.rgb(0, 0, 0));

                    Log.e("방송 live_path", titleIntnet.getStringExtra("pathURL"));

                    /* 영상 송출 중지
                     *  - 데이터베이스의 livestream_list 테이블에서 해당 영상의 vod_tag 값을 1로 업데이트
                     *  - 시작 시간과 끝나는 시간을 데이터베이스로 보내서 둘의 시간 차이를 업데이트한다 */
                    Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
                    retrofit2.Call<ServerResponse> call = apiConfig.update_liveList(titleIntnet.getStringExtra("pathURL"),
                                                                                    startTime, System.currentTimeMillis());
                    call.enqueue(new Callback<ServerResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<ServerResponse> call, Response<ServerResponse> response) {
                            Log.e("방송 종료 VOD 업데이트", response.body().getMessage());
                        }

                        @Override
                        public void onFailure(retrofit2.Call<ServerResponse> call, Throwable t) {
                            Log.e("방송 종료 VOD 업데이트", t.getMessage());
                        }
                    });

                    Log.e("스트리밍 송출","정지");
                    finish();

                }else {

                    /* 영상 송출 시작
                     *  - 채팅 Service 시작 */
                    ispublising = false;
                    rtmp_publisher.startPublishing();
                    start_camera.setColorFilter(Color.rgb(255, 59, 29));
                    writeChat_et.setVisibility(View.VISIBLE);
                    chatSend_btn.setVisibility(View.VISIBLE);

                    startTime = System.currentTimeMillis();

                    /* 데이터베이스의 livestream_list 테이블에 생성된 생방송 저장
                     *  - 테이블 컬럼 : host_ID(방송 생성한 사용자 아이디), host_Profile, live_title,
                     *               live_set(공개/비공개), live_password, live_count(시청자수),
                     *               live_path(.mpd 파일 경로), vod_tag(생방송인지 VOD 인지 설정하는 태그), vod_thumnail(썸네일 경로)
                     *  - vod_tag = 0 : 생방송 / vod_tag = 1 : VOD */
                    Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
                    Call<ServerResponse> call = apiConfig.insert_liveList(loginIds, titleIntnet.getStringExtra("liveTitle"),
                                                                            titleIntnet.getStringExtra("liveSet"),
                                                                            titleIntnet.getStringExtra("pathURL"),
                                                                            titleIntnet.getIntExtra("vod_tag", 0),
                                                                            titleIntnet.getStringExtra("livePass"),
                                                                            titleIntnet.getStringExtra("vodThumb"),
                                                                            startTime);
                    call.enqueue(new Callback<ServerResponse>() {
                        @Override
                        public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                            Log.e("방송 목록 저장", response.body().getMessage());
                        }

                        @Override
                        public void onFailure(Call<ServerResponse> call, Throwable t) {
                            Log.e("방송 목록 저장", t.getMessage());
                        }
                    });

                    Log.e("스트리밍 송출", "시작");

                    /* 영상 송출 시작 하면 printWriter 로 서버에 채팅방 개설 됐다는 메세지 보내기
                     *  - json 형식 : {"newRoomFlag":"true",
                     *                "chatFlag":"true",
                     *                "roomId":"방송 path",
                     *                "sendUser":"보내는사람",
                     *                "message":"내용",
                     *                "messageDate":"날짜"} */
                    try{
                        sendJSON.put("newRoomFlag", true);
                        sendJSON.put("chatFlag", true);
                        sendJSON.put("roomId", titleIntnet.getStringExtra("pathURL"));
                        sendJSON.put("hostUser", loginIds);
                        sendJSON.put("sendUser", loginIds);
                        sendJSON.put("sendProfile", loginPro);
                        sendJSON.put("message", "");

                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    /* 서비스를 통해 메세지 보내기 */
                    chatService.sendMessageSocket(sendJSON);

                    /* 자신의 방송 북마크한 사용자에게 FCM 메세지 보내기 */
                    sendFCM_message(loginIds, loginIds + " 님이 방송을 시작했습니다!");
                }
                break;

            /* 우측 상단 카메라 회전 버튼 */
            case R.id.switch_camera:
                rtmp_publisher.switchCamera();
                break;
        }
    }

    /* 방송 송출 시작 전에 백버튼 잘못눌렀을 경우 알림창 띄워준다
     *  - 방송 송출을 취소할 경우 LiveStreamSetting 에서 데이터베이스에 넣어줬던 목록을 삭제한다 */
    @Override
    public void onBackPressed() {
        /* 방송 송출 취소 */
        DialogInterface.OnClickListener cancelLive = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
                Call<ServerResponse> call = apiConfig.delete_liveList(titleIntnet.getStringExtra("pathURL"));
                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        Log.e("라이브리스트 삭제", response.body().getMessage());
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                        Log.e("라이브리스트 삭제", t.getMessage());
                    }
                });

                finish();
            }
        };
        DialogInterface.OnClickListener noCancel = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };
        new AlertDialog.Builder(this)
                .setTitle("알림")
                .setMessage("라이브 방송을 취소하시겠습니까?")
                .setNegativeButton("아니오", noCancel)
                .setPositiveButton("예", cancelLive)
                .show();
    }

    /* FCM 전송 메소드 */
    public void sendFCM_message(String vodHostId, String message) {
        Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
        Call<ServerResponse> call = apiConfig.sendFCM(vodHostId, message);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }


    /* ====================================== 채팅 ===================================== */

    /* 채팅 메시지 보내기 */
    public void chatSend(View view) {
        if(writeChat_et.getText().toString() == null || writeChat_et.getText().toString().equals("")) {
            Toast.makeText(this, "메세지를 입력하세요", Toast.LENGTH_SHORT).show();
        }else {
            try{
                String mDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                sendJSON.put("newRoomFlag", false);
                sendJSON.put("messageDate", mDate);
                sendJSON.put("message", writeChat_et.getText().toString());
                sendJSON.put("startTime", startTime);

                sendJSON.put("aeroPK", AERO_PK++);

                /* 보낸 채팅 메세지 나한테도 보이기 */
                liveChatArray.add(sendJSON);
                Message handlerM = handler.obtainMessage();
                handler.sendMessage(handlerM);

                /* 대화내용 서비스를 통해 소켓으로 보내기 */
                chatService.sendMessageSocket(sendJSON);

                /* 채팅 내용 Nosql 에 저장 */
                Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
                Call<ServerResponse> call = apiConfig.insert_chat(titleIntnet.getStringExtra("pathURL") + "_" + AERO_PK,
                                                                loginIds, loginPro,
                                                                writeChat_et.getText().toString(),
                                                                titleIntnet.getStringExtra("pathURL"), startTime, mDate);
                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        Log.e("채팅 내용 저장", response.body().getMessage());
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                        Log.e("채팅 내용 저장", t.getMessage());
                    }
                });

            }catch (Exception e) {
                e.printStackTrace();
            }
            writeChat_et.setText("");
        }
    }

    /* 받은 채팅 메세지 리사이클러뷰에 출력 */
    private Runnable showMessage = new Runnable() {
        @Override
        public void run() {
            try{
                receiveJSON = new JSONObject(socketMessage);
                Log.e("livestreamstart 받은 메세지", receiveJSON.toString());
                liveChatArray.add(receiveJSON);
                Message message = handler.obtainMessage();
                handler.sendMessage(message);
                if(AERO_PK == 0) {
                    AERO_PK ++;
                }else {
                    AERO_PK = receiveJSON.getInt("aeroPK") + 1;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //chatAdapter.notifyItemInserted(liveChatArray.size() - 1);
            chatAdapter.notifyDataSetChanged();
            chat_recycler.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
    };

    /* 채팅 서비스 바인드 */
    @Override
    protected void onStart() {
        super.onStart();
        Intent bindIntent = new Intent(this, ChatService.class);
        bindService(bindIntent, mConnection, BIND_AUTO_CREATE);
    }

    /* 바인드된 서비스 unbind 해준다 */
    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        AERO_PK = 0;
    }

    /* binding 서비스 콜백 */
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

    /* 서비스 콜백 메소드
     *  - 소켓에서 받은 메세지 출력 */
    private ChatService.ChatCallBack chatCallBack = new ChatService.ChatCallBack() {
        @Override
        public void receivedMessage() {
            socketMessage = chatService.getReceiveMessage();
            messageHandler.post(showMessage);
        }
    };

}
