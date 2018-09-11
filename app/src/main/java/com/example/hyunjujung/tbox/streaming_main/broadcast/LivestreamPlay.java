package com.example.hyunjujung.tbox.streaming_main.broadcast;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.chatting.Chat_Adapter;
import com.example.hyunjujung.tbox.chatting.ChatService;
import com.example.hyunjujung.tbox.data_vo.chatting.ChatItemVO;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ 실시간 방송 시청 화면 ]
 *
 *  - FragStream_main 의 실시간 방송 목록에서 하나의 방송을 클릭하면 넘어오는 화면이다
 *  - 클릭된 방송에 해당하는 실시간 방송을 시청하는 화면
 *  - 채팅 기능
 *
 */

public class LivestreamPlay extends AppCompatActivity {
    static final String DASH_URI = "http://52.78.51.174/toysLive/";
    SimpleExoPlayerView livestream_player;

    Handler mainHandler;
    SimpleExoPlayer simpleExoPlayer;
    TrackSelection.Factory videoTrackSelectionFactory;
    TrackSelector trackSelector;
    LoadControl loadControl;
    DataSource.Factory dataSourceFactory;
    MediaSource videoSource;
    Uri dash_uri;
    String userAgent;
    final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

    Intent StreamList_adap_intent;

    /* 상단에 보이는 live 방송 텍스트뷰와 이미지 */
    TextView live_text;
    ImageView live_image;

    /* 라이브 방송일때 채팅 RecyclerView */
    RecyclerView liveChat_R;
    LinearLayoutManager chatLayoutManager;
    ArrayList<JSONObject> liveChatArray = new ArrayList<>();
    Chat_Adapter chatAdapter;

    /* VOD 재생할때 해당 VOD 채팅내용 모두 저장하는 ArrayList */
    ArrayList<ChatItemVO> vodChatArray = new ArrayList<>();

    /* 채팅 EditText, 버튼 */
    EditText playChat_write;
    ImageButton chatSend_btn;

    /* 채팅 서비스 */
    ChatService chatService;

    /* 소켓에서 넘어온 메세지 저장 */
    static String socketMessage;
    Handler messageHandler;

    /* 보내는 메세지 저장하는 jsonObject */
    JSONObject sendJSON = new JSONObject();

    /* 받은 메세지 저장하는 jsonObject */
    JSONObject receiveJSON = new JSONObject();

    /* 로그인 아이디, 프로필 저장 변수 */
    String loginIds, loginPro = "";

