package com.example.hyunjujung.tbox.adapter.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.ServerResponse;
import com.example.hyunjujung.tbox.data_vo.broadcast.StreamLiveListVO;
import com.example.hyunjujung.tbox.retrofit.Retrofit_ApiConfig;
import com.example.hyunjujung.tbox.retrofit.Retrofit_Creator;
import com.example.hyunjujung.tbox.streaming_main.broadcast.LivestreamPlay;
import com.example.hyunjujung.tbox.streaming_main.OtherUserMain;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 *  [ 실시간 방송 목록 Recyclerview 어댑터 클래스 ]
 *
 *  - FragStream_main 화면에 있는 실시간 방송 목록(Recyclerview) 의 데이터를 담을 어댑터 클래스이다
 *  - 보고싶은 방송을 클릭했을때 클릭 된 방송의 idx와 경로를 가지고 플레이어 화면으로 이동한다
 *
 */

public class StreamLive_Adapter extends RecyclerView.Adapter<StreamLive_Adapter.ViewHolder>{
    Context context;
    ArrayList<StreamLiveListVO> streamlive_array;
    String SERVER_URL = "http://52.78.51.174/";
    String loginId, loginAge, loginGender;

    public StreamLive_Adapter(Context context, ArrayList<StreamLiveListVO> streamlive_array, String loginId, String loginAge, String loginGender) {
        this.context = context;
        this.streamlive_array = streamlive_array;
        this.loginId = loginId;
        this.loginAge = loginAge;
        this.loginGender = loginGender;
    }

    @Override
    public StreamLive_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stream_live_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        if(streamlive_array.get(position).getHost_Profile().equals("")) {
            /* 회원가입 시에 사용자가 프로필 사진을 설정 안했을때는 아이콘으로 프로필을 대체한다 */
            holder.live_host_profile.setImageResource(R.drawable.ic_clickuser);
        }else {
            Glide.with(context).load(SERVER_URL + "user_profile/" +  streamlive_array.get(position).getHost_Profile()).into(holder.live_host_profile);
        }

        /* 생방송 썸네일 */
        if(!streamlive_array.get(position).getVod_thumbnail().equals(null)) {
            Glide.with(context).load(SERVER_URL + "vodThumb/" + streamlive_array.get(position).getVod_thumbnail()).into(holder.live_thumbnail);
        }

        holder.live_subject.setText(streamlive_array.get(position).getLive_title());
        holder.live_pCount.setText(streamlive_array.get(position).getLive_count() + "");
        holder.live_host_ID.setText(streamlive_array.get(position).getHost_ID());

        /* 생방송 목록 클릭 이벤트
         * - 클릭 된 방송의 idx 와 경로를 가지고 플레이 화면으로 이동한다
         * - vod_tag = 0 : 라이브 방송이면 play 화면에서 컨트롤러 보이지 않는다.
         * - liveStart : 생방송 시작 시간 (milliSecond)*/
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent go_live_play = new Intent(context.getApplicationContext(), LivestreamPlay.class);
                go_live_play.putExtra("live_idx", streamlive_array.get(position).getLive_idx());
                go_live_play.putExtra("live_path", streamlive_array.get(position).getLive_path());
                go_live_play.putExtra("vod_tag", streamlive_array.get(position).getVod_tag());
                go_live_play.putExtra("liveStart", streamlive_array.get(position).getLive_startTime());
                go_live_play.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(go_live_play);

                /* 라이브 방송 시청하려고 클릭하면 시청자 통계를 위해 Nosql 의 tobx.chart 에 insert 된다
                 *  - 구성 bins
                 *      : chartPK, hostUser, userAge, userGender, year, month, day */
                insertChart(loginId + "-" + streamlive_array.get(position).getLive_path(),
                               loginAge, loginGender, streamlive_array.get(position).getHost_ID());
            }
        });

        /* 프로필 이미지 클릭 리스너
         *  - 클릭하면 프로필에 해당하는 유저의 타임라인으로 이동한다 */
        holder.live_host_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!loginShared().equals(streamlive_array.get(position).getHost_ID())) {
                    Intent goTimeline = new Intent(context.getApplicationContext(), OtherUserMain.class);
                    goTimeline.putExtra("host_Id", streamlive_array.get(position).getHost_ID());
                    goTimeline.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(goTimeline);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return streamlive_array.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView live_thumbnail;
        public CircleImageView live_host_profile;
        public TextView live_host_ID, live_subject, live_pCount, vod_tag;

        public ViewHolder(View view) {
            super(view);
            live_thumbnail = (ImageView)view.findViewById(R.id.live_thumbnail);
            live_host_profile = (CircleImageView)view.findViewById(R.id.live_host_profile);
            live_host_ID = (TextView)view.findViewById(R.id.live_host_ID);
            live_subject = (TextView)view.findViewById(R.id.live_subject);
            live_pCount = (TextView)view.findViewById(R.id.live_pCount);
            vod_tag = (TextView)view.findViewById(R.id.vod_tag);
        }
    }

    public void updateList(ArrayList<StreamLiveListVO> updatelist) {
        this.streamlive_array = updatelist;
        notifyDataSetChanged();
    }

    public String loginShared() {
        SharedPreferences loginShared = context.getSharedPreferences("logins", Context.MODE_PRIVATE);
        return loginShared.getString("loginId", null);
    }

    /* 시청자 통계를 위한 Nosql insert 메소드
     *  - 라이브 방송 클릭 시에 아이디, 연령, 성별, BJ 아이디를 데이터베이스에 저장한다 */
    public void insertChart(String loginId, String loginAge, String loginGender, String hostId) {
        Retrofit_ApiConfig insertNosql = Retrofit_Creator.getApiConfig();
        Call<ServerResponse> insertCall = insertNosql.insert_chart(loginId, loginAge, loginGender, hostId);
        insertCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }
}
