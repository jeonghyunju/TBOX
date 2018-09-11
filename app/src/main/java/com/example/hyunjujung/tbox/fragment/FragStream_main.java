package com.example.hyunjujung.tbox.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.adapter.broadcast.RealtimeAdapter;
import com.example.hyunjujung.tbox.adapter.broadcast.StreamLive_Adapter;
import com.example.hyunjujung.tbox.adapter.broadcast.StreamVOD_Adapter;
import com.example.hyunjujung.tbox.data_vo.broadcast.LiveAndVodArray;
import com.example.hyunjujung.tbox.data_vo.broadcast.RealtimeListVO;
import com.example.hyunjujung.tbox.data_vo.broadcast.StreamLiveListVO;
//import com.example.hyunjujung.tbox.databinding.FragstreamMainBinding;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;

import org.w3c.dom.Text;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ 메인 화면으로써 ToysLogin 액티비티에서 로그인 후 넘어오는 화면이다 ]
 *
 *  - 실시간 급상승 방송 목록과 현재 방송중인 목록이 표시된다
 *  - 방송 목록들은 데이터베이스의 livestream_list 테이블에서 가져온다
 *  - 실시간 급상승 방송 목록 : 현재 방송중인 목록에서 10분 동안 100명 이상이 들어온 방송 10개를 시청자가 많은 순으로 가져온다
 *  - 현재 방송중인 목록 : livestream_list 테이블을 전부 가져온다
 *
 */

public class FragStream_main extends Fragment {
    //FragstreamMainBinding dataBindings_stream;

    /* 실시간 방송 목록 RecyclerView 와 어댑터 */
    RecyclerView streamLiveRecycle;
    LinearLayoutManager live_recLayout;
    ArrayList<StreamLiveListVO> liveList_array = new ArrayList<>();
    StreamLive_Adapter streamLive_adapter;

    /* VOD 목록 RecyclerView 와 어댑터 */
    RecyclerView vodRecycle;
    LinearLayoutManager vod_recLayout;
    ArrayList<StreamLiveListVO> vodList_array = new ArrayList<>();
    StreamVOD_Adapter VOD_adapter;

    /* 실시간 급상승 목록 RecyclerView 와 어댑터 */
    RecyclerView stream_top_Recycle;
    LinearLayoutManager realtimeLM;
    RealtimeAdapter realtimeAdapter;
    ArrayList<RealtimeListVO> realtimeArray = new ArrayList<>();

    /* 로그인 유저 아이디, 프로필 저장 변수 */
    String loginId, loginPro, loginAge, loginGender = "";

    SwipeRefreshLayout swipeRefresh;
    TextView noBroadcast, no_streamTop;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //dataBindings_stream = DataBindingUtil.inflate(inflater, R.layout.fragstream_main, container, false);
        View view = inflater.inflate(R.layout.fragstream_main, container, false);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        streamLiveRecycle = view.findViewById(R.id.stream_live_Recycle);
        vodRecycle = view.findViewById(R.id.vod_Recycle);
        stream_top_Recycle = view.findViewById(R.id.stream_top_Recycle);
        noBroadcast = view.findViewById(R.id.no_broadcast);
        no_streamTop = view.findViewById(R.id.no_streamTop);

        /* 실시간 방송 목록 당겨서 새로고침 이벤트 리스너 */
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(liveList_array.size() > 0 || vodList_array.size() > 0) {
                    liveList_array.clear();
                    vodList_array.clear();
                    realtimeArray.clear();
                    selectLivelist();
                }else {
                    selectLivelist();
                }
                swipeRefresh.setRefreshing(false);
            }
        });

        SharedPreferences loginShared = getContext().getSharedPreferences("logins", Context.MODE_PRIVATE);
        loginId = loginShared.getString("loginId", null);
        loginPro = loginShared.getString("login_pro", null);
        loginAge = loginShared.getString("loginAge", null);
        loginGender = loginShared.getString("loginGender", null);

        //return dataBindings_stream.getRoot();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(liveList_array.size() > 0 || vodList_array.size() > 0) {
            liveList_array.clear();
            vodList_array.clear();
        }

        /* 실시간 방송 목록 Recyclerview 에 layoutmanager 설정 */
        live_recLayout = new LinearLayoutManager(getContext());
        streamLiveRecycle.setLayoutManager(live_recLayout);

        /* VOD 목록 Recyclerview 에 layoutmanager 설정 */
        vod_recLayout = new LinearLayoutManager(getContext());
        vodRecycle.setLayoutManager(vod_recLayout);

        /* 실시간 급상승 목록 설정 */
        realtimeLM = new LinearLayoutManager(getContext());
        realtimeLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        stream_top_Recycle.setLayoutManager(realtimeLM);

        /* 실시간 방송 목록 Recyclerview 에 어댑터 바인드 */
        streamLive_adapter = new StreamLive_Adapter(getActivity().getApplicationContext(), liveList_array, loginId, loginAge, loginGender);
        streamLiveRecycle.setAdapter(streamLive_adapter);

        /* VOD 목록 Recyclerview 에 어댑터 바인드 */
        VOD_adapter = new StreamVOD_Adapter(getActivity().getApplicationContext(), vodList_array);
        vodRecycle.setAdapter(VOD_adapter);

        /* 실시간 급상승 목록 어댑터 바인드 */
        realtimeAdapter = new RealtimeAdapter(getActivity().getApplicationContext(), realtimeArray);
        stream_top_Recycle.setAdapter(realtimeAdapter);

        selectLivelist();

    }

    /* 데이터베이스의 livestream_list 테이블에서 생방송 목록과 VOD 목록을 가져오기 위한 메소드
     *  - liveList_array : 생방송 목록을 담는 ArrayList
     *  - vodList_array : VOD 목록을 담는 ArrrayList */
    public void selectLivelist() {
        Retrofit_ApiConfig apiConfig = Retrofit_Creator.getApiConfig();
        Call<LiveAndVodArray> call = apiConfig.select_liveList("host_ID");
        call.enqueue(new Callback<LiveAndVodArray>() {
            @Override
            public void onResponse(Call<LiveAndVodArray> call, Response<LiveAndVodArray> response) {
                liveList_array.addAll(response.body().getLivelistArray());
                vodList_array.addAll(response.body().getVodlistArray());
                realtimeArray.addAll(response.body().getRealtimeArray());

                if(liveList_array.size() > 0) {
                    noBroadcast.setVisibility(View.INVISIBLE);
                }else {
                    noBroadcast.setVisibility(View.VISIBLE);
                }

                if(realtimeArray.size() > 0) {
                    no_streamTop.setVisibility(View.INVISIBLE);
                }else {
                    no_streamTop.setVisibility(View.VISIBLE);
                }

                streamLive_adapter.updateList(liveList_array);
                VOD_adapter.updateVodlist(vodList_array);
                realtimeAdapter.updateRealtime(realtimeArray);
            }
            @Override
            public void onFailure(Call<LiveAndVodArray> call, Throwable t) {
                Log.e("라이브 목록 가져오기", t.getMessage());
            }
        });
    }

}