    static int AERO_PK = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.livestream_play);
        messageHandler = new Handler();

        livestream_player = (SimpleExoPlayerView)findViewById(R.id.livestream_player);

        live_text = (TextView)findViewById(R.id.live_text);
        live_image = (ImageView)findViewById(R.id.live_image);
        playChat_write = (EditText)findViewById(R.id.playChat_write);
        chatSend_btn = (ImageButton)findViewById(R.id.chatSend_btn);
        liveChat_R = (RecyclerView)findViewById(R.id.liveChat_R);

        userAgent = Util.getUserAgent(this, "Exo_Dash_Player");

        SharedPreferences loginShared = getSharedPreferences("logins", MODE_PRIVATE);
        loginIds = loginShared.getString("loginId", null);
        loginPro = loginShared.getString("login_pro", null);

        /* 채팅 recyclerView */
        chatLayoutManager = new LinearLayoutManager(this);
        liveChat_R.setLayoutManager(chatLayoutManager);
        chatAdapter = new Chat_Adapter(this, true, liveChatArray);
        liveChat_R.setAdapter(chatAdapter);

        /* create player */
        mainHandler = new Handler();
        videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        loadControl = new DefaultLoadControl();
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        StreamList_adap_intent = getIntent();

        /* 플레이어에 view 를 바인드 한다 */
        livestream_player.setPlayer(simpleExoPlayer);

        Log.e("라이브, VOD 경로", StreamList_adap_intent.getStringExtra("live_path"));
        dash_uri = Uri.parse(DASH_URI + StreamList_adap_intent.getStringExtra("live_path") + ".mpd");
        dataSourceFactory = buildDataSourceFactory(bandwidthMeter);
        videoSource = new DashMediaSource(dash_uri, buildDataSourceFactory(null), new DefaultDashChunkSource.Factory(dataSourceFactory), mainHandler, null);
        simpleExoPlayer.prepare(videoSource);
        simpleExoPlayer.setPlayWhenReady(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /* 라이브 방송 시청시에
         *  - 하단 컨트롤러 없애기
         *  - 상단 LIVE 텍스트 보이기
         *      : chatFlag = false
         *      : roomId 로 채팅방의 고유 아이디를 채팅 서버에 보낸다
         */
        livestream_player.setUseController(false);
        live_text.setVisibility(View.VISIBLE);
        live_image.setVisibility(View.VISIBLE);
        chatSend_btn.setVisibility(View.VISIBLE);
        playChat_write.setVisibility(View.VISIBLE);

        try {
            sendJSON.put("newRoomFlag", false);
            sendJSON.put("chatFlag", false);
            sendJSON.put("roomId", StreamList_adap_intent.getStringExtra("live_path"));
            sendJSON.put("sendUser", loginIds);
            sendJSON.put("sendProfile", loginPro);
            sendJSON.put("message", "no message");

            Log.e("라이브 플레이 제이슨", sendJSON.toString());
            chatService.sendMessageSocket(sendJSON);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    /* 채팅 보내기 */
    public void chatSend(View view) {
        if(playChat_write.getText().toString().equals("") || playChat_write.getText().toString() == null) {
            Toast.makeText(this, "메세지를 입력하세요", Toast.LENGTH_SHORT).show();
        }else {
            try {
                String mDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                sendJSON.put("chatFlag", true);
                sendJSON.put("messageDate", mDate);
                sendJSON.put("message", playChat_write.getText().toString());

                /* VOD 재생시에 채팅과 영상의 싱크를 맞추기 위해 라이브 방송 시작 시간을 millisSecond 로 보낸다 */
                sendJSON.put("startTime", StreamList_adap_intent.getLongExtra("liveStart", 0));

                /* Nosql 의 PK 를 생성하기 위한 값 */
                sendJSON.put("aeroPK", AERO_PK++);

                /* 내가 보낸 메세지 나에게 표시 */
                liveChatArray.add(sendJSON);
                Message handlerM = handler.obtainMessage();
                handler.sendMessage(handlerM);

                chatService.sendMessageSocket(sendJSON);

                /* 채팅 내용 Nosql 에 저장 */
                Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
                retrofit2.Call<ServerResponse> call = apiConfig.insert_chat(StreamList_adap_intent.getStringExtra("live_path") + "_" + AERO_PK,
                                                                            loginIds, loginPro,
                                                                            playChat_write.getText().toString(),
                                                                            StreamList_adap_intent.getStringExtra("live_path"),
                                                                            StreamList_adap_intent.getLongExtra("liveStart", 0), mDate);
                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(retrofit2.Call<ServerResponse> call, Response<ServerResponse> response) {
                        Log.e("채팅 내용 저장", response.body().getMessage());
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ServerResponse> call, Throwable t) {
                        Log.e("채팅 내용 저장", t.getMessage());
                    }
                });

            }catch (Exception e) {
                e.printStackTrace();
            }

            playChat_write.setText("");
        }
    }

    /* 받은 채팅 메세지 리사이클러뷰에 출력 */
    private Runnable showMessage = new Runnable() {
        @Override
        public void run() {
            try{
                receiveJSON = new JSONObject(socketMessage);
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
            chatAdapter.notifyDataSetChanged();
            liveChat_R.scrollToPosition(chatAdapter.getItemCount() - 1);

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
            ChatService.LocalBinder binder = (ChatService.LocalBinder)iBinder;
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
