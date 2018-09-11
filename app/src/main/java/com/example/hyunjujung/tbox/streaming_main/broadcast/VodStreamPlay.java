package com.example.hyunjujung.tbox.streaming_main.broadcast;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.chatting.Chat_Adapter;
import com.example.hyunjujung.tbox.data_vo.chatting.ChatItemVO;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;

/**
    [ VOD 재생 화면 ]

    - FragStream_main 화면에서 VOD 목록 중 한 건 클릭하면 넘어오는 화면
    - 라이브 방송 시청할때 나눴던 채팅이 영상 시간에 맞춰서 같이 재생된다

 */

public class VodStreamPlay extends AppCompatActivity {
    private static final String TAG = "VodStreamPlay";
    static final String DASH_URI = "http://52.78.51.174/toysLive/";
    SimpleExoPlayerView vodstream_player;

    /* VOD 재생시에 필요한 변수들 */
    Handler mainHandler;
    SimpleExoPlayer simpleExoPlayer;                        //  VOD 재생하기 위한 플레이어
    TrackSelection.Factory videoTrackSelectionFactory;
    TrackSelector trackSelector;
    LoadControl loadControl;
    DataSource.Factory dataSourceFactory;
    MediaSource videoSource;
    Uri dash_uri;                                           //  VOD 영상이 존재하는 파일 경로 변수
    String userAgent;
    final DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

    Intent StreamList_adap_intent;

    /* VOD 방송일때 채팅 RecyclerView */
    RecyclerView vodChat_R;
    LinearLayoutManager chatLayoutManager;
    ArrayList<ChatItemVO> vodChatArray = new ArrayList<>();
    Chat_Adapter chatAdapter;

    /* 방송에 포함된 채팅과 영상 싱크를 맞추기 위해 채팅을 담을 ArrayList */
    ArrayList<ChatItemVO> syncChatArray = new ArrayList<>();

    Handler syncHandler;
    static int second;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vod_stream_play);
        mainHandler = new Handler();
        StreamList_adap_intent = getIntent();
        syncHandler = new Handler();

        vodstream_player = (SimpleExoPlayerView)findViewById(R.id.vodstream_player);
        vodChat_R = (RecyclerView)findViewById(R.id.vodChat_R);

        userAgent = Util.getUserAgent(this, "Exo_Dash_Player");

        /* RecyclerView */
        chatLayoutManager = new LinearLayoutManager(this);
        vodChat_R.setLayoutManager(chatLayoutManager);
        chatAdapter = new Chat_Adapter(this, syncChatArray, false);
        vodChat_R.setAdapter(chatAdapter);

        /* create player */
        mainHandler = new Handler();
        videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        loadControl = new DefaultLoadControl();
        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        /* 플레이어에 view 를 바인드 한다 */
        vodstream_player.setPlayer(simpleExoPlayer);

        Log.e("라이브, VOD 경로", StreamList_adap_intent.getStringExtra("live_path"));
        dash_uri = Uri.parse(DASH_URI + StreamList_adap_intent.getStringExtra("live_path") + ".mpd");
        dataSourceFactory = buildDataSourceFactory(bandwidthMeter);
        videoSource = new DashMediaSource(dash_uri, buildDataSourceFactory(null), new DefaultDashChunkSource.Factory(dataSourceFactory), mainHandler, null);
        simpleExoPlayer.prepare(videoSource);
        simpleExoPlayer.setPlayWhenReady(true);

        PlayerListener playerListener = new PlayerListener();
        simpleExoPlayer.addListener(playerListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* VOD 에 해당하는 채팅내용 모두 불러와서 vodChatArray 에 저장한다 */
        Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
        retrofit2.Call<List<ChatItemVO>> call = apiConfig.select_chat(StreamList_adap_intent.getStringExtra("live_path"));
        call.enqueue(new Callback<List<ChatItemVO>>() {
            @Override
            public void onResponse(retrofit2.Call<List<ChatItemVO>> call, Response<List<ChatItemVO>> response) {
                vodChatArray.addAll(response.body());

                if(second > 0) {
                    second = 0;
                }

                /* Nosql 에서 해당 VOD 의 채팅내용을 받아온 후 영상과 채팅의 싱크를 맞추기 위해 Thread 를 스타트한다 */
                SyncTime syncTime = new SyncTime();
                syncTime.start();
            }

            @Override
            public void onFailure(retrofit2.Call<List<ChatItemVO>> call, Throwable t) {
                Log.e("VOD 채팅 목록 가져오기 실패", t.getMessage());
            }
        });

    }

    /* 채팅과 영상의 싱크를 맞추기 위한 Thread */
    public class SyncTime extends Thread {
        @Override
        public void run() {
            while(true) {
                try{
                    for(int i=0 ; i<vodChatArray.size() ; i++) {
                        if((vodChatArray.get(i).getSyncTime() / 100) * 100 == second) {
                            syncChatArray.add(vodChatArray.get(i));
                            Message handlerM = handler.obtainMessage();
                            handler.sendMessage(handlerM);
                        }
                    }
                    if(syncChatArray.size() == vodChatArray.size()) {
                        break;
                    }
                    Thread.sleep(100);
                    second = second+100;
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /* 영상 재생에 맞춰서 채팅을 RecyclerView 에 추가하기 위한 핸들러 */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            chatAdapter.notifyDataSetChanged();
            vodChat_R.scrollToPosition(chatAdapter.getItemCount() - 1);
        }
    };

    /* ExoPlayer */
    private DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter, buildHttpDataSourceFactory(bandwidthMeter));
    }
    private HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    /* ExoPlayer 이벤트 리스너
     *  - onSeekProcessed : 사용자가 하단 seekBar 를 조작할 때 불려지는 이벤트 리스너 메소드
     *  - getCurrentPosition 으로 현재 seekBar 의 millis 를 가져와서 채팅의 millis 와 같으면 채팅을 Array 에 추가한다 */
    public class PlayerListener extends Player.DefaultEventListener {

        @Override
        public void onSeekProcessed() {
            super.onSeekProcessed();
            int seekTime = (int)simpleExoPlayer.getCurrentPosition();
            second = (seekTime/100) * 100;
            if(syncChatArray.size() > 0) {
                syncChatArray.clear();
            }
            for(int i=0 ; i<vodChatArray.size() ; i++) {
                if(vodChatArray.get(i).getSyncTime() <= seekTime) {
                    syncChatArray.add(vodChatArray.get(i));
                    Message handlerM = handler.obtainMessage();
                    handler.sendMessage(handlerM);
                }else if(vodChatArray.get(0).getSyncTime() > seekTime){
                    syncChatArray.clear();
                    Message handlers = handler.obtainMessage();
                    handler.sendMessage(handlers);
                }
            }
        }
    }
}
